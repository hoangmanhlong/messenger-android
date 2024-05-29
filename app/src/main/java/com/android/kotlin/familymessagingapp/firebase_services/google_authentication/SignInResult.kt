package com.android.kotlin.familymessagingapp.firebase_services.google_authentication

import com.android.kotlin.familymessagingapp.model.UserData

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)