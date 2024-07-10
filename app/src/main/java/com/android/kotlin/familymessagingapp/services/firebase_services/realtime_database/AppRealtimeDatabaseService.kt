package com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.android.kotlin.familymessagingapp.services.firebase_services.storage.AppFirebaseStorage
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Bug crash
 * Description:
 * W  Listen at /users_information_data/ fp5VSXmPSyfIrjuWDT7Az1KfkZp2 failed: DatabaseError: This client does not have permission to perform this operation
 * W  Listen at /chatrooms/0 failed: DatabaseError: This client does not have permission to perform this operation
 *
 * Solution: remove all listener before logout
 */
class AppRealtimeDatabaseService(
    private val application: Application,
    private val auth: FirebaseAuth,
    private val appFirebaseStorage: AppFirebaseStorage
) {

    private val registeredChatRoomsListeners = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val registerUserDataListener = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val registerMessagesListener = mutableMapOf<DatabaseReference, ValueEventListener>()

    private val databaseReference = Firebase.database.reference

    companion object {
        const val TAG = "AppRealtimeDatabaseService"
    }

    val userDataRef = databaseReference.child(Constant.REALTIME_DATABASE_USER_REF_NAME)

    val chatroomsRef = databaseReference.child(Constant.REALTIME_DATABASE_CHAT_ROOM_REF)

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
                    this@callbackFlow.trySend(snapshot.getValue(UserData::class.java))
                }

                override fun onCancelled(error: DatabaseError) {
                    // When the listener is cancelled or an error occurs, the flow is closed
                    // immediately by calling close(). This will terminate the flow and stop any
                    // further emissions. The close function will also invoke the
                    // awaitClose { ... } block to remove the listener, but with an immediate
                    // closure of the flow.

                    // Pass exception to the flow
                    // 1. Error Propagation: Ensures the collector can handle and respond to the error.
                    // 2. Debugging: Provides context about why the flow was closed.
                    // 3. Error Handling: Allows for proper error handling in the flow's collector, improving the robustness and user experience of the application.
                    close(error.toException())
                }

            }
            currentUserRef.addValueEventListener(listener)
            registerUserDataListener[currentUserRef] = listener
            awaitClose { currentUserRef.removeEventListener(listener) }
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
                    val chatroomFlows: List<Flow<ChatRoom?>> = chatRooms.map { getChatRoomFlow(it) }
                    combine(chatroomFlows) {
                        // Sort chat room list by latestTime
                        it.filterNotNull().toList()
                            .sortedByDescending { chatroom -> chatroom.latestTime }
                    }
                }
            } else {
                registeredChatRoomsListeners.forEach {
                    chatroomsRef.removeEventListener(it.value)
                }
                flowOf(emptyList())
            }
        }

    /**
     * Đăng ký trình nghe tại phòng có id của user hiện tại
     * chỉ đăng ký 1 lần và huỷ khi Flow đóng
     * @param chatroomId chatroomID of userdata
     */
    private fun getChatRoomFlow(chatroomId: String): Flow<ChatRoom?> = callbackFlow {
        val chatroomRef = chatroomsRef.child(chatroomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var chatroom = snapshot.getValue(ChatRoom::class.java)
                val otherUserUid = chatroom?.members?.first { it != auth.uid }
                otherUserUid?.let {
                    userDataRef.child(otherUserUid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userdata = snapshot.getValue(UserData::class.java)
                                chatroom = chatroom?.copy(
                                    chatRoomImage = userdata?.userAvatar,
                                    chatroomName = userdata?.username
                                )
                                this@callbackFlow.trySend(chatroom)
                            }

                            override fun onCancelled(error: DatabaseError) {
//                                close(error.toException())
                                trySend(null).isSuccess
                            }
                        })
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

    suspend fun saveUserData(userData: UserData, imageUri: Uri?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var updatedUserData = userData
                if (imageUri != null) {
                    val downloadUrl = appFirebaseStorage.putUserAvatarUriToStorage(
                        imageUri,
                        appFirebaseStorage.userAvatarRef.child(userData.uid!!)
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

    fun addChatRoomMessageListener(chatroomId: String): Flow<List<Message>> = callbackFlow {
        val messageRef = chatroomsRef.child(chatroomId).child(ChatRoom.MESSAGES)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                snapshot.children.forEach {
                    it.getValue(Message::class.java)?.let { it1 -> messages.add(it1) }
                }
                this@callbackFlow.trySend(messages).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        messageRef.addValueEventListener(listener)
        registerMessagesListener[messageRef] = listener
        awaitClose { messageRef.removeEventListener(listener) }
    }

    fun removeMessageListener() {
        registerMessagesListener.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
        registerMessagesListener.clear()
    }

    /**
     * Problem: Với trường username
     * Chỉ tìm kiếm được được khi keyword giống 100% username
     * Ví dụ: keyword = "long", username trên server = "Long" => kết quả sẽ sai
     * Solution: thêm trường usernameLowercase vào UserData
     * usernameLowercase = username.toLoweCase
     */
    suspend fun search(keyword: String): List<UserData> {
        val list = mutableListOf<UserData>()
        return try {
            val dataSnapshot = if (StringUtils.isValidEmail(keyword)) {
                userDataRef.orderByChild(UserData.EMAIL).equalTo(keyword).get().await()
            } else if (StringUtils.isNumber(keyword)) {
                userDataRef.orderByChild(UserData.PHONE_NUMBER).equalTo(keyword).get().await()
            } else {
                userDataRef.orderByChild(UserData.USERNAME).equalTo(keyword).get().await()
            }

            if (dataSnapshot.exists()) {
                for (snapshot in dataSnapshot.children) {
                    snapshot.getValue(UserData::class.java)?.let {
                        if (it.uid != auth.uid) list.add(it)
                    }
                }
                list
            } else {
                list
            }
        } catch (e: Exception) {
            Log.d(TAG, "search: $e")
            list
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
    suspend fun updateNewMessage(chatRoom: ChatRoom, message: Message): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // If message has photo, upload to storage
                var photoUrl = message.photo
                if (photoUrl != null) {
                    photoUrl = appFirebaseStorage.putUserAvatarUriToStorage(
                        imageUri = photoUrl.toUri(),
                        storageRef = appFirebaseStorage.chatroomRef.child("${StringUtils.getCurrentTime()}")
                    )
                }
                val updatedMessage = message.copy(
                    messageId = "${StringUtils.getCurrentTime()}",
                    timestamp = StringUtils.getCurrentTime(),
                    photo = photoUrl,
                    text = message.text,
                    audio = message.audio,
                    fromId = auth.uid,
                    toId = chatRoom.members?.first { it != auth.uid },
                    video = message.video,
                    type = message.type,
                    status = message.status
                )
                if (chatRoom.chatRoomId == null) {
                    // Thêm uid của user hiện tại vào chatroom
                    val members = mutableListOf<String>()
                    chatRoom.members?.let { members.add(it.first()) }
                    members.add(auth.uid!!)
                    val finalChatRoom = ChatRoom(
                        chatRoomId = StringUtils.generateChatRoomId(members[0], members[1]),
                        members = members,
                        messages = listOf(updatedMessage),
                        latestTime = StringUtils.getCurrentTime(),
                        lastMessage = updatedMessage.text
                    )
                    chatroomsRef.child(finalChatRoom.chatRoomId!!)
                        .setValue(finalChatRoom)
                        .await()
                    members.forEach {
                        userDataRef.child(it).child(UserData.CHAT_ROOMS)
                    }
                } else {
                    val messagesList = chatRoom.messages.orEmpty().toMutableList().apply {
                        add(updatedMessage)
                    }
                    val chatRoomUpdates = mapOf(
                        ChatRoom.MESSAGES to messagesList,
                        ChatRoom.LAST_MESSAGE to message.text,
                        ChatRoom.LATEST_TIME to StringUtils.getCurrentTime()
                    )

                    chatroomsRef.child(chatRoom.chatRoomId)
                        .updateChildren(chatRoomUpdates)
                        .await()
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun removeAllListener() {
        registeredChatRoomsListeners.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
        registerUserDataListener.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
        registerUserDataListener.clear()
        registeredChatRoomsListeners.clear()
    }
}