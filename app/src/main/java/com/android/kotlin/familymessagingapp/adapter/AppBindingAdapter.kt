package com.android.kotlin.familymessagingapp.adapter

import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.PinnedMessage
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.bumptech.glide.Glide

// Rule : The first parameter in the binding function is always view
// If using object class, add fields @JvmStatic before the method
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
@BindingAdapter("bindChatroomFormattedTime")
fun bindChatroomFormattedTime(textView: TextView, time: Long) {
    textView.text = StringUtils.formatTime(time, true)
}

@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("bindMessageFormattedTime")
fun bindMessageFormattedTime(textView: TextView, time: Long) {
    textView.text = StringUtils.formatTime(time, false)
}

@BindingAdapter("bindLastMessageOfChatroom")
fun bindLastMessageOfChatroom(textView: TextView, message: Message) {
    textView.text = StringUtils.showLastMessageToChatRoomView(textView.context, message)
}

@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("bindPinnedBy")
fun bindPinnedBy(textView: TextView, pinnedMessage: PinnedMessage) {
    textView.text = textView.context.getString(
        R.string.pinned_by,
        pinnedMessage.senderName,
        StringUtils.formatTime(pinnedMessage.pinTime!!, false)
    )
}
