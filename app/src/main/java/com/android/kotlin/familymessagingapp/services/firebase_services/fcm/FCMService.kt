package com.android.kotlin.familymessagingapp.services.firebase_services.fcm

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FCMService {

    suspend fun getFCMToken(): String? = withContext(Dispatchers.IO) {
        try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun subscribeToTopic(topicName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topicName).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun unSubscribeToTopic(topicName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}