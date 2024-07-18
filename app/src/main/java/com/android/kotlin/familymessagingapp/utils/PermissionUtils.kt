package com.android.kotlin.familymessagingapp.utils

import android.app.NotificationManager
import android.content.Context

object PermissionUtils {

    fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }
}