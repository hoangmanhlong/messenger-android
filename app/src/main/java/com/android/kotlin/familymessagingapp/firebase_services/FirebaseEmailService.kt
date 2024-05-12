package com.android.kotlin.familymessagingapp.firebase_services

import androidx.lifecycle.LiveData
import com.android.kotlin.familymessagingapp.model.FirebaseCallStatus

interface FirebaseEmailService {
    //    val currentUser: Flow<UserData?>
    val currentUserid: String
    fun hasUser(): Boolean
    suspend fun signIn(email: String, password: String): Boolean
    suspend fun signUp(email: String, password: String): LiveData<FirebaseCallStatus>
    suspend fun signOut()
    suspend fun deleteAccount()
}