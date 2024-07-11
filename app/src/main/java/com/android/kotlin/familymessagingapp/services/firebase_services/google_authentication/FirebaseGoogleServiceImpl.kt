package com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.services.firebase_services.storage.AppFirebaseStorage
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.CancellationException

class FirebaseGoogleServiceImpl(
    private val auth: FirebaseAuth,
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val appDataStore: AppDataStore,
    private val appRealtimeDatabaseService: AppRealtimeDatabaseService,
    private val appFirebaseStorage: AppFirebaseStorage
) : FirebaseGoogleService {

    companion object {
        const val TAG = "FirebaseGoogleServiceImpl"
    }

    private val userAvatarImageRef = appFirebaseStorage.userAvatarRef

    /**
     * 13:26:12.649  W  com.google.android.gms.common.api.ApiException: 16: [28433] Cannot find a matching credential.
     * 13:26:12.649  W  	at com.google.android.gms.common.internal.ApiExceptionUtil.fromStatus(com.google.android.gms:play-services-base@@18.1.0:3)
     * 13:26:12.649  W  	at com.google.android.gms.common.api.internal.TaskUtil.setResultOrApiException(com.google.android.gms:play-services-base@@18.1.0:4)
     * 13:26:12.649  W  	at com.google.android.gms.internal.auth-api.zbau.zbb(com.google.android.gms:play-services-auth@@20.5.0:1)
     * 13:26:12.649  W  	at com.google.android.gms.internal.auth-api.zbx.zba(com.google.android.gms:play-services-auth@@20.5.0:4)
     * 13:26:12.649  W  	at com.google.android.gms.internal.auth-api.zbb.onTransact(com.google.android.gms:play-services-auth@@20.5.0:3)
     * 13:26:12.649  W  	at android.os.Binder.execTransactInternal(Binder.java:1285)
     * 13:26:12.649  W  	at android.os.Binder.execTransact(Binder.java:1244)
     */
    override suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(buildSignInRequest()).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    override suspend fun signInWithIntent(intent: Intent): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(intent)
                val googleIdToken = credential.googleIdToken
                val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
                val firebaseUser = auth.signInWithCredential(googleCredentials).await().user
                firebaseUser?.let {
                    val userData = firebaseUser.run {
                        UserData(
                            uid = uid,
                            username = displayName,
                            userAvatar = photoUrl?.toString(),
                            email = email,
                            phoneNumber = phoneNumber
                        )
                    }
                    val userDataRef = appRealtimeDatabaseService.userDataRef
                    val userInfoSnapshot = userDataRef.child(it.uid).get().await()
                    if (userInfoSnapshot.value == null) {
                        // User does not exist, update user data
                        if (userData.userAvatar != null) {
                            val downloadUrl = appFirebaseStorage.createDownloadUrlFromImageUrl(
                                userData.userAvatar,
                                userAvatarImageRef.child(firebaseUser.uid)
                            )

                            // Update user data with avatar URL
                            val updatedUserData = userData.copy(userAvatar = downloadUrl)
                            userDataRef.child(firebaseUser.uid).setValue(updatedUserData).await()
                        } else {
                            userDataRef.child(it.uid).setValue(userData).await()
                        }
                    }
                }
                appDataStore.saveBoolean(AppDataStore.IS_AUTHENTICATE_BY_EMAIL, false)
                Result.Success(true)
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e
                Result.Error(e)
            }
        }
    }

    override suspend fun signOut() {
        try {
//            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    override fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            uid = uid,
            username = displayName,
            userAvatar = photoUrl?.toString(),
            email = email,
            phoneNumber = phoneNumber
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}