package com.android.kotlin.familymessagingapp.model

// This class includes the states from the firebase call.
sealed class FirebaseCallStatus {
    data object Calling : FirebaseCallStatus()
    data object Success : FirebaseCallStatus()
    data class Error(val exception: Exception?) : FirebaseCallStatus()
}