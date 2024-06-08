package com.android.kotlin.familymessagingapp.data.remote.dto.req

import com.google.gson.annotations.SerializedName

data class LoginReq(

    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)