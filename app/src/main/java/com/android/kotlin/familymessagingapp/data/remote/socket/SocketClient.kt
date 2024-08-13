package com.android.kotlin.familymessagingapp.data.remote.socket

import com.android.kotlin.familymessagingapp.BuildConfig
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.ServerErrorException
import com.android.kotlin.familymessagingapp.model.toMessageSocketEvent
import com.android.kotlin.familymessagingapp.utils.Constant
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class BackendEventObject {

    @Serializable
    data class OnlineStatus(val uid: String) : BackendEventObject()

    @Serializable
    data class Verified(val uid: String, val verified: Boolean) : BackendEventObject()

    @Serializable
    data class ChatRoom(
        val chatRoomId: String,
        val chatRoomName: String?,
        val chatRoomImage: String?,
        val members: List<String>? = null,
        val newMessage: Message? = null,
        val chatRoomType: String? = null
    ) : BackendEventObject()

    @Serializable
    data class Message(
        val messageId: String? = null,
        val senderId: String? = null,
        val text: String? = null,
        val photo: String? = null,
        val video: String? = null,
        val audio: String? = null,
        val timestamp: String? = null
    ) : BackendEventObject()

    @Serializable
    data class NewMessageNotification(val chatRoom: ChatRoom) : BackendEventObject()
}

class SocketClient {

    companion object {
        val TAG: String = SocketClient::class.java.simpleName
        const val USER_ONLINE_STATUS_SOCKET_EVENT = Constant.USER_ONLINE_STATUS_SOCKET_EVENT
        const val USER_VERIFIED_STATUS_SOCKET_EVENT = Constant.USER_VERIFIED_STATUS_SOCKET_EVENT
        const val NEW_MESSAGE_SOCKET_EVENT = Constant.NEW_MESSAGE_SOCKET_EVENT
        const val NEW_CHATROOM_SOCKET_EVENT = Constant.NEW_CHATROOM_SOCKET_EVENT
    }

    private var socket: Socket? = null

    private val pushNewMessageSocketListener = Emitter.Listener {}

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
        emitStatus(USER_ONLINE_STATUS_SOCKET_EVENT, BackendEventObject.OnlineStatus(uid))
    }

    fun emitNewMessageToOtherUser(chatRoom: ChatRoom, message: Message) {
        val chatroom: BackendEventObject.ChatRoom = BackendEventObject.ChatRoom(
            chatRoomId = chatRoom.chatRoomId!!,
            chatRoomName = chatRoom.chatRoomImage,
            chatRoomImage = chatRoom.chatRoomImage,
            members = chatRoom.members!!,
            newMessage = message.toMessageSocketEvent(),
            chatRoomType = chatRoom.chatRoomType
        )
        emitStatus(NEW_MESSAGE_SOCKET_EVENT, chatroom)
    }

    fun emitNewChatRoom(chatRoom: ChatRoom): Result<Boolean> {
        return if (socket == null) {
            Result.failure(ServerErrorException())
        } else {
            val chatroomDto: BackendEventObject.ChatRoom = BackendEventObject.ChatRoom(
                chatRoomId = chatRoom.chatRoomId!!,
                chatRoomName = chatRoom.chatroomName,
                chatRoomImage = chatRoom.chatRoomImage,
                members = chatRoom.members!!,
                newMessage = null
            )
            emitStatus(NEW_CHATROOM_SOCKET_EVENT, chatroomDto)
            Result.success(true)
        }
    }

    /**
     * Từ khóa inline chỉ ra rằng hàm này sẽ được inline hóa, nghĩa là mã của hàm sẽ được chèn trực
     * tiếp vào chỗ mà nó được gọi. Điều này giúp giảm overhead của việc gọi hàm và có thể tối ưu hóa hiệu suất.
     *
     * reified là từ khóa đặc biệt trong Kotlin, cho phép bạn sử dụng thông tin về kiểu dữ liệu T
     * tại thời điểm chạy. Điều này chỉ có thể được sử dụng trong các hàm inline.
     */
    private inline fun <reified T : BackendEventObject> emitStatus(event: String, data: T) {
        socket?.emit(event, Json.encodeToString(data))
    }
}