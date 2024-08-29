package com.android.kotlin.familymessagingapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
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

    fun shareImage(context: Context, drawable: Drawable) {
        val uriToImage = MediaUtils.createUriFromDrawable(context, drawable) ?: return
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            // Example: content://com.google.android.apps.photos.contentprovider/...
            putExtra(Intent.EXTRA_STREAM, uriToImage)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Cấp quyền đọc tạm thời
        }
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ hình ảnh qua"))
    }
}