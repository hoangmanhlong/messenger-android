package com.android.kotlin.familymessagingapp.firebase_services.google_authentication

import android.content.Intent
import android.content.IntentSender
import com.android.kotlin.familymessagingapp.model.UserData

interface FirebaseGoogleService {
    suspend fun signIn(): IntentSender?
    suspend fun signInWithIntent(intent: Intent): SignInResult
    suspend fun signOut()
    fun getSignedInUser(): UserData?
}