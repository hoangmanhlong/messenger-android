package com.android.kotlin.familymessagingapp.data.remote.dto.res

import com.android.kotlin.familymessagingapp.model.UserData

data class LoginRes(
    val token: String?,
    val userData: UserData?
)