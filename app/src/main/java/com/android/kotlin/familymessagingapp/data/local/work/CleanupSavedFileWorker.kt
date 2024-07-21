package com.android.kotlin.familymessagingapp.data.local.work

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.kotlin.familymessagingapp.utils.Constant
import java.io.File

private val TAG = CleanupSavedFileWorker::class.java.simpleName
class CleanupSavedFileWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val appContext = applicationContext

    override fun doWork(): Result {
        return try {
            cleanupDirectory()
            Result.success()
        } catch (exception: Exception) {
            exception.printStackTrace()
            Result.failure()
        }
    }

    private fun cleanupDirectory() {
        val outputDirectory = File(appContext.filesDir, Constant.OUTPUT_PATH)
        if (outputDirectory.exists()) {
            val entries = outputDirectory.listFiles()
            if (entries != null) {
                for (entry in entries) {
                    val name = entry.name
                    if (name.isNotEmpty() && name.endsWith(".png")) {
                        val deleted = entry.delete()
                        Log.i(TAG, "Deleted $name - $deleted")
                    }
                }
            }
        }
    }
}