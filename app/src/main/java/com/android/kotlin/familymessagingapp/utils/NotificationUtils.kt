package com.android.kotlin.familymessagingapp.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.model.NotificationType

/**
 * Create a Notification that is shown as a heads-up notification if possible.
 *
 * For this codelab, this is used to show a notification so that you know when different steps
 * of the background work chain are starting
 *
 * @param data Message shown on the notification
 * @param context Context needed to create progress data
 * @param notificationType Type of notification
 */
@SuppressLint("IntentReset")
fun <T> makeStatusNotification(context: Context, data: T?, notificationType: NotificationType) {

    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = Constant.VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = Constant.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(Constant.CHANNEL_ID, name, importance)
        channel.description = description

        // Add the channel
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)


    }

    var pendingIntent: PendingIntent? = null

    var notificationContentText = ""

    var notificationContentTitle = ""

    if (notificationType is NotificationType.DOWNLOAD) {
        if (data != null) {
            val data = data as Uri
            // Create an intent to open the file
            val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
                this.data = data
                type = context.contentResolver.getType(data) ?: "*/*"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openFileIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        notificationContentText = context.getString(notificationType.name)
        notificationContentTitle = context.getString(NotificationType.DOWNLOAD.DEFAULT.name)
    }

    // Create the notification
    val builder = NotificationCompat.Builder(context, Constant.CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_earth_notification)
        .setContentTitle(notificationContentTitle)
        .setContentText(notificationContentText)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setVibrate(LongArray(0))
        .setContentIntent(pendingIntent)

    // Show the notification
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }
    NotificationManagerCompat.from(context).notify(Constant.NOTIFICATION_ID, builder.build())
}