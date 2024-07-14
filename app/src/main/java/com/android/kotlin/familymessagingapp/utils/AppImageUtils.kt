package com.android.kotlin.familymessagingapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.android.kotlin.familymessagingapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

object AppImageUtils {

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

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> convertImageUrlToBitmap(image: T, context: Context): Bitmap? {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                Glide.with(context)
                    .asBitmap()
                    .load(image)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            if (continuation.isActive) {
                                continuation.resume(resource) {}
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Handle placeholder if needed
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            if (continuation.isActive) {
                                continuation.resume(null) {}
                            }
                        }
                    })
            }
        }
    }
}