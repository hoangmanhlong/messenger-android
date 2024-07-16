package com.android.kotlin.familymessagingapp.services.firebase_services.email_authentication

import android.app.Application
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database.FirebaseRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.services.firebase_services.storage.FirebaseStorageService
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseEmailService @Inject constructor(
    private val application: Application,
    private val auth: FirebaseAuth,
    private val appDataStore: AppDataStore,
    private val firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService,
    private val firebaseStorageService: FirebaseStorageService
) {

    private val userAvatarImageRef = firebaseStorageService.userAvatarRef

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
                appDataStore.saveBoolean(AppDataStore.IS_AUTHENTICATE_BY_EMAIL, true)
                firebaseUser?.let {
                    val userdata = firebaseUser.run {
                        UserData(
                            uid = uid,
                            username = displayName,
                            userAvatar = photoUrl?.toString(),
                            email = this.email,
                            phoneNumber = phoneNumber
                        )
                    }
                    val userRef = firebaseRealtimeDatabaseService.userDataRef
                    val userInfoSnapshot = userRef.child(firebaseUser.uid).get().await()
                    if (userInfoSnapshot.value == null) {
                        userRef.child(firebaseUser.uid).setValue(userdata).await()
                        if (userdata.userAvatar != null) {
                            val downloadUrl = firebaseStorageService.createDownloadUrlFromImageUrl(
                                userdata.userAvatar,
                                userAvatarImageRef.child(firebaseUser.uid)
                            )

                            // Update user data with avatar URL
                            val updatedUserData = userdata.copy(userAvatar = downloadUrl)
                            userRef.child(firebaseUser.uid).setValue(updatedUserData).await()
                        } else {
                            userRef.child(it.uid).setValue(userdata).await()
                        }
                    }
                }
                appDataStore.saveBoolean(AppDataStore.IS_AUTHENTICATE_BY_EMAIL, true)
                Result.Success(true)
            } catch (e: Exception) {
//                if (e is FirebaseAuthUserCollisionException) {}
                Result.Error(e)
            }
        }
    }
}