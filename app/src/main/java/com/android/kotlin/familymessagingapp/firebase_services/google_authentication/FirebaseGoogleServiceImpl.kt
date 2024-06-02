package com.android.kotlin.familymessagingapp.firebase_services.google_authentication

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.firebase_services.realtime.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.utils.Constant
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class FirebaseGoogleServiceImpl(
    private val auth: FirebaseAuth,
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val appDataStore: AppDataStore,
    databaseReference: DatabaseReference,
    storageReference: StorageReference
) : FirebaseGoogleService {

    private val userDataRef = databaseReference.child(Constant.REALTIME_DATABASE_USER_REF_NAME)

    companion object {
        const val TAG = "FirebaseGoogleServiceImpl"
    }

    private val userAvatarImageRef =
        storageReference.child(Constant.FIREBASE_STORAGE_USER_AVATAR_IMAGE_REF_NAME)

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

    override suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        var userData: UserData? = null
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            userData = user?.run {
                UserData(
                    uid = uid,
                    username = displayName,
                    userAvatar = photoUrl?.toString(),
                    email = email,
                    phoneNumber = phoneNumber
                )
            }

            user?.let {
                val userInfoSnapshot = userDataRef.child(it.uid).get().await()
                if (userInfoSnapshot.value == null) {
//                    // User does not exist, update user data
//                    if (userData?.userAvatar != null) {
//                        // Upload user avatar to Firebase Storage
//                        val avatarUri = Uri.parse(userData.userAvatar)
//                        val storageRef = userAvatarImageRef.child(it.uid)
//                        storageRef.putFile(avatarUri).await()
//                        val downloadUrl = storageRef.downloadUrl.await().toString()
//
//                        // Update user data with avatar URL
//                        val updatedUserData = userData.copy(userAvatar = downloadUrl)
//                        userDataRef.child(it.uid).setValue(updatedUserData).await()
//                    } else {
                    userDataRef.child(it.uid).setValue(userData).await()
                }
            }
            appDataStore.saveBoolean(AppDataStore.IS_AUTHENTICATE_BY_EMAIL, false)
            SignInResult(
                data = userData,
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
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