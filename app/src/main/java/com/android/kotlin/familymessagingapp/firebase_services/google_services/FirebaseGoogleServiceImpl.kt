package com.android.kotlin.familymessagingapp.firebase_services.google_services

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.model.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class FirebaseGoogleServiceImpl(
    private val auth: FirebaseAuth,
    private val context: Context,
    private val oneTapClient: SignInClient
) : FirebaseGoogleService {

    override suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(buildSignInRequest()).await()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    override suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        uid = uid,
                        username = displayName,
                        userAvatar = photoUrl?.toString(),
                        email = email,
                        phoneNumber = phoneNumber
                    )
                },
                errorMessage = null
            )
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    override suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
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