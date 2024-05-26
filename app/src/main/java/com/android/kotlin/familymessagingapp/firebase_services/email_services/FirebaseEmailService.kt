package com.android.kotlin.familymessagingapp.firebase_services.email_services

import androidx.lifecycle.LiveData
import com.android.kotlin.familymessagingapp.model.FirebaseCallStatus

interface FirebaseEmailService {
    suspend fun signIn(email: String, password: String): Boolean
    suspend fun signUp(email: String, password: String): LiveData<FirebaseCallStatus>
    suspend fun signOut()
    suspend fun deleteAccount()
}