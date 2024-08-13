package com.android.kotlin.familymessagingapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.ValueInvalidException
import com.android.kotlin.familymessagingapp.services.firebase_services.email_authentication.FirebaseEmailService
import com.android.kotlin.familymessagingapp.services.firebase_services.facebook.FacebookService
import com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication.FirebaseGoogleService
import com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database.FirebaseRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.services.firebase_services.storage.FirebaseStorageService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException


class FirebaseServiceRepository(
    private val auth: FirebaseAuth,
    val firebaseGoogleService: FirebaseGoogleService,
    val firebaseEmailService: FirebaseEmailService,
    val firebaseStorageService: FirebaseStorageService,
    val firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService,
    val facebookService: FacebookService,
    private val backendServiceRepository: BackendServiceRepository
) {

    companion object {
        val TAG: String = FirebaseServiceRepository::class.java.simpleName
    }

    private var gotAuthenticationStateWhenStartingTheApp = false

    private val _authenticateState: MutableLiveData<Boolean?> = MutableLiveData(null)
    val authenticateState: LiveData<Boolean?> = _authenticateState

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _authenticateState.value = auth.currentUser != null
        if (!gotAuthenticationStateWhenStartingTheApp)  {
            if (_authenticateState.value == true) firebaseRealtimeDatabaseService.addUserDataListener()
            gotAuthenticationStateWhenStartingTheApp = true
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
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
                    Result.Error(ValueInvalidException())
                }
            } catch (e: Exception) {
                Log.d(TAG, "deleteAccount: $e")
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
            firebaseRealtimeDatabaseService.updateVerifiedStatus(false)
            backendServiceRepository.disconnectSocket()
            firebaseRealtimeDatabaseService.removeAllListener()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }
}