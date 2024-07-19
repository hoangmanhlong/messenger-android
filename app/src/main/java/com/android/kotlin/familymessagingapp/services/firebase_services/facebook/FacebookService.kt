package com.android.kotlin.familymessagingapp.services.firebase_services.facebook

import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FacebookService(private val auth: FirebaseAuth) {
    suspend fun handleFacebookAccessToken(token: AccessToken): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val credential = FacebookAuthProvider.getCredential(token.token)
                auth.signInWithCredential(credential).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}