package com.android.kotlin.familymessagingapp.firebase_services.realtime_database

import android.app.Application
import android.net.Uri
import com.android.kotlin.familymessagingapp.firebase_services.storage.AppFirebaseStorage
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class AppRealtimeDatabaseService(
    private val application: Application,
    private val auth: FirebaseAuth,
    private val appFirebaseStorage: AppFirebaseStorage
) {

    private val databaseReference = Firebase.database.reference

    companion object {
        const val TAG = "AppRealtimeDatabaseService"
    }

    val userDataRef = databaseReference.child(Constant.REALTIME_DATABASE_USER_REF_NAME)

    val currentUserDataFlow: Flow<UserData?>
        get() = callbackFlow {
            auth.currentUser?.let {
                val currentUserRef: DatabaseReference = userDataRef.child(it.uid)
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        this@callbackFlow.trySend(snapshot.getValue(UserData::class.java))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        currentUserRef.removeEventListener(this)

                    }

                }
                currentUserRef.addValueEventListener(listener)
                awaitClose { currentUserRef.removeEventListener(listener) }
            } ?: close()
        }

    suspend fun saveUserData(userData: UserData, imageUri: Uri?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (imageUri != null) {
                    val downloadUrl = appFirebaseStorage.putUserAvatarUriToStorage(
                        application,
                        imageUri,
                        appFirebaseStorage.userAvatarRef
                    )
                    downloadUrl?.let { userData.copy(userAvatar = downloadUrl) }
                }
                userDataRef.child(userData.uid!!).setValue(userData)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}