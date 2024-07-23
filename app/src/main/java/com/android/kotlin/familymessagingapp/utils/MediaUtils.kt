package com.android.kotlin.familymessagingapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import androidx.core.graphics.drawable.toBitmapOrNull
import com.android.kotlin.familymessagingapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

object MediaUtils {

    fun <T> loadImageWithListener(
        context: Context,
        photo: T,
        actionOnResourceReady: (Drawable) -> Unit,
        actionOnLoadFailed: () -> Unit,
    ) {
        try {
            Glide.with(context)
                .load(photo)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .sizeMultiplier(0.50f) //optional
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        actionOnLoadFailed()
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        actionOnResourceReady(resource)
                        return true
                    }

                }).submit()
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    suspend fun <T> convertImageUrlToBitmap(context: Context, image: T): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                Glide.with(context).asBitmap().load(image).submit().get()
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun downloadImageDrawableToDeviceStorage(
        context: Context,
        drawable: Drawable
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = drawable.toBitmapOrNull(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                if (bitmap == null) return@withContext false
                else {
                    val file = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "${System.currentTimeMillis()}.png"
                    )
                    val outputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Writes bitmap to a temporary file and returns the Uri for the file
     * @param applicationContext Application context
     * @param bitmap Bitmap to write to temp file
     * @return Uri for temp file with bitmap
     * @throws FileNotFoundException Throws if bitmap file cannot be found
     */
    @Throws(FileNotFoundException::class)
    suspend fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
        return withContext(Dispatchers.IO) {
            val name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString())
            val outputDir = File(applicationContext.filesDir, Constant.OUTPUT_PATH)
            if (!outputDir.exists()) {
                outputDir.mkdirs() // should succeed
            }
            val outputFile = File(outputDir, name)
            var out: FileOutputStream? = null
            try {
                out = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
            } finally {
                out?.let {
                    try {
                        it.close()
                    } catch (ignore: IOException) {
                    }

                }
            }
            Uri.fromFile(outputFile)
        }
    }

    private fun galleryAddPic(context: Context, photoPath: String) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(photoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            context.sendBroadcast(mediaScanIntent)
        }
    }

}