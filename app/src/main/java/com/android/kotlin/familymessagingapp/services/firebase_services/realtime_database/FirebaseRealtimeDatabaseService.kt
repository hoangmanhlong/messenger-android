package com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database

import android.net.Uri
import androidx.core.net.toUri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Bug crash
 * Description:
 * W  Listen at /users_information_data/ fp5VSXmPSyfIrjuWDT7Az1KfkZp2 failed: DatabaseError: This client does not have permission to perform this operation
 * W  Listen at /chatrooms/0 failed: DatabaseError: This client does not have permission to perform this operation
 *
 * Solution: remove all listener before logout
 */
class FirebaseRealtimeDatabaseService(
    private val auth: FirebaseAuth,
    private val firebaseStorageService: FirebaseStorageService
) {

    private val registeredChatRoomsListeners = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val registerUserDataListener = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val registerChatroomListener = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val databaseReference = Firebase.database.reference

    companion object {
        val TAG: String = FirebaseRealtimeDatabaseService::class.java.simpleName
    }

    val userDataRef = databaseReference.child(Constant.REALTIME_DATABASE_USER_REF_NAME)

    private val chatRoomsRef = databaseReference.child(Constant.REALTIME_DATABASE_CHAT_ROOM_REF)

    private val userAvatarImageRef = firebaseStorageService.userAvatarRef

    /**
     * Event Listener Flow of current User Data
     */
    val currentUserDataFlow: Flow<UserData?>
        get() = callbackFlow {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                close()
                return@callbackFlow
            }
            val currentUserRef: DatabaseReference = userDataRef.child(currentUser.uid)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    this@callbackFlow.trySend(snapshot.getValue(UserData::class.java)).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    this@callbackFlow.trySend(null).isSuccess
                    close()
                }
            }
            currentUserRef.addValueEventListener(listener)
            registerUserDataListener[currentUserRef] = listener
            awaitClose {
                currentUserRef.removeEventListener(listener)
                registerUserDataListener.remove(currentUserRef)
            }
        }

    /**
     * Nghe user data mới nhất từ [currentUserDataFlow]
     * Nếu là null thì hủy toàn bộ trình nghe và kết thúc
     * Nếu khác null thì lấy toàn bộ chatroom snapshot theo chatroomID trong userData
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val chatRoomsFlow: Flow<List<ChatRoom>>
        get() = currentUserDataFlow.flatMapLatest { userData ->
            if (userData != null) {
                val chatRooms = userData.chatrooms
                if (chatRooms.isNullOrEmpty()) {
                    flowOf(emptyList()) // Emit empty list for empty chatrooms
                } else {
                    val chatroomFlows: List<Flow<ChatRoom?>> = chatRooms.map {
                        getChatRoomFlow(it, userData)
                    }
                    combine(chatroomFlows) {
                        // Sort chat room list by latestTime
                        it.filterNotNull().toList()
                            .sortedByDescending { chatroom -> chatroom.latestTime }
                    }
                }
            } else {
                registeredChatRoomsListeners.forEach {
                    chatRoomsRef.removeEventListener(it.value)
                }
                flowOf(emptyList())
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
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
                trySend(null).isSuccess
            }
        }

        // Check if listener is already registered
        if (!registeredChatRoomsListeners.containsKey(chatroomRef)) {
            chatroomRef.addValueEventListener(listener)
            registeredChatRoomsListeners[chatroomRef] = listener
        }

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
    }

    suspend fun updateNewUserDataInRealtime(userData: UserData) {
        val currentUid = userData.uid
        if (!currentUid.isNullOrEmpty()) {
            if (!userData.userAvatar.isNullOrEmpty()) {
                val downloadUrl = firebaseStorageService.createDownloadUrlFromImageUrl(
                    userData.userAvatar,
                    userAvatarImageRef.child(userData.uid)
                )
                // Update user data with avatar URL
                val updatedUserData = userData.copy(userAvatar = downloadUrl)
                userDataRef.child(currentUid).setValue(updatedUserData).await()
            } else {
                userDataRef.child(currentUid).setValue(userData).await()
            }
        }
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
        pinnedMessage: PinnedMessage
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            chatRoomsRef
                .child(chatRoom.chatRoomId!!)
                .child(ChatRoom.PINNED_MESSAGES)
                .child(pinnedMessage.messageId!!)
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
}