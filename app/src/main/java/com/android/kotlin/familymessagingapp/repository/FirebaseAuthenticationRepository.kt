package com.android.kotlin.familymessagingapp.repository

import androidx.lifecycle.asLiveData
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.firebase_services.email_authentication.FirebaseEmailService
import com.android.kotlin.familymessagingapp.firebase_services.google_authentication.FirebaseGoogleService
import com.android.kotlin.familymessagingapp.firebase_services.realtime_database.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.firebase_services.storage.AppFirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class FirebaseAuthenticationRepository(
    private val auth: FirebaseAuth,
    val firebaseGoogleService: FirebaseGoogleService,
    val firebaseEmailService: FirebaseEmailService,
    val appFirebaseStorage: AppFirebaseStorage,
    val appRealtimeDatabaseService: AppRealtimeDatabaseService,
    private val dataMemoryRepository: DataMemoryRepository
) {
    val authenticated: Flow<Boolean>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                this@callbackFlow.trySend(auth.currentUser != null)
            }
            Firebase.auth.addAuthStateListener(listener)
            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
        }

    suspend fun deleteAccount(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val firebaseUser = auth.currentUser
                val uid = firebaseUser?.uid
                if (uid != null) {
                    appFirebaseStorage.deleteUserData(uid)
                    appRealtimeDatabaseService.deleteUserData(uid)
                    if (dataMemoryRepository
                            .appDataStore
                            .getBooleanPreferenceFlow(AppDataStore.IS_AUTHENTICATE_BY_EMAIL, true)
                            .first() == true
                    ) {
                        firebaseEmailService.signOut()
                    } else {
                        firebaseGoogleService.signOut()
                    }
                    firebaseUser.delete().await()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}