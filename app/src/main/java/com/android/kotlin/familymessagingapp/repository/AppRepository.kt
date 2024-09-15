package com.android.kotlin.familymessagingapp.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.model.NotificationType
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.DeviceUtils
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.PermissionUtils
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.android.kotlin.familymessagingapp.utils.makeStatusNotification

class AppRepository(private val application: Application) {
    fun composeFeedback(context: Context) {
        val emailAddress = arrayOf(application.getString(R.string.feedback_mail))
        DeviceUtils.composeEmail(context, emailAddress, null)
    }

    fun openSystemSetting(context: Context) = DeviceUtils.openApplicationInfo(context)

    fun isCameraPermissionGranted() =
        PermissionUtils.permissionsGranted(application, android.Manifest.permission.CAMERA)

    fun getCurrentNotificationStatus(): Boolean = PermissionUtils.areNotificationsEnabled(application)

    fun convertImageUriToBitmap(uri: Uri) = MediaUtils.uriToBitmap(application, uri)

    suspend fun readQrCodeFromBitmap(bitmap: Bitmap) = MediaUtils.readQrCodeFromBitmap(bitmap)

    fun shareQrCode(context: Context, qrView: ViewGroup, userName: String?) {
        val bitmapFromViewGroup = MediaUtils.getBitmapFromView(qrView) ?: return
        val formattedSharedQRCode = StringUtils.formatMyQRCodeImageName(userName)
        DeviceUtils.shareImage(context, bitmapFromViewGroup, formattedSharedQRCode)
    }

    fun shareBitmap(context: Context, bitmap: Bitmap) {
        DeviceUtils.shareImage(context, bitmap, Constant.SHARED_IMAGE_NAME)
    }

    fun saveQrCode(qrView: ViewGroup, userName: String?) {
        val bitmapFromViewGroup = MediaUtils.getBitmapFromView(qrView) ?: return
        val displayName = StringUtils.formatMyQRCodeImageName(userName)
        val downloadedImageUri = MediaUtils.saveBitmapToGallery(application, bitmapFromViewGroup, displayName)
        makeStatusNotification(
            context = application,
            data = downloadedImageUri,
            notificationType = if(downloadedImageUri == null) NotificationType.DOWNLOAD.ERROR else NotificationType.DOWNLOAD.SUCCESS
        )
    }
}