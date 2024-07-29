package com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.PinnedMessage
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.model.UserSettings
import com.android.kotlin.familymessagingapp.services.firebase_services.storage.FirebaseStorageService
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * ### Spec
 * #### Bug crash
 * Description:
 * W  Listen at /users_information_data/ fp5VSXmPSyfIrjuWDT7Az1KfkZp2 failed: DatabaseError: This client does not have permission to perform this operation
 * W  Listen at /chatrooms/0 failed: DatabaseError: This client does not have permission to perform this operation
 *
 * Solution: remove all listener before logout
 *
 * #### Problem
 * - [chatroomObserver] When chatroom list of chatroom is changed all chatroom listener cancelled. This is not really optimal. Chatrooms that still exist should not be deleted and re-added.
 */
class FirebaseRealtimeDatabaseService(
    private val auth: FirebaseAuth,
    private val firebaseStorageService: FirebaseStorageService
) {

    companion object {
        val TAG: String = FirebaseRealtimeDatabaseService::class.java.simpleName
    }

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private val registeredChatRoomsListeners = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val registerUserDataListener = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val registerChatroomListener = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val privateUserDataRef = Firebase.database.reference.child(Constant.FIREBASE_REALTIME_DATABASE_PRIVATE_USER_DATA)

    private val databaseReference = Firebase.database.reference

    private val userDataRef = databaseReference.child(Constant.REALTIME_DATABASE_USER_REF_NAME)

    private val chatRoomsRef = databaseReference.child(Constant.REALTIME_DATABASE_CHAT_ROOM_REF)

    private val userPrivateInformationRef = databaseReference
        .child(Constant.FIREBASE_REALTIME_DATABASE_USER_PRIVATE_INFO_REF_NAME)

    private val searchHistoryRef = databaseReference
        .child(Constant.FIREBASE_REALTIME_DATABASE_USER_PRIVATE_INFO_REF_NAME)
        .child(Constant.FIREBASE_REALTIME_DATABASE_SEARCH_HISTORY_REF_NAME)

    private val userAvatarImageRef = firebaseStorageService.userAvatarRef

    private val _currentUserData: MutableLiveData<UserData?> = MutableLiveData(UserData())
    val currentUserData: LiveData<UserData?> = _currentUserData

    private val _chatRooms = MutableStateFlow<List<ChatRoom>?>(null)
    val chatRooms: StateFlow<List<ChatRoom>?> = _chatRooms

    private val userdataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            _currentUserData.value = snapshot.getValue(UserData::class.java)
        }

        override fun onCancelled(error: DatabaseError) {
            _currentUserData.value = null
        }
    }

    // Current chatroom list of current user
    private val chatroomIDList: MutableList<String> = mutableListOf()

    // Clean up the coroutine scope when the service is no longer needed
    fun cleanup() {
        job.cancel()
    }

    /**
     * #### Listen to the changes of the current User's chat room list
     * This method is called when userdata changed
     *
     * Add chatroom listener when chatroom id list from user not empty and chatroom id list is changed
     *
     * @param userData latest userdata from current user
     */
    suspend fun chatroomObserver(userData: UserData?) {
        if (userData != null && !userData.uid.isNullOrEmpty()) {
            val chatRooms = userData.chatrooms
            if (chatRooms.isNullOrEmpty()) {
                clearChatRoomsListener()
                _chatRooms.value = emptyList()
                chatroomIDList.clear()
            } else {
                if (StringUtils.areListsEqual(chatRooms, chatroomIDList)) return
                clearChatRoomsListener()
                chatroomIDList.clear()
                chatroomIDList.addAll(chatRooms)
                val chatroomFlows: List<Flow<ChatRoom?>> = chatRooms.map {
                    getChatRoomFlow(it, userData)
                }
                combine(chatroomFlows) {
                    // Sort chat room list by latestTime
                    it.filterNotNull().toList()
                        .sortedByDescending { chatroom -> chatroom.latestTime }
                }.collect {
                    _chatRooms.value = it
                }
            }
        } else {
            clearChatRoomsListener()
            _chatRooms.value = null
            chatroomIDList.clear()
        }
    }

    /**
     * Add UserData Listener
     *
     * Description: add listener to userdata if user is authenticated
     */
    fun addUserDataListener() {
        coroutineScope.launch {
            if (registerUserDataListener.keys.isEmpty() && !auth.uid.isNullOrEmpty()) {
                val currentUserRef = userDataRef.child(auth.uid!!)
                currentUserRef.addValueEventListener(userdataListener)
                registerUserDataListener[currentUserRef] = userdataListener
                sendFCMTokenToServer()
            }
        }
    }

    private fun sendFCMTokenToServer() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful && auth.uid == null) return@addOnCompleteListener
            val token = task.result
            privateUserDataRef.child(auth.uid!!).child(Constant.FCM_TOKEN).setValue(token)
        }
    }

    /**
     * Đăng ký trình nghe tại phòng có id của user hiện tại
     * chỉ đăng ký 1 lần và huỷ khi Flow đóng
     * @param chatroomId chatroomID of userdata
     */
    private fun getChatRoomFlow(
        chatroomId: String,
        currentUserData: UserData
    ): Flow<ChatRoom?> = callbackFlow {
        val chatroomRef = chatRoomsRef.child(chatroomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var chatroom = snapshot.getValue(ChatRoom::class.java)
                    if (chatroom != null) {
                        val listOfOtherMembers = chatroom.members?.filter { it != auth.uid }
                        val membersData: MutableList<UserData> = mutableListOf()
                        membersData.add(currentUserData)
                        if (!listOfOtherMembers.isNullOrEmpty()) {
                            val deferredList = listOfOtherMembers.map { memberId ->
                                // async và awaitAll: Để lấy dữ liệu của các memberId khác song song,
                                // bạn có thể sử dụng async để tạo các tác vụ không đồng bộ cho từng truy
                                // vấn Firebase và sau đó sử dụng awaitAll để chờ tất cả các truy vấn hoàn thành.
                                async {
                                    val userData = getUserData(memberId)
                                    userData?.let { membersData.add(it) }
                                }
                            }
                            // Wait for all data to be collected
                            launch {
                                deferredList.awaitAll()
                                chatroom = chatroom?.copy(membersData = membersData)
                                chatroom?.getChatRoomNameAndImage()
                                trySend(chatroom).isSuccess
                            }
                        } else {
                            trySend(null).isSuccess
                        }
                    } else {
                        trySend(null).isSuccess
                    }
                } else {
                    trySend(null).isSuccess
                    close()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null).isSuccess
                close(error.toException())
            }
        }

        chatroomRef.addValueEventListener(listener)
        registeredChatRoomsListeners[chatroomRef] = listener

        awaitClose {
            chatroomRef.removeEventListener(listener)
            registeredChatRoomsListeners.remove(chatroomRef)
        }
    }

    /**
     * suspendCoroutine: Hàm này chuyển đổi callback của Firebase thành một hàm suspend,
     * giúp dễ dàng sử dụng trong coroutine.
     */
    private suspend fun getUserData(uid: String): UserData? = suspendCoroutine { continuation ->
        userDataRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)
                continuation.resume(userData)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(null)
            }
        })
    }

    suspend fun updateEnabledAIUserData(enabled: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val uid = auth.uid
                if (uid != null) {
                    userDataRef
                        .child(uid)
                        .child(UserData.SETTINGS)
                        .child(UserSettings.ENABLED_AI)
                        .setValue(enabled)
                        .await()
                    true
                } else
                    false
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun saveUserData(userData: UserData, imageUri: Uri?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var updatedUserData = userData
                if (imageUri != null) {
                    val downloadUrl = firebaseStorageService.putUserAvatarUriToStorage(
                        imageUri,
                        firebaseStorageService.userAvatarRef.child(userData.uid!!)
                    )
                    updatedUserData = userData.copy(userAvatar = downloadUrl)
                }
                userDataRef.child(userData.uid!!).setValue(updatedUserData).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Check chatroom exist
     *
     * @param otherUserId user who click from search result
     */
    suspend fun checkChatRoomExist(otherUserId: String): String? = withContext(Dispatchers.IO) {
        try {
            val currentUserUid = auth.uid
            if (currentUserUid == null) {
                null
            } else {
                val chatroomIds = listOf(
                    StringUtils.generateChatRoomId(otherUserId, currentUserUid),
                    StringUtils.generateChatRoomId(currentUserUid, otherUserId)
                )

                chatroomIds.firstOrNull { chatroomId ->
                    chatRoomsRef.child(chatroomId).get().await().exists()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteUserData(uid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                userDataRef.child(uid).removeValue().await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun addChatRoomListener(chatroomId: String): Flow<ChatRoom?> = callbackFlow {
        val messageRef = chatRoomsRef.child(chatroomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatroom = snapshot.getValue(ChatRoom::class.java)
                trySend(chatroom).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
                close(error.toException())
            }
        }
        messageRef.addValueEventListener(listener)
        registerChatroomListener[messageRef] = listener
        awaitClose { messageRef.removeEventListener(listener) }
    }

    fun removeChatRoomListener() {
        registerChatroomListener.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        registerChatroomListener.clear()
    }

    /**
     * Problem: Với trường username
     * Chỉ tìm kiếm được được khi keyword giống 100% username
     * Ví dụ: keyword = "long", username trên server = "Long" => kết quả sẽ sai
     * Solution: thêm trường usernameLowercase vào UserData
     * usernameLowercase = username.toLoweCase
     */
    suspend fun search(keyword: String): List<UserData> {
        return try {
            val query = when {
                StringUtils.isValidEmail(keyword) -> userDataRef.orderByChild(UserData.EMAIL)
                    .equalTo(keyword)

                StringUtils.isNumber(keyword) -> userDataRef.orderByChild(UserData.PHONE_NUMBER)
                    .equalTo(keyword)

                else -> userDataRef.orderByChild(UserData.USERNAME).equalTo(keyword)
            }

            val dataSnapshot = query.get().await()
            if (dataSnapshot.exists()) {
                dataSnapshot.children.mapNotNull { snapshot ->
                    snapshot.getValue(UserData::class.java)?.takeIf { it.uid != auth.uid }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    /**
     * Lưu tin nhắn mới vào chatroom
     * sẽ có 2 trường hợp xảy ra
     * 1. chatroom đã tồn tại - có tin nhắn trong chatroom này
     *      - lưu tin nhắn mới vào chatroom đó theo vào message path  với key là messageID
     * 2. chatroom mới - lần đầu tiên người dùng tạo tin nhắn trong chatroom (Trong trường hợp người
     * dùng đến từ chức năng tìm kiếm)
     *      - Khởi tạo chatroom trên realtime bao gồm cả tin nhắn đầu tiên (đầy đủ thông tin)
     *      - lưu chatroomID vào userdata của các member trong phòng
     * @param chatRoom
     * @param message new message that send by user
     */
    suspend fun updateNewMessage(chatRoom: ChatRoom, message: Message): Result<ChatRoom?> {
        var newChatRoom: ChatRoom? = null
        return withContext(Dispatchers.IO) {
            try {
                // Upload photo if it exists
                val photoUrl = uploadPhotoMessageToStorage(message.photo)

                val currentTimestamp = StringUtils.getCurrentTime()

                val updatedMessage = message.copy(
                    messageId = currentTimestamp.toString(),
                    timestamp = currentTimestamp,
                    photo = photoUrl,
                    text = message.text,
                    audio = message.audio,
                    senderId = auth.uid,
                    video = message.video,
                    type = message.type,
                    status = message.status,
                    emoticon = message.emoticon,
                    replyMessageId = message.replyMessageId
                )

                if (chatRoom.chatRoomId == null) {
                    // Thêm uid của user hiện tại vào chatroom
                    val members = mutableListOf<String>()
                    chatRoom.members?.firstOrNull()?.let { members.add(it) }
                    members.add(auth.uid!!)
                    val finalChatRoom = ChatRoom(
                        chatRoomId = StringUtils.generateChatRoomId(members[0], members[1]),
                        members = members,
                        messages = hashMapOf(updatedMessage.messageId!! to updatedMessage),
                        latestTime = currentTimestamp,
                        lastMessage = updatedMessage
                    )
                    val chatroomID = finalChatRoom.chatRoomId!!
                    chatRoomsRef.child(chatroomID)
                        .setValue(finalChatRoom)
                        .await()
                    newChatRoom = finalChatRoom

                    // Update user data with chat room ID
                    val updateUserChatRooms: suspend (String) -> Unit = { memberId ->
                        val userChatRoomsRef =
                            userDataRef.child(memberId).child(UserData.CHAT_ROOMS)
                        val userDataSnapshot = userChatRoomsRef.get().await()
                        val currentChatRooms = userDataSnapshot.getValue(object :
                            GenericTypeIndicator<List<String>>() {}) ?: listOf()
                        val updatedChatRooms = currentChatRooms.toMutableList().apply {
                            if (!contains(chatroomID)) {
                                add(chatroomID)
                            }
                        }
                        userChatRoomsRef.setValue(updatedChatRooms).await()
                    }

                    members.forEach { member -> updateUserChatRooms(member) }
                } else {
                    val chatRoomUpdates = mapOf(
                        "${ChatRoom.MESSAGES}/${updatedMessage.messageId}" to updatedMessage,
                        ChatRoom.LAST_MESSAGE to updatedMessage,
                        ChatRoom.LATEST_TIME to currentTimestamp
                    )

                    chatRoomsRef.child(chatRoom.chatRoomId)
                        .updateChildren(chatRoomUpdates)
                        .await()
                }
                Result.Success(newChatRoom)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    private suspend fun uploadPhotoMessageToStorage(photo: String?): String? {
        return photo?.let {
            firebaseStorageService.putUserAvatarUriToStorage(
                imageUri = it.toUri(),
                storageRef = firebaseStorageService.chatroomRef.child(
                    StringUtils.getCurrentTime().toString()
                )
            )
        }
    }

    private fun clearChatRoomsListener() {
        registeredChatRoomsListeners.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        registeredChatRoomsListeners.clear()
    }

    private fun clearUserDataListener() {
        registerUserDataListener.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        registerUserDataListener.clear()
    }

    fun removeAllListener() {
        clearUserDataListener()
        clearChatRoomsListener()
        _currentUserData.value = UserData()
        _chatRooms.value = null
        chatroomIDList.clear()
    }

    /**
     * Update new user data in realtime database
     * Check current UserData Node has exist or not
     * Add UserData Listener
     *
     * This method is called when Login with Google Account [com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication.FirebaseGoogleService.signInWithIntent]
     *
     * Add UserData listener when userdata node exist or update new data in realtime database success
     *
     * @param userData Userdata from FirebaseUser
     *
     */
    suspend fun updateNewUserDataInRealtime(userData: UserData) {
        // This method is called when firebaseUser is not null, so uid will always be non-null
        val currentUid = auth.uid!!
        // Get the current user node reference
        val userInfoSnapshot = userDataRef.child(currentUid).get().await()
        // If the node is null, then return
        if (userInfoSnapshot.exists()) {
            addUserDataListener()
            return
        }
        var updatedUserData = userData
        // Check google account if have avatar url then update it to firebase storage and
        // get user update userdata
        if (!userData.userAvatar.isNullOrEmpty()) {
            val downloadUrl = firebaseStorageService.createDownloadUrlFromImageUrl(
                userData.userAvatar,
                userAvatarImageRef.child(currentUid)
            )
            // Update user data with avatar URL
            updatedUserData = updatedUserData.copy(userAvatar = downloadUrl)
        }
        // Update the user data in the Realtime Database
        userDataRef.child(currentUid).setValue(updatedUserData).await()
        addUserDataListener()
    }

    suspend fun addNewPinnedMessage(
        chatRoom: ChatRoom,
        pinnedMessage: PinnedMessage
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            chatRoomsRef
                .child(chatRoom.chatRoomId!!)
                .child(ChatRoom.PINNED_MESSAGES)
                .child(pinnedMessage.messageId!!)
                .setValue(pinnedMessage)
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deletePinnedMessage(
        chatRoom: ChatRoom,
        pinnedMessageId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            chatRoomsRef
                .child(chatRoom.chatRoomId!!)
                .child(ChatRoom.PINNED_MESSAGES)
                .child(pinnedMessageId)
                .removeValue()
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateEmojiMessage(
        chatroomId: String,
        messageId: String,
        emoji: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            chatRoomsRef.child(chatroomId)
                .child(ChatRoom.MESSAGES)
                .child(messageId)
                .child(Message.EMOTICON)
                .setValue(emoji)
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteMessage(chatroomId: String, messageId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                chatRoomsRef.child(chatroomId)
                    .child(ChatRoom.MESSAGES)
                    .child(messageId)
                    .removeValue()
                    .await()
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
}