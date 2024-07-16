package com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication

import android.content.IntentSender

sealed class FindIntentSenderResult {
    data class Success(val intentSender: IntentSender) : FindIntentSenderResult()
    object NoAccountFound : FindIntentSenderResult()
    data class Error(val exception: Exception) : FindIntentSenderResult()
}