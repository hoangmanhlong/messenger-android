package com.android.kotlin.familymessagingapp.data

import android.app.Application
import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import com.android.kotlin.familymessagingapp.model.MediaData
import com.android.kotlin.familymessagingapp.utils.StringUtils
import java.io.File

class AndroidDownloader(private val application: Application) {

    private val downloadManager by lazy { application.getSystemService(DownloadManager::class.java) }

    /**
     *  The http protocol is not allowed on Android 10 unless you request clear text traffic in manifest file.
     *
     *  For getExternalFilesDir() you do not need read/write permissions in manifest file nor request
     *  legacy for them. Also no code needed at run time.
     */
    fun downloadFile(mediaData: MediaData): Long {
        val fileName = mediaData.fileName ?: "downloaded_file_${StringUtils.getCurrentTime()}"
//        val title = application.getString(
//            R.string.dot_3,
//            application.getString(R.string.app_name),
//            application.getString(R.string.download),
//            fileName
//        )
        val request = DownloadManager.Request(mediaData.url!!.toUri())
            .setMimeType(mediaData.mime)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(fileName)
//            .setAllowedOverRoaming(false)
//            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setDestinationUri(Uri.fromFile(File(application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)))
        return downloadManager.enqueue(request)
    }
}