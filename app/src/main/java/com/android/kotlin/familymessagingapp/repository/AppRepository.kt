package com.android.kotlin.familymessagingapp.repository

import com.android.kotlin.familymessagingapp.data.remote.client_retrofit.AppApiService
import com.android.kotlin.familymessagingapp.data.remote.client_retrofit.AppService
import com.android.kotlin.familymessagingapp.data.remote.dto.req.LoginReq
import com.android.kotlin.familymessagingapp.data.remote.dto.req.RegisterReq
import com.android.kotlin.familymessagingapp.data.remote.dto.res.ObjectResponse
import com.android.kotlin.familymessagingapp.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(
    private val appService: AppService
) {
    suspend fun register(username: String, password: String): Result<ObjectResponse> {
        return withContext(Dispatchers.IO) {
           appService.register(RegisterReq(username, password))
        }
    }

    suspend fun login(username: String, password: String): Result<ObjectResponse> {
        return withContext(Dispatchers.IO) {
            appService.login(LoginReq(username, password))
        }
    }
}