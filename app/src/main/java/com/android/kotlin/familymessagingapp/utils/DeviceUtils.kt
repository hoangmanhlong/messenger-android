package com.android.kotlin.familymessagingapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.android.kotlin.familymessagingapp.BuildConfig
import java.util.Locale

object DeviceUtils {

    @RequiresApi(Build.VERSION_CODES.O)
    fun openNotificationPermissionSetting(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
        }
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
}