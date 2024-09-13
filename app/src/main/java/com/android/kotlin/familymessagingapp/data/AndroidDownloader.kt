package com.android.kotlin.familymessagingapp.data

import android.app.Application
import android.app.DownloadManager
import android.os.Environment
import androidx.core.net.toUri
import com.android.kotlin.familymessagingapp.model.MediaData
import com.android.kotlin.familymessagingapp.utils.StringUtils

class AndroidDownloader(private val application: Application) {

    private val downloadManager by lazy { application.getSystemService(DownloadManager::class.java) }

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
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        return downloadManager.enqueue(request)
    }
}