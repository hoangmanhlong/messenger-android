package com.android.kotlin.familymessagingapp.data.local.work

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.makeStatusNotification
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log

private val TAG = SaveImageToFileWorker::class.java.simpleName

class SaveImageToFileWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val Title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat(
        "yyyy.MM.dd 'at' HH:mm:ss z",
        Locale.getDefault()
    )

    override fun doWork(): Result {
        val resolver = applicationContext.contentResolver
        return try {
            val resourceUri = inputData.getString(Constant.IMAGE_KEY)
            Log.d(TAG, resourceUri.toString())
            val bitmap = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )
            val imageUrl = MediaStore.Images.Media.insertImage(
                resolver, bitmap, Title, dateFormatter.format(Date())
            )


            if (!imageUrl.isNullOrEmpty()) {
                val output = workDataOf(Constant.IMAGE_KEY to imageUrl)

                Result.success(output)
            } else {
                Log.e(TAG, "Writing to MediaStore failed")
                Result.failure()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            Result.failure()
        }
    }
}