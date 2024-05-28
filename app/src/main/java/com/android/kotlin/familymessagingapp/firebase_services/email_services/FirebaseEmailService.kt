package com.android.kotlin.familymessagingapp.firebase_services.email_services

interface FirebaseEmailService {
    suspend fun signIn(email: String, password: String): Boolean
    suspend fun signUp(email: String, password: String): Boolean
    suspend fun signOut()
    suspend fun deleteAccount()
}