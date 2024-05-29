package com.android.kotlin.familymessagingapp.repository

import com.android.kotlin.familymessagingapp.firebase_services.email_authentication.FirebaseEmailService
import com.android.kotlin.familymessagingapp.firebase_services.google_authentication.FirebaseGoogleService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

interface FirebaseAuthenticationRepository {
//    val currentUser: Flow<UserData?>
    val firebaseGoogleService: FirebaseGoogleService
    val firebaseEmailService: FirebaseEmailService
    fun hasUser(): Boolean
}

class FirebaseAuthenticationRepositoryImpl(
    override val firebaseGoogleService: FirebaseGoogleService,
    override val firebaseEmailService: FirebaseEmailService
) : FirebaseAuthenticationRepository {

//    override val currentUser: Flow<UserData?>
//        get() = callbackFlow {
//            val listener = FirebaseAuth.AuthStateListener { auth ->
//                this.trySend(auth.currentUser?.let { firebaseUser ->
//
//                })
//            }
//            Firebase.auth.addAuthStateListener(listener)
//            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
//        }

    override fun hasUser(): Boolean = Firebase.auth.currentUser != null
}