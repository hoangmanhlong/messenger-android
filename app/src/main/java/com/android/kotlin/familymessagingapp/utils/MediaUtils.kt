package com.android.kotlin.familymessagingapp.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.webkit.MimeTypeMap
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmapOrNull
import com.android.kotlin.familymessagingapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID


enum class FileType(val value: String) {
    TEXT("0"),
    IMAGE("1"),
    PDF("2"),
    DOC("3"),
    EXCEL("4"),
    PRESENTATION("5"),
    AUDIO("6"),
    VIDEO("7"),
    COMPRESSED("8"),
    SCRIPT("9"),
    FONT("10"),
    UNKNOWN("11"),
}

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

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): String? {
        val displayName = UUID.randomUUID().toString()
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val contentResolver = context.contentResolver
        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            var outputStream: OutputStream? = null
            try {
                outputStream = contentResolver.openOutputStream(uri)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            } finally {
                outputStream?.close()
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)

            return uri.toString()
        }

        return null
    }

    fun createImageUri(context: Context, displayName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val contentResolver = context.contentResolver
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    fun createImageUri(context: Context): Uri? {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    fun getQRCodeBitmapFromString(string: String): Bitmap? {
        return try {
            val formattedQRString = StringUtils.generateFormattedQrCode(string)
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(formattedQRString, BarcodeFormat.QR_CODE, 400, 400)
        } catch (e: Exception) {
            null
        }
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        // create a bitmap object
        var screenshot: Bitmap? = null
        try {
            // inflate screenshot object
            // with Bitmap.createBitmap it
            // requires three parameters
            // width and height of the view and
            // the background color
            screenshot =
                Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            // Now draw this bitmap on a canvas
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            e.stackTrace
        }
        // return the bitmap
        return screenshot
    }

    fun createUriFromDrawable(context: Context, drawable: Drawable): Uri? {
        var file: File? = null
        return try {
            val bitmap = (drawable as BitmapDrawable).bitmap

            file = File(context.cacheDir, "image_shared_to_other_apps.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // Bước 3: Tạo một Uri từ tệp tin vừa lưu
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider", // Đảm bảo bạn đã khai báo trong AndroidManifest.xml
                file
            )
        } catch (e: Exception) {
            file?.delete()
            null
        }
    }

    fun createTempImageFile(context: Context): Uri? {
        var file: File? = null
        return try {
            // Create an image file name
            val storageDir = context.cacheDir

            // Create a temporary file in the cache directory
            file = File.createTempFile(Constant.PHOTO_TAKEN_FROM_CAMERA_NAME, ".jpg", storageDir)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider", // Đảm bảo bạn đã khai báo trong AndroidManifest.xml
                file
            )
        } catch (ex: IOException) {
            file?.delete()
            null
        }
    }

    suspend fun clearCache(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir
                if (cacheDir.isDirectory) {
                    val children = cacheDir.list()
                    for (child in children!!) {
                        File(cacheDir, child).delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isValidMediaFileSize(context: Context, uri: Uri): Boolean {
        val fileSizeInBytes = getFileSize(context, uri)
        val fileSizeInMB = fileSizeInBytes / (1024 * 1024)
        return fileSizeInMB <= Constant.MAXIMUM_FILE_SIZE_MB
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            it.moveToFirst()
            it.getLong(sizeIndex)
        } ?: 0
    }

    fun getFileType(context: Context, uri: Uri): FileType {
        val mimeType = getMimeType(context, uri)
        return when (mimeType) {
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/jpg" -> FileType.IMAGE
            "application/pdf" -> FileType.PDF
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> FileType.DOC
            "text/plain" -> FileType.TEXT
            "application/zip", "application/x-7z-compressed", "application/x-rar-compressed", "application/gzip" -> FileType.COMPRESSED
            "application/x-sh", "application/x-python", "application/javascript", "application/x-java-source" -> FileType.SCRIPT
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> FileType.PRESENTATION
            "audio/mpeg", "audio/x-wav", "audio/ogg", "audio/flac", "audio/aac", "audio/x-ms-wma" -> FileType.AUDIO
            "font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-opentype" -> FileType.FONT
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> FileType.EXCEL
            "video/mp4", "video/x-matroska", "video/x-msvideo", "video/quicktime", "video/x-ms-wmv" -> FileType.VIDEO
            else -> FileType.UNKNOWN
        }
    }

    @DrawableRes
    fun getFileDrawableRes(fileType: FileType): Int {
        return when (fileType) {
            FileType.IMAGE -> R.drawable.mc_file_image
            FileType.PDF -> R.drawable.mc_file_pdf
            FileType.DOC -> R.drawable.mc_file_document
            FileType.TEXT -> R.drawable.mc_file_text
            FileType.COMPRESSED -> R.drawable.mc_file_pack
            FileType.PRESENTATION -> R.drawable.mc_file_presentation
            FileType.SCRIPT -> R.drawable.mc_file_script
            FileType.AUDIO -> R.drawable.mc_file_audio
            FileType.EXCEL -> R.drawable.mc_file_spreadsheet
            FileType.VIDEO -> R.drawable.mc_file_video
            FileType.FONT -> R.drawable.mc_file_font
            FileType.UNKNOWN -> R.drawable.mc_file_unknown
        }
    }

    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri) ?: getMimeTypeFromExtension(uri)
    }

    private fun getMimeTypeFromExtension(uri: Uri): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return if (extension != null) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } else {
            null
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (fileName == null) {
            fileName = uri.path
            val cut = fileName?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                fileName = fileName?.substring(cut + 1)
            }
        }
        return fileName
    }

    /**
     * Delete file in cache
     *
     * The ContentResolver.delete() method might not always behave as expected when called on
     * background threads, especially when dealing with cached or media content URIs.
     */
    fun deleteUriInCache(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null, null)
    }
}