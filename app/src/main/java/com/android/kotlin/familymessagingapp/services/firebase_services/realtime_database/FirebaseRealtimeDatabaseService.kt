package com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.remote.ServerCode
import com.android.kotlin.familymessagingapp.data.remote.socket.CreateNewChatRoomSocketEvenBus
import com.android.kotlin.familymessagingapp.data.remote.socket.SocketClient
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.android.kotlin.familymessagingapp.model.Contact
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.MobileConfig
import com.android.kotlin.familymessagingapp.model.PinnedMessage
import com.android.kotlin.familymessagingapp.model.PrivateUserData
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.ServerErrorException
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.model.toContact
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.android.kotlin.familymessagingapp.screen.chatroom.NewChatRoomEventBus
import com.android.kotlin.familymessagingapp.services.firebase_services.fcm.FCMService
import com.android.kotlin.familymessagingapp.services.firebase_services.storage.FirebaseStorageService
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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
 * Fatal Exception: java.lang.NullPointerException: Can't pass null for argument 'pathString' in child()
 *
 * Solution: check again index of element in array(0, 1, 2...), If(0, 2, 3,...) will crash
 *
 * #### Problem
 * - [chatroomObserver] When chatroom list of chatroom is changed all chatroom listener cancelled. This is not really optimal. Chatrooms that still exist should not be deleted and re-added.
 */
class FirebaseRealtimeDatabaseService(
    private val auth: FirebaseAuth,
    private val firebaseStorageService: FirebaseStorageService,
    private val fcmService: FCMService,
    private val socketClient: SocketClient,
    private val localDatabaseRepository: LocalDatabaseRepository
) {

    companion object {
        val TAG: String = FirebaseRealtimeDatabaseService::class.java.simpleName
    }

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private val registeredChatRoomsListeners = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val registerPublicUserDataListener =
        mutableMapOf<DatabaseReference, ValueEventListener>()

    private val registerPrivateUserDataListener =
        mutableMapOf<DatabaseReference, ValueEventListener>()

    private val registerChatroomListener = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val databaseReference = Firebase.database.reference

    private val privateUserDataRef =
        databaseReference.child(Constant.FIREBASE_REALTIME_DATABASE_PRIVATE_USER_DATA_REF_NAME)

    private val secureUserDataRef =
        databaseReference.child(Constant.FIREBASE_REALTIME_DATABASE_SECURE_USER_DATA_REF_NAME)

    private val publicUserDataRef =
        databaseReference.child(Constant.REALTIME_DATABASE_PUBLIC_USER_DATA_REF_NAME)

    private val chatRoomsRef =
        databaseReference.child(Constant.FIREBASE_REALTIME_DATABASE_CHAT_ROOM_REF)

    private val userAvatarImageRef = firebaseStorageService.userAvatarRef

    private val _publicUserData: MutableLiveData<UserData?> = MutableLiveData(UserData())
    val publicUserData: LiveData<UserData?> = _publicUserData

    private val _privateUserData: MutableLiveData<PrivateUserData?> = MutableLiveData(null)
    val privateUserData: LiveData<PrivateUserData?> = _privateUserData

    private val _chatRooms = MutableStateFlow<List<ChatRoom>?>(null)
    val chatRooms: StateFlow<List<ChatRoom>?> = _chatRooms

    /**
     * Message sent when creating a double chat room
     */
    private var messageSentWhenCreatingDoubleChatRoom: Message? = null

    private var chatRoomSentWhenCreatingDoubleChatRoom: ChatRoom? = null

    private val privateUserdataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            updatePrivateUserData(snapshot.getValue(PrivateUserData::class.java))
        }

        override fun onCancelled(error: DatabaseError) {
            updatePrivateUserData(null)
        }
    }

    private val publicUserdataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            _publicUserData.value = snapshot.getValue(UserData::class.java)
        }

        override fun onCancelled(error: DatabaseError) {
            _publicUserData.value = null
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
    private suspend fun chatroomObserver(chatRooms: List<String>?) {
        if (chatRooms.isNullOrEmpty()) {
            updateChatRoomListNullOrEmptyStatus()
        } else {
            if (StringUtils.areListsEqual(chatRooms, chatroomIDList)) return
            clearChatRoomsListener()
            chatroomIDList.clear()
            chatroomIDList.addAll(chatRooms)
            val chatroomFlows: List<Flow<ChatRoom?>> = chatRooms.map {
                getChatRoomFlow(it)
            }
            combine(chatroomFlows) {
                // Sort chat room list by latestTime
                it.filterNotNull().toList()
                    .sortedByDescending { chatroom -> chatroom.latestTime }
            }.collect {
                _chatRooms.value = it
            }
        }
    }

    private fun updateChatRoomListNullOrEmptyStatus() {
        clearChatRoomsListener()
        _chatRooms.value = emptyList()
        chatroomIDList.clear()
    }

    private fun updatePrivateUserData(privateUserData: PrivateUserData?) {
        _privateUserData.value = privateUserData
        coroutineScope.launch {
            localDatabaseRepository.appDataStore.saveBoolean(
                AppDataStore.ENABLED_AI,
                privateUserData?.mobileConfig?.turnOnSuggestedAnswers ?: false
            )

            if (privateUserData == null) updateChatRoomListNullOrEmptyStatus()
            else chatroomObserver(privateUserData.chatRooms)
        }
    }

    /**
     * Add UserData Listener
     *
     * Description: add listener to userdata if user is authenticated
     */
    fun addUserDataListener() {
        socketClient.connect()
        coroutineScope.launch {
            if (registerPublicUserDataListener.keys.isEmpty() && registerPrivateUserDataListener.keys.isEmpty() && !auth.uid.isNullOrEmpty()) {

                val currentUserRef = publicUserDataRef.child(auth.uid!!)
                currentUserRef.addValueEventListener(publicUserdataListener)
                registerPublicUserDataListener[currentUserRef] = publicUserdataListener

                val privateUserDataRef = privateUserDataRef.child(auth.uid!!)
                privateUserDataRef.addValueEventListener(privateUserdataListener)
                registerPrivateUserDataListener[privateUserDataRef] = privateUserdataListener

                sendFCMTokenToServer(auth.uid!!)
                socketClient.addOnlineStatusSocketListener(auth.uid!!)
                updateVerifiedStatus(true)
            }
        }
    }

    private suspend fun sendFCMTokenToServer(uid: String) {
        val fcmToken = fcmService.getFCMToken()
        if (!fcmToken.isNullOrEmpty()) {
            secureUserDataRef.child(uid).child(Constant.FCM_TOKEN).setValue(fcmToken)
        }
    }

    /**
     * Đăng ký trình nghe tại phòng có id của user hiện tại
     * chỉ đăng ký 1 lần và huỷ khi Flow đóng
     * @param chatroomId chatroomID of userdata
     */
    private fun getChatRoomFlow(
        chatroomId: String
    ): Flow<ChatRoom?> = callbackFlow {
        val chatroomRef = chatRoomsRef.child(chatroomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var chatroom = snapshot.getValue(ChatRoom::class.java)
                    if (chatroom != null) {
                        val listOfOtherMembers = chatroom.members?.filter { it != auth.uid }
                        val membersData: MutableList<UserData> = mutableListOf()
                        membersData.add(_publicUserData.value!!)
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
//                                notificationHelper.updateShortcuts(membersData)
                                chatroom?.getChatRoomNameAndImage()
                                chatroom?.getSenderDataOfMessage()
                                chatroom?.getLatestMessageData()
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
        publicUserDataRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)
                continuation.resume(userData)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(null)
            }
        })
    }

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        if (Firebase.auth.currentUser == null) return@withContext emptyList()
        try {
            _privateUserData.value?.contacts?.mapNotNull {
                async {
                    val userData = getUserData(it.key)
                    it.value.toContact().copy(contactData = userData)
                }
            }?.awaitAll() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateEnabledAIUserData(enabled: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val uid = auth.uid
                if (uid != null) {
                    privateUserDataRef
                        .child(uid)
                        .child(PrivateUserData.MOBILE_CONFIG)
                        .child(MobileConfig.TURN_ON_SUGGESTED_ANSWERS)
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
                    val downloadUrl = firebaseStorageService.putImageUriToStorage(
                        imageUri,
                        firebaseStorageService.userAvatarRef.child(userData.uid!!)
                    )
                    updatedUserData = userData.copy(userAvatar = downloadUrl)
                }
                publicUserDataRef.child(userData.uid!!).setValue(updatedUserData).await()
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
                publicUserDataRef.child(uid).removeValue().await()
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
    suspend fun search(keyword: String, searchByUid: Boolean): List<UserData> {
        return try {
            if (searchByUid) {
                val userData =
                    publicUserDataRef.child(keyword).get().await().getValue(UserData::class.java)
                if (userData == null) emptyList() else listOf(userData)
            } else {
                val query = when {
                    StringUtils.isValidEmail(keyword) -> publicUserDataRef.orderByChild(UserData.EMAIL)
                        .equalTo(keyword)

                    StringUtils.isNumber(keyword) -> publicUserDataRef.orderByChild(UserData.PHONE_NUMBER)
                        .equalTo(keyword)

                    else -> publicUserDataRef.orderByChild(UserData.USERNAME).equalTo(keyword)
                }

                val dataSnapshot = query.get().await()
                if (dataSnapshot.exists()) {
                    dataSnapshot.children.mapNotNull { snapshot ->
                        snapshot.getValue(UserData::class.java)?.takeIf { it.uid != auth.uid }
                    }
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun updateVerifiedStatus(isVerified: Boolean) {
        coroutineScope.launch {
            try {
                val uid = auth.uid ?: return@launch
                secureUserDataRef.child(uid)
                    .child(Constant.FIREBASE_REALTIME_DATABASE_VERIFIED_STATUS_REF_NAME)
                    .setValue(isVerified)
                    .await()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    /**
     * #### Create new message
     *
     * @param message The message object consists only of content fields such as [Message.text, Message.audio, Message.photo, Message.video]
     */
    private suspend fun createNewMessage(message: Message): Message {
        // Upload photo if it exists
        val photoUrl = uploadPhotoMessageToStorage(message.photo)

        val currentTimestamp = StringUtils.getCurrentTime()

        return message.copy(
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
    }

    /**
     * #### Push message to chatroom
     *
     * @param chatRoom A chat room already exists in the database.
     * @param message New messages sent by users
     */
    suspend fun pushMessageToChatRoom(chatRoom: ChatRoom, message: Message): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val newMessage = createNewMessage(message)

                // Add new message, update latest message, latest active time to chat room
                val chatRoomUpdates = mapOf(
                    "${ChatRoom.MESSAGES}/${newMessage.messageId}" to newMessage,
                    ChatRoom.LAST_MESSAGE to newMessage,
                    ChatRoom.LATEST_TIME to StringUtils.getCurrentTime()
                )

                // Update chat room to Firebase Realtime Database
                chatRoomsRef.child(chatRoom.chatRoomId!!).updateChildren(chatRoomUpdates).await()

                // send notify to socket to push new message to other user
                socketClient.emitNewMessageToOtherUser(chatRoom, newMessage)
                Result.Success(true)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private suspend fun updateChatRoomNameAndImage(chatRoom: ChatRoom): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                var chatRoomImage = chatRoom.chatRoomImage
                if (!chatRoomImage.isNullOrEmpty()) {
                    chatRoomImage = firebaseStorageService.putImageUriToStorage(
                        chatRoom.chatRoomImage!!.toUri(),
                        firebaseStorageService.initializeChatRoomImageRefInStorage(chatRoom.chatRoomId!!)
                    )
                }

                // Add new message, update latest message, latest active time to chat room
                val chatRoomUpdates = mapOf(
                    ChatRoom.CHAT_ROOM_IMAGE to chatRoomImage,
                    ChatRoom.LATEST_TIME to StringUtils.getCurrentTime(),
                    ChatRoom.CHAT_ROOM_NAME to chatRoom.chatroomName,
                )

                // Update chat room to Firebase Realtime Database
                chatRoomsRef.child(chatRoom.chatRoomId!!).updateChildren(chatRoomUpdates).await()

                Result.Success(true)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun createChatRoom(chatRoom: ChatRoom, message: Message?) {
        this.messageSentWhenCreatingDoubleChatRoom = message
        this.chatRoomSentWhenCreatingDoubleChatRoom = chatRoom
        if (chatRoom.members.isNullOrEmpty() || chatRoom.chatRoomType == null) return
        withContext(Dispatchers.IO) {
            EventBus.getDefault().register(this@FirebaseRealtimeDatabaseService)
            val result = socketClient.emitNewChatRoom(
                ChatRoom(members = chatRoom.members, chatRoomType = chatRoom.chatRoomType)
            )
            if (result is Result.Error) {
                EventBus.getDefault()
                    .postSticky(NewChatRoomEventBus(Result.Error(result.exception)))
                EventBus.getDefault().unregister(this@FirebaseRealtimeDatabaseService)
            }
        }
    }

    /**
     * #### Listener when new chatroom is created from socket event
     *
     * @param createNewChatRoomSocketEvenBus a Eventbus event object containing the response result from the server
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    fun createChatRoomStatus(createNewChatRoomSocketEvenBus: CreateNewChatRoomSocketEvenBus) {
        coroutineScope.launch {
            val chatRoom: ChatRoom? = createNewChatRoomSocketEvenBus.chatRoom
            val responseStatusCode: Int? = createNewChatRoomSocketEvenBus.responseStatusCode
            var newChatRoom = this@FirebaseRealtimeDatabaseService.chatRoomSentWhenCreatingDoubleChatRoom

            if (ServerCode.SUCCESS.code == responseStatusCode && !chatRoom?.chatRoomId.isNullOrEmpty()) {
                if (chatRoom?.chatRoomType == ChatRoomType.Double.type && this@FirebaseRealtimeDatabaseService.messageSentWhenCreatingDoubleChatRoom != null) {
                    try {
                        pushMessageToChatRoom(
                            chatRoom,
                            this@FirebaseRealtimeDatabaseService.messageSentWhenCreatingDoubleChatRoom!!
                        )

                        // Send create new chatroom status to UI - ChatRoomViewModel
                        EventBus
                            .getDefault()
                            .postSticky(NewChatRoomEventBus(Result.Success(chatRoom)))
                    } catch (e: Exception) {
                        EventBus.getDefault().postSticky(NewChatRoomEventBus(Result.Error(e)))
                    }
                }
                if (chatRoom?.chatRoomType == ChatRoomType.Group.type && newChatRoom != null) {
                    try {
                        newChatRoom = newChatRoom.copy(chatRoomId = chatRoom.chatRoomId)
                        updateChatRoomNameAndImage(newChatRoom)
                        EventBus
                            .getDefault()
                            .postSticky(NewChatRoomEventBus(Result.Success(chatRoom)))
                    } catch (e: Exception) {
                        EventBus.getDefault().postSticky(NewChatRoomEventBus(Result.Error(e)))
                    }
                }
            } else {
                EventBus.getDefault()
                    .postSticky(NewChatRoomEventBus(Result.Error(ServerErrorException())))
            }
            this@FirebaseRealtimeDatabaseService.messageSentWhenCreatingDoubleChatRoom = null
            this@FirebaseRealtimeDatabaseService.chatRoomSentWhenCreatingDoubleChatRoom = null
            EventBus.getDefault().unregister(this@FirebaseRealtimeDatabaseService)
            EventBus.getDefault().removeStickyEvent(createNewChatRoomSocketEvenBus)
        }
    }

    private suspend fun uploadPhotoMessageToStorage(photo: String?): String? {
        return photo?.let {
            firebaseStorageService.putImageUriToStorage(
                imageUri = it.toUri(),
                storageRef = firebaseStorageService.chatroomMessageRef.child(
                    StringUtils.getCurrentTime().toString()
                )
            )
        }
    }

    /**
     * Delete all registered chatroom listeners
     */
    private fun clearChatRoomsListener() {
        registeredChatRoomsListeners.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        registeredChatRoomsListeners.clear()
    }

    private fun clearPublicUserDataListener() {
        registerPublicUserDataListener.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        registerPublicUserDataListener.clear()
    }

    private fun clearPrivateUserDataListener() {
        registerPrivateUserDataListener.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
        registerPrivateUserDataListener.clear()
    }

    /**
     * Delete everything(Listener, Data, List) of current user
     */
    fun removeAllListener() {
        clearPublicUserDataListener()
        clearPrivateUserDataListener()
        clearChatRoomsListener()
        _publicUserData.value = UserData()
        _privateUserData.value = null
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
        val userInfoSnapshot = publicUserDataRef.child(currentUid).get().await()
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
        publicUserDataRef.child(currentUid).setValue(updatedUserData).await()
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
                coroutineScope {
                    val deleteMessageTask = async {
                        chatRoomsRef.child(chatroomId)
                            .child(ChatRoom.MESSAGES)
                            .child(messageId)
                            .removeValue()
                            .await()
                    }
                    val deletePinnedMessageTask = async {
                        chatRoomsRef.child(chatroomId)
                            .child(ChatRoom.PINNED_MESSAGES)
                            .child(messageId)
                            .removeValue()
                            .await()
                    }
                    // Await both tasks
                    deleteMessageTask.await()
                    deletePinnedMessageTask.await()
                }
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
}