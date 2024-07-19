package com.android.kotlin.familymessagingapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.android.kotlin.familymessagingapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
}