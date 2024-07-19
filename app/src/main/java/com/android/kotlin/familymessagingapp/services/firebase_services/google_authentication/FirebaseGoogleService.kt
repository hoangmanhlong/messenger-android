package com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication

import android.content.Intent
import com.android.kotlin.familymessagingapp.BuildConfig
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.toUserData
import com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database.FirebaseRealtimeDatabaseService
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

class FirebaseGoogleService(
    private val auth: FirebaseAuth,
    private val oneTapClient: SignInClient,
    private val appDataStore: AppDataStore,
    private val firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService
) {

    companion object {
        val TAG: String = FirebaseGoogleService::class.java.simpleName
    }

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
    suspend fun signIn(): FindIntentSenderResult {
        return withContext(Dispatchers.IO) {
            try {
                val beginSignIn = oneTapClient.beginSignIn(buildSignInRequest()).await()
                FindIntentSenderResult.Success(beginSignIn.pendingIntent.intentSender)
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e
                if (e is ApiException) FindIntentSenderResult.NoAccountFound
                else FindIntentSenderResult.Error(e)
            }
        }
    }

    suspend fun signInWithIntent(intent: Intent): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(intent)
                val googleIdToken = credential.googleIdToken
                val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
                val firebaseUser = auth.signInWithCredential(googleCredentials).await().user
                firebaseUser?.let {
                    val userDataRef = firebaseRealtimeDatabaseService.userDataRef
                    val userInfoSnapshot = userDataRef.child(it.uid).get().await()
                    if (userInfoSnapshot.value == null) {
                        firebaseRealtimeDatabaseService.updateNewUserDataInRealtime(
                            firebaseUser.toUserData()
                        )
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

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.firebaseServerClientId)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}