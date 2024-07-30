package com.android.kotlin.familymessagingapp.data.remote.socket

import com.android.kotlin.familymessagingapp.BuildConfig
import com.android.kotlin.familymessagingapp.utils.Constant
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class SocketClient {

    companion object {
        const val onlineStatusSocketEvent = Constant.USER_ONLINE_STATUS_SOCKET_EVENT
    }

    private var socket: Socket? = null

    private val emitOnlineStatusSocketListener = Emitter.Listener {}

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

    fun addOnlineStatusSocketListener(event: String) {
        socket?.on(event, emitOnlineStatusSocketListener)
    }

    fun removeOnlineStatusSocketListener(event: String) {
        socket?.off(event, emitOnlineStatusSocketListener)
    }
}