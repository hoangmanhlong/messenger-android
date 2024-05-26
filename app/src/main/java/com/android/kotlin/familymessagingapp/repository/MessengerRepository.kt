package com.android.kotlin.familymessagingapp.repository

import com.android.kotlin.familymessagingapp.firebase_services.email_services.FirebaseEmailService
import com.android.kotlin.familymessagingapp.firebase_services.google_services.FirebaseGoogleService
import com.android.kotlin.familymessagingapp.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface MessengerRepository {
//    val currentUser: Flow<UserData?>
    val firebaseGoogleService: FirebaseGoogleService
    val firebaseEmailService: FirebaseEmailService
    fun hasUser(): Boolean
}

class MessengerRepositoryImpl(
    override val firebaseGoogleService: FirebaseGoogleService,
    override val firebaseEmailService: FirebaseEmailService
) : MessengerRepository {

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