package com.android.kotlin.familymessagingapp.repository

import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.services.firebase_services.email_authentication.FirebaseEmailService
import com.android.kotlin.familymessagingapp.services.firebase_services.facebook.FacebookService
import com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication.FirebaseGoogleService
import com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database.FirebaseRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.services.firebase_services.storage.FirebaseStorageService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException


class FirebaseServiceRepository(
    private val auth: FirebaseAuth,
    val firebaseGoogleService: FirebaseGoogleService,
    val firebaseEmailService: FirebaseEmailService,
    val firebaseStorageService: FirebaseStorageService,
    val firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService,
    private val localDatabaseRepository: LocalDatabaseRepository,
    val facebookService: FacebookService
) {

    companion object {
        const val TAG = "FirebaseServiceRepository"
    }

    val authenticated: Flow<Boolean>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser != null)
            }
            Firebase.auth.addAuthStateListener(listener)
            awaitClose { Firebase.auth.removeAuthStateListener(listener) }
        }

    suspend fun deleteAccount(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val firebaseUser = auth.currentUser
                val uid = firebaseUser?.uid
                if (uid != null) {
                    firebaseStorageService.deleteUserData(uid)
                    firebaseRealtimeDatabaseService.deleteUserData(uid)
                    auth.currentUser?.run { delete().await() }
                    Result.Success(true)
                } else {
                    Result.Error(Exception("Uid Invalid"))
                }
            } catch (e: Exception) {
                // TODO: Delete Account Error with FirebaseAuthRecentLoginRequiredException
                // Current solution: sign out but the account still exists in the backend
                if (e is FirebaseAuthRecentLoginRequiredException) signOut()
                Result.Error(e)
            }
        }
    }

    /**
     * Before logging out, all necessary devices will be deleted such as realtime listener
     */
    fun signOut() {
        try {
            firebaseRealtimeDatabaseService.removeAllListener()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }
}