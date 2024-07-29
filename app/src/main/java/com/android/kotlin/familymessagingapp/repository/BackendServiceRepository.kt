package com.android.kotlin.familymessagingapp.repository

import com.android.kotlin.familymessagingapp.data.remote.client_retrofit.BackendApiService
import com.android.kotlin.familymessagingapp.data.remote.dto.req.LoginReq
import com.android.kotlin.familymessagingapp.data.remote.dto.req.RegisterReq
import com.android.kotlin.familymessagingapp.data.remote.dto.res.ObjectResponse
import com.android.kotlin.familymessagingapp.data.remote.socket.SocketClient
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackendServiceRepository(
    private val backend: BackendApiService,
    private val socketClient: SocketClient
) {

    val socket = socketClient.getSocket()

    suspend fun register(username: String, password: String): Result<ObjectResponse> {
        return withContext(Dispatchers.IO) {
            backend.registerAccount(RegisterReq(username, password))
        }
    }

    suspend fun login(username: String, password: String): Result<ObjectResponse> {
        return withContext(Dispatchers.IO) {
            backend.login(LoginReq(username, password))
        }
    }

    suspend fun sendFCMToken(userToken: String, fcmToken: String): Result<ObjectResponse> {
        return withContext(Dispatchers.IO) {
            backend.sendFCMToken(
                StringUtils.generateBearerToken(userToken),
                StringUtils.generateBearerToken(fcmToken)
            )
        }
    }
}