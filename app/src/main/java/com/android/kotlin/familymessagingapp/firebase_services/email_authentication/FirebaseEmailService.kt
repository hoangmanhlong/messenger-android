package com.android.kotlin.familymessagingapp.firebase_services.email_authentication

import com.android.kotlin.familymessagingapp.model.Result

interface FirebaseEmailService {
    suspend fun signIn(email: String, password: String): Boolean
    suspend fun signUp(email: String, password: String): Result<Boolean>
    suspend fun signOut()
    suspend fun deleteAccount()
}