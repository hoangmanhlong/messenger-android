package com.android.kotlin.familymessagingapp.data.remote.client_retrofit

import com.android.kotlin.familymessagingapp.data.remote.dto.req.LoginReq
import com.android.kotlin.familymessagingapp.data.remote.dto.req.RegisterReq
import com.android.kotlin.familymessagingapp.data.remote.dto.res.ObjectResponse
import com.android.kotlin.familymessagingapp.model.Result
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApiService {

    @Headers("Content-Type: application/json", "Accept:application/json")
    @POST("/register")
    suspend fun registerAccount(@Body registerReq: RegisterReq): Result<ObjectResponse>

    @Headers("Content-Type: application/json", "Accept:application/json")
    @POST("/login")
    suspend fun login(@Body loginReq: LoginReq): Result<ObjectResponse>

    @Headers("Content-Type: application/json", "Accept:application/json")
    @GET("/auth/{id}")
    suspend fun getChatRoom(
        @Header("Authorization") userBearerToken: String,
        @Path("id") chatroomId: String
    ): Result<ObjectResponse>

    @Headers("Content-Type: application/json", "Accept:application/json")
    @GET("/auth/chatrooms/{userId}")
    suspend fun getChatRooms(
        @Header("Authorization") userBearerToken: String,
        @Path("userId") userId: String
    ): Result<ObjectResponse>

    @Headers("Content-Type: application/json", "Accept:application/json")
    @POST("/send_token")
    suspend fun sendFCMToken(
        @Header("Authorization") userBearerToken: String,
        @Body fcmBearerToken: String
    ): Result<ObjectResponse>
}