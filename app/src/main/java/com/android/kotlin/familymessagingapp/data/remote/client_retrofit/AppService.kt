package com.android.kotlin.familymessagingapp.data.remote.client_retrofit

import com.android.kotlin.familymessagingapp.data.remote.dto.res.ObjectResponse
import com.android.kotlin.familymessagingapp.data.remote.dto.req.LoginReq
import com.android.kotlin.familymessagingapp.data.remote.dto.req.RegisterReq
import com.android.kotlin.familymessagingapp.model.Result
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AppService {

    @POST("/register")
    suspend fun register(@Body registerReq: RegisterReq): Result<ObjectResponse>

    @POST("/login")
    suspend fun login(@Body loginReq: LoginReq): Result<ObjectResponse>

    @GET("/auth/{id}")
    suspend fun getChatRoom(
        @Header("Authorization") bearerToken: String,
        @Path("id") chatroomId: String
    ): Result<ObjectResponse>

    @GET("/auth/chatrooms/{userId}")
    suspend fun getChatRooms(
        @Header("Authorization") bearerToken: String,
        @Path("userId") userId: String
    ): Result<ObjectResponse>
}