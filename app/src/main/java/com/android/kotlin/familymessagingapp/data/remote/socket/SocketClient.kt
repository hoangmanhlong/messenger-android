package com.android.kotlin.familymessagingapp.data.remote.socket

import com.android.kotlin.familymessagingapp.BuildConfig
import com.android.kotlin.familymessagingapp.utils.Constant
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class OnlineStatus(val uid: String)

@Serializable
data class Verified(val uid: String, val verified: Boolean)

@Serializable
data class NewMessage(
    val chatRoomId: String,
    val messageId: String,
    val senderId: String
)

class SocketClient {

    companion object {
        const val onlineStatusSocketEvent = Constant.USER_ONLINE_STATUS_SOCKET_EVENT
        const val verifiedStatusSocketEvent = Constant.USER_VERIFIED_STATUS_SOCKET_EVENT
        const val NEW_MESSAGE_SOCKET_EVENT = Constant.NEW_MESSAGE_SOCKET_EVENT
    }

    private var socket: Socket? = null

    private val emitOnlineStatusSocketListener = Emitter.Listener {}

    private val emitVerifiedStatusSocketListener = Emitter.Listener {}

    private val pushNewMessageSocketListener = Emitter.Listener {}

    init {
        getSocket()
    }

    private fun getSocket(): Socket? {
        return try {
            socket ?: IO.socket(BuildConfig.serverUrl)
        } catch (e: Exception) {
            null
        }
    }

    fun connect() {
        getSocket()?.connect()
    }

    fun disconnect() {
        getSocket()?.disconnect()
    }

    fun addOnlineStatusSocketListener(uid: String) {
        socket?.on(onlineStatusSocketEvent, emitOnlineStatusSocketListener)
        emitStatus(onlineStatusSocketEvent, OnlineStatus(uid))
    }

    fun removeOnlineStatusSocketListener(event: String) {
        socket?.off(event, emitOnlineStatusSocketListener)
    }

    fun addVerifiedStatusSocketListener(event: String) {
        socket?.on(event, emitVerifiedStatusSocketListener)
    }

    fun removeVerifiedStatusSocketListener(event: String) {
        socket?.off(event, emitVerifiedStatusSocketListener)
    }

    fun emitOnlineStatus(uid: String) {
        emitStatus(onlineStatusSocketEvent, OnlineStatus(uid))
    }

    fun emitVerifiedStatus(uid: String, verified: Boolean) {
        emitStatus(verifiedStatusSocketEvent, Verified(uid, verified))
    }

    fun emitNewMessage(uid: String) {
//        emitStatus(NEW_MESSAGE_SOCKET_EVENT, NewMessage())
    }

    /**
     * Từ khóa inline chỉ ra rằng hàm này sẽ được inline hóa, nghĩa là mã của hàm sẽ được chèn trực
     * tiếp vào chỗ mà nó được gọi. Điều này giúp giảm overhead của việc gọi hàm và có thể tối ưu hóa hiệu suất.
     *
     * reified là từ khóa đặc biệt trong Kotlin, cho phép bạn sử dụng thông tin về kiểu dữ liệu T
     * tại thời điểm chạy. Điều này chỉ có thể được sử dụng trong các hàm inline.
     */
    private inline fun <reified T> emitStatus(event: String, data: T) {
        socket?.emit(event, Json.encodeToString(data))
    }
}