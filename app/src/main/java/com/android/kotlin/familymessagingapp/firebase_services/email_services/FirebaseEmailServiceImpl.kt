package com.android.kotlin.familymessagingapp.firebase_services.email_services

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseEmailServiceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : FirebaseEmailService {

    override suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signUp(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signOut() = auth.signOut()

    override suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }
}