package com.android.kotlin.familymessagingapp.services.firebase_services.email_authentication

import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.toUserData
import com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database.FirebaseRealtimeDatabaseService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException
import javax.inject.Inject

class FirebaseEmailService @Inject constructor(
    private val auth: FirebaseAuth,
    private val appDataStore: AppDataStore,
    private val firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService
) {

    suspend fun signIn(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                auth.signInWithEmailAndPassword(email, password).await().user
                appDataStore.saveBoolean(AppDataStore.IS_AUTHENTICATE_BY_EMAIL, true)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun signUp(email: String, password: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val firebaseUser = auth.createUserWithEmailAndPassword(email, password).await().user
                firebaseUser?.let {
                    firebaseRealtimeDatabaseService.updateNewUserDataInRealtime(firebaseUser.toUserData())
                }
                appDataStore.saveBoolean(AppDataStore.IS_AUTHENTICATE_BY_EMAIL, false)
                Result.Success(true)
            } catch (e: Exception) {
//                if (e is FirebaseAuthUserCollisionException) {}
                e.printStackTrace()
                if (e is CancellationException) throw e
                Result.Error(e)
            }
        }
    }
}