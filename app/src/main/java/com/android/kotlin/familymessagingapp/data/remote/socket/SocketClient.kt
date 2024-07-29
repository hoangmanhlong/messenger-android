package com.android.kotlin.familymessagingapp.data.remote.socket

import com.android.kotlin.familymessagingapp.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket

class SocketClient {
    fun getSocket(): Socket? {
        return try {
            IO.socket(BuildConfig.serverUrl)
        } catch (e: Exception) {
            null
        }
    }
}