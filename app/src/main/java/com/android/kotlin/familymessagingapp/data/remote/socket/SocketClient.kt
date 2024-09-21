package com.android.kotlin.familymessagingapp.data.remote.socket

import com.android.kotlin.familymessagingapp.BuildConfig
import com.android.kotlin.familymessagingapp.data.remote.ServerCode
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.MediaData
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.ServerErrorException
import com.android.kotlin.familymessagingapp.model.toMessageSocketEvent
import com.android.kotlin.familymessagingapp.utils.Constant
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

@Serializable
sealed class BackendEvent {

    @Serializable
    data class OnlineStatus(val uid: String) : BackendEvent()

    @Serializable
    data class Verified(val uid: String, val verified: Boolean) : BackendEvent()

    @Serializable
    data class ChatRoomRequest(
        val chatRoomId: String? = null,
        val chatRoomName: String? = null,
        val chatRoomImage: String? = null,
        val members: List<String>? = null,
        val newMessage: Message? = null,
        val chatRoomType: String? = null
    ) : BackendEvent()

    @Serializable
    data class Message(
        val messageId: String? = null,
        val senderId: String? = null,
        val text: String? = null,
        val medias: List<MediaData>? = null,
        val timestamp: String? = null,
        val senderName: String? = null,
    ) : BackendEvent()

    @Serializable
    data class NewMessageNotification(val chatRoomRequest: ChatRoomRequest) : BackendEvent()

    @Serializable
    data class CreateNewChatRoomResponse(
        val chatRoom: ChatRoomRequest? = null,
        val responseStatusCode: Int? = null
    ) : BackendEvent()

    @Serializable
    data class Response(val responseStatusCode: Int) : BackendEvent()

    @Serializable
    data class NewChatRoomMemberRequest(
        val chatRoomId: String,
        val members: List<String>
    ) : BackendEvent()
}

class SocketClient {

    companion object {
        val TAG: String = SocketClient::class.java.simpleName
        const val USER_ONLINE_STATUS_SOCKET_EVENT = Constant.USER_ONLINE_STATUS_SOCKET_EVENT
        const val USER_VERIFIED_STATUS_SOCKET_EVENT = Constant.USER_VERIFIED_STATUS_SOCKET_EVENT
        const val NEW_MESSAGE_SOCKET_EVENT = Constant.NEW_MESSAGE_SOCKET_EVENT
        const val NEW_CHATROOM_SOCKET_EVENT = Constant.NEW_CHATROOM_SOCKET_EVENT
        const val NEW_CHATROOM_MEMBERS_SOCKET_EVENT = Constant.NEW_CHATROOM_MEMBERS_SOCKET_EVENT
    }

    private var socket: Socket? = null

    private val pushNewMessageSocketListener = Emitter.Listener {}

    /**
     * Chat room is being created and sent to server
     */
    private var chatroom: ChatRoom? = null

    private val createNewChatRoomSocketEventListener = Emitter.Listener { args ->
        // Kiểm tra nếu args.getOrNull(0) là một JSONObject và chuyển đổi nó thành String
        val data = (args.getOrNull(0) as? JSONObject)?.toString()
        if (data == null) {
            chatroom = null
            socket?.off(NEW_CHATROOM_SOCKET_EVENT)
            return@Listener
        }

        val createNewChatRoomResponse =
            Json.decodeFromString<BackendEvent.CreateNewChatRoomResponse>(data)
        chatroom =
            chatroom?.copy(chatRoomId = createNewChatRoomResponse.chatRoom?.chatRoomId)
        EventBus.getDefault().postSticky(
            CreateNewChatRoomSocketEvenBus(
                chatRoom = chatroom,
                createNewChatRoomResponse.responseStatusCode,
            )
        )
        chatroom = null
        socket?.off(NEW_CHATROOM_SOCKET_EVENT)
    }

    private val addNewChatRoomMemberSocketEventListener = Emitter.Listener {args ->
        // Kiểm tra nếu args.getOrNull(0) là một JSONObject và chuyển đổi nó thành String
        var responseStatusCode = ServerCode.ERROR.code
        val data = (args.getOrNull(0) as? JSONObject)?.toString()
        if (data != null) {
            val response = Json.decodeFromString<BackendEvent.Response>(data)
            responseStatusCode = response.responseStatusCode
        }
        EventBus.getDefault().postSticky(AddNewChatRoomMembersSocketEventbus(responseStatusCode))
        socket?.off(NEW_CHATROOM_MEMBERS_SOCKET_EVENT)
    }

    init {
        socket = getSocket()
    }

    private fun getSocket(): Socket? {
        return try {
            IO.socket(BuildConfig.serverUrl)
        } catch (e: Exception) {
            null
        }
    }

    fun connect() = socket?.connect()

    fun disconnect() = socket?.disconnect()

    fun addOnlineStatusSocketListener(uid: String) {
        emitStatus(USER_ONLINE_STATUS_SOCKET_EVENT, BackendEvent.OnlineStatus(uid))
    }

    fun emitNewMessageToOtherUser(chatRoom: ChatRoom, message: Message, senderName: String?) {
        val chatroom: BackendEvent.ChatRoomRequest = BackendEvent.ChatRoomRequest(
            chatRoomId = chatRoom.chatRoomId!!,
            chatRoomName = chatRoom.chatRoomName,
            members = chatRoom.members!!,
            newMessage = message.toMessageSocketEvent().copy(senderName = senderName),
            chatRoomType = chatRoom.chatRoomType
        )
        emitStatus(NEW_MESSAGE_SOCKET_EVENT, chatroom)
    }

    /**
     * Send a event to server to create a new chat room.
     *
     * @param chatRoom: ChatRoom contain members and chatroom type
     * @return Result<Boolean> return true if success, false otherwise
     */
    fun emitNewChatRoom(chatRoom: ChatRoom): Result<Boolean> {
        return if (socket == null || socket?.connected() == false) {
            Result.Error(ServerErrorException())
        } else {
            // Just send members (chatroom members) and chatRoomType (chatroom type) for the server
            // to create a group on the database. After receiving the response, the app will update
            // the remaining fields.
            val chatroomDto: BackendEvent.ChatRoomRequest = BackendEvent.ChatRoomRequest(
                members = chatRoom.members!!,
                chatRoomType = chatRoom.chatRoomType
            )
            this.chatroom = chatRoom
            socket?.on(NEW_CHATROOM_SOCKET_EVENT, createNewChatRoomSocketEventListener)
            emitStatus(NEW_CHATROOM_SOCKET_EVENT, chatroomDto)
            Result.Success(true)
        }
    }

    fun emitNewChatRoomMembers(chatRoomId: String, members: List<String>) {
        if (socket == null || socket?.connected() == false) {
            EventBus.getDefault().postSticky(AddNewChatRoomMembersSocketEventbus(ServerCode.ERROR.code))
        } else {
            val newChatRoomMemberRequest = BackendEvent.NewChatRoomMemberRequest(
                chatRoomId = chatRoomId,
                members = members
            )
            socket?.on(NEW_CHATROOM_MEMBERS_SOCKET_EVENT, addNewChatRoomMemberSocketEventListener)
            emitStatus(NEW_CHATROOM_MEMBERS_SOCKET_EVENT, newChatRoomMemberRequest)
        }
    }

    /**
     * Từ khóa inline chỉ ra rằng hàm này sẽ được inline hóa, nghĩa là mã của hàm sẽ được chèn trực
     * tiếp vào chỗ mà nó được gọi. Điều này giúp giảm overhead của việc gọi hàm và có thể tối ưu hóa hiệu suất.
     *
     * reified là từ khóa đặc biệt trong Kotlin, cho phép bạn sử dụng thông tin về kiểu dữ liệu T
     * tại thời điểm chạy. Điều này chỉ có thể được sử dụng trong các hàm inline.
     */
    private inline fun <reified T : BackendEvent> emitStatus(event: String, data: T) {
        socket?.emit(event, Json.encodeToString(data))
    }
}