package com.android.kotlin.familymessagingapp.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.android.kotlin.familymessagingapp.R
import com.bumptech.glide.Glide


object AppBindingAdapter {

    @JvmStatic
    @BindingAdapter("bindNormalImage")
    fun <T> bindNormalImage(imageView: ImageView, photo: T?) {
        photo?.let {
            Glide.with(imageView.context)
                .load(photo)
                .error(R.drawable.ic_broken_image)
                .placeholder(R.drawable.loading_animation)
                .into(imageView)
        } ?: imageView.setImageResource(R.drawable.ic_broken_image)
    }
}