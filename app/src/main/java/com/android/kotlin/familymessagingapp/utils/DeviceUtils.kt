package com.android.kotlin.familymessagingapp.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import com.android.kotlin.familymessagingapp.BuildConfig
import com.android.kotlin.familymessagingapp.R
import java.util.Locale

object DeviceUtils {

    fun openNotificationPermissionSetting(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
        }
        context.startActivity(intent)
    }

    fun openApplicationInfo(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun composeEmail(context: Context, addresses: Array<String>, subject: String?) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps handle this.
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        intent.resolveActivity(context.packageManager).let { context.startActivity(intent) }
    }

    fun getCurrentLanguage(context: Context): String {
        val currentLocale: Locale = context.resources.configuration.locales[0]
        return currentLocale.language
    }

    fun shareImage(context: Context, bitmap: Bitmap, imageName: String) {
        val uriToImage = MediaUtils.createUriFromDrawable(context, bitmap, imageName) ?: return
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uriToImage)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant temporary read permissions
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_image_with)))
    }

    fun isAppInBackground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return true

        for (appProcess in appProcesses) {
            if (appProcess.processName == BuildConfig.APPLICATION_ID) {
                return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
        return true
    }

}