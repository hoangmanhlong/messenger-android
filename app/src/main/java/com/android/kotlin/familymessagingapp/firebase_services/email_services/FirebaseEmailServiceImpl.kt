package com.android.kotlin.familymessagingapp.firebase_services.email_services

import androidx.lifecycle.LiveData
import com.android.kotlin.familymessagingapp.model.FirebaseCallStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseEmailServiceImpl : FirebaseEmailService {

    private fun hasUser(): Boolean = Firebase.auth.currentUser != null

    override suspend fun signIn(email: String, password: String): Boolean {
        return try {
            if (hasUser()) return false
            Firebase.auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signUp(email: String, password: String): LiveData<FirebaseCallStatus> {
        TODO("Not yet implemented")
    }

//    override suspend fun signUp(email: String, password: String): LiveData<FirebaseCallStatus> {
//        val status = MutableLiveData(FirebaseCallStatus.LOADING)
//        try {
//            Firebase.auth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val user = Firebase.auth.currentUser
//                        user?.let {
//                            constant.userdataRef.child(currentUserid)
//                                .setValue(
//                                    UserData(
//                                        uid = currentUserid,
//                                        email = email,
//                                        username = null,
//                                        phoneNumber = null,
//                                        userAvatar = null
//                                    )
//                                )
//                                .addOnCompleteListener { saveUserDataResult ->
//                                    if (saveUserDataResult.isSuccessful)
//                                        status.value = FirebaseCallStatus.SUCCESS
//                                    else
//                                        status.value = FirebaseCallStatus.ERROR
//
//                                }
//                        }
//                    } else {
//                        status.value = FirebaseCallStatus.ERROR
//                    }
//                }
//            Result.success(Unit)
//        } catch (e: Exception) {
//            status.value = FirebaseCallStatus.ERROR
//        }
//        return FirebaseCallStatus.Success
//    }

    override suspend fun signOut() = Firebase.auth.signOut()

    override suspend fun deleteAccount() {
        Firebase.auth.currentUser!!.delete().await()
    }
}