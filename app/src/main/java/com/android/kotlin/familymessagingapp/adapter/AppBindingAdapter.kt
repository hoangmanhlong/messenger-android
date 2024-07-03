package com.android.kotlin.familymessagingapp.adapter

import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.bumptech.glide.Glide

// Rule : The first parameter in the binding function is always view
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
        } ?: imageView.setImageResource(R.drawable.ic_user_default)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    @BindingAdapter("bindChatroomFormattedTime")
    fun bindChatroomFormattedTime(textView: TextView, time: Long) {
        textView.text = StringUtils.formatTime(time)
    }
}