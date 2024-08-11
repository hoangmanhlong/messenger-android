package com.android.kotlin.familymessagingapp.receiver

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles the "Reply" action in the chat notification.
 */
@AndroidEntryPoint
class ReplyReceiver : BroadcastReceiver() {

    companion object {
        const val KEY_TEXT_REPLY = "reply"
    }

    @Inject
    lateinit var firebaseServiceRepository: FirebaseServiceRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        GlobalScope.launch(Dispatchers.IO) {
            val results = RemoteInput.getResultsFromIntent(intent) ?: return@launch
            // The message typed in the notification reply.
            val input = results.getCharSequence(KEY_TEXT_REPLY)?.toString()
            val uri = intent.data ?: return@launch
            val chatRoomId = uri.lastPathSegment ?: return@launch

            if (chatRoomId.isNotEmpty() && !input.isNullOrBlank()) {
                val message = Message(text = input)
                firebaseServiceRepository.firebaseRealtimeDatabaseService
                    .updateNewMessage(
                        chatRoom = ChatRoom(),
                        message = message
                    )
                // We should update the notification so that the user can see that the reply has been
                // sent.
//                repository.updateNotification(chatRoomId)
            }
        }
    }
}