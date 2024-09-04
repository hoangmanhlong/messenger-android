package com.android.kotlin.familymessagingapp.services.firebase_services.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.work.SaveFCMTokenToLocalAndSendToServerWorker
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.DeviceUtils
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class AppFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var appDataStore: AppDataStore

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        if (!DeviceUtils.isAppInBackground(applicationContext)) return

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val jsonPayload = JSONObject(remoteMessage.data as Map<*, *>)
            // Lấy các giá trị từ JSON
            val photo = jsonPayload.optString("photo", "")
            val senderId = jsonPayload.optString("senderId", "")
            val text = jsonPayload.optString("text", "")
            val chatRoomId = jsonPayload.optString(ChatRoom.CHAT_ROOM_ID, "")
            val senderName = jsonPayload.optString("senderName", "")
            val chatRoomName = jsonPayload.optString(ChatRoom.CHAT_ROOM_NAME, "")
            val chatRoomType = jsonPayload.optString(ChatRoom.CHAT_ROOM_TYPE, "")

            CoroutineScope(Dispatchers.IO).launch {
                appDataStore.saveString(
                    AppDataStore.CHAT_ROOM_ID_FROM_NOTIFICATION,
                    chatRoomId
                )
            }

            sendNotification(chatRoomName, senderName, chatRoomType, text, photo)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }
    // [END on_new_token]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(
        chatRoomName: String,
        senderName: String,
        chatRoomType: String,
        text: String,
        photo: String,
    ) {
        val requestCode = System.currentTimeMillis().toInt()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        if (text.isNullOrEmpty() && photo.isNullOrEmpty()) return

        val formatContentMessage = if (chatRoomType == ChatRoomType.Group.type) {
            senderName + ": " + text.ifEmpty { applicationContext.getString(R.string.photo_last_message) }
        } else {
            text.ifEmpty { applicationContext.getString(R.string.photo_last_message) }
        }

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_earth_notification)
            .setContentTitle(chatRoomName)
            .setContentText(formatContentMessage)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}