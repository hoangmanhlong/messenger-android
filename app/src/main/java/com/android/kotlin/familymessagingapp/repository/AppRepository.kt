package com.android.kotlin.familymessagingapp.repository

import com.android.kotlin.familymessagingapp.data.remote.client_retrofit.AppApi
import com.android.kotlin.familymessagingapp.data.remote.dto.req.LoginReq
import com.android.kotlin.familymessagingapp.data.remote.dto.req.RegisterReq
import com.android.kotlin.familymessagingapp.data.remote.dto.res.ObjectResponse
import com.android.kotlin.familymessagingapp.data.remote.dto.res.RegisterRes
import com.android.kotlin.familymessagingapp.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(
    private val api: AppApi
) {
    suspend fun register(username: String, password: String): Result<ObjectResponse> {
        return withContext(Dispatchers.IO) {
           api.register(RegisterReq(username, password))
        }
    }

    suspend fun login(username: String, password: String): Result<ObjectResponse> {
        return withContext(Dispatchers.IO) {
            api.login(LoginReq(username, password))
        }
    }
}