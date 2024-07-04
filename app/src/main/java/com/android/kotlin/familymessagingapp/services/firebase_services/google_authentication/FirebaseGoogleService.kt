package com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication

import android.content.Intent
import android.content.IntentSender
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.UserData

interface FirebaseGoogleService {
    suspend fun signIn(): IntentSender?
    suspend fun signInWithIntent(intent: Intent): Result<Boolean>
    suspend fun signOut()
    fun getSignedInUser(): UserData?
}