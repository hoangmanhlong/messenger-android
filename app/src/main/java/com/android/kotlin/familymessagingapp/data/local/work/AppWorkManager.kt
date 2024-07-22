package com.android.kotlin.familymessagingapp.data.local.work

import android.app.Application
import android.graphics.Bitmap
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.MediaUtils

class AppWorkManager(private val application: Application) {

    private val workManager = WorkManager.getInstance(application)

    suspend fun saveImageToDeviceStorage(bitmap: Bitmap) {
        val imageUri = MediaUtils.writeBitmapToFile(application, bitmap)
        var continuation = workManager.beginUniqueWork(
            Constant.IMAGE_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(CleanupSavedFileWorker::class.java)
        )
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .setInputData(workDataOf(Constant.IMAGE_KEY to imageUri.toString()))
            .build()
        continuation = continuation.then(save)

        // Actually start the work
        continuation.enqueue()
    }
}