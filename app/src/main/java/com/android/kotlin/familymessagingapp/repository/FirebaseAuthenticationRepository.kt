package com.android.kotlin.familymessagingapp.repository

import com.android.kotlin.familymessagingapp.firebase_services.email_authentication.FirebaseEmailService
import com.android.kotlin.familymessagingapp.firebase_services.google_authentication.FirebaseGoogleService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class FirebaseAuthenticationRepository(
    val firebaseGoogleService: FirebaseGoogleService,
    val firebaseEmailService: FirebaseEmailService
) {
    val authenticated: Flow<Boolean>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                this@callbackFlow.trySend(auth.currentUser != null)
            }
            Firebase.auth.addAuthStateListener(listener)
            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
        }

    fun getUserUID(): FirebaseUser? = Firebase.auth.currentUser
}