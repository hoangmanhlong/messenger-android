package com.android.kotlin.familymessagingapp.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.PinnedMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target

// Rule : The first parameter in the binding function is always view
// If using object class, add fields @JvmStatic before the method
@BindingAdapter("bindNormalImage")
fun <T> bindNormalImage(imageView: ImageView, photo: T?) {
    photo?.let {
        Glide.with(imageView.context)
            .load(photo)
            .error(R.drawable.ic_broken_image)
            .placeholder(R.drawable.loading_animation)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .into(imageView)
    } ?: imageView.setImageResource(R.drawable.ic_user_default)
}

@BindingAdapter("bindChatRoomImage")
fun bindChatRoomImage(imageView: ImageView, chatRoom: ChatRoom) {
    val chatRoomImageUrl = chatRoom.chatRoomImage
    if (chatRoomImageUrl.isNullOrEmpty()) {
        val defaultImageRes = when (chatRoom.chatRoomType) {
            ChatRoomType.Double.type -> R.drawable.ic_user_default
            ChatRoomType.Group.type -> R.drawable.group
            else -> R.drawable.ic_user_default
        }
        imageView.setImageResource(defaultImageRes)
    } else {
        val density = imageView.context.resources.displayMetrics.density
        val imageViewWidth = imageView.width
        val imageViewHeight = imageView.height

        // Giới hạn kích thước hình ảnh dựa trên mật độ màn hình và kích thước của ImageView
        val targetWidth = (imageViewWidth * density).toInt()
        val targetHeight = (imageViewHeight * density).toInt()

        Glide.with(imageView.context)
            .load(chatRoomImageUrl)
            .error(R.drawable.ic_broken_image)
            .placeholder(R.drawable.loading_animation)
            .override(targetWidth, targetHeight)
            .fitCenter()
            .into(imageView)
    }
}


@BindingAdapter("bindChatroomFormattedTime")
fun bindChatroomFormattedTime(textView: TextView, time: Long) {
    textView.text = StringUtils.formatTime(time, true)
}

@BindingAdapter("bindMessageFormattedTime")
fun bindMessageFormattedTime(textView: TextView, time: Long) {
    textView.text = StringUtils.formatTime(time, false)
}

@BindingAdapter("bindLastMessageOfChatroom")
fun bindLastMessageOfChatroom(textView: TextView, message: Message?) {
    textView.text = StringUtils.getFormattedLatestMessageOfChatRoom(textView.context, message)
}

@BindingAdapter("bindPinnedBy")
fun bindPinnedBy(textView: TextView, pinnedMessage: PinnedMessage) {
    textView.text = textView.context.getString(
        R.string.pinned_by,
        pinnedMessage.senderName,
        StringUtils.formatTime(pinnedMessage.pinTime!!, false)
    )
}

@BindingAdapter("bindChatRoomTypeStatus")
fun bindChatRoomTypeStatus(textView: TextView, chatRoom: ChatRoom) {
    if (chatRoom.chatRoomType == ChatRoomType.Group.type) {
        val members = chatRoom.members
        if (members.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
            textView.text = textView.context.getString(
                R.string.format_chatroom_members,
                members.size.toString()
            )
        }
    } else {
        textView.visibility = View.GONE
    }
}

@BindingAdapter("bindFormattedContentOfPinnedMessage")
fun bindFormattedContentOfPinnedMessage(textView: TextView, pinnedMessage: PinnedMessage) {
    val pinnedMessageData = pinnedMessage.pinnedMessageData
    textView.text = StringUtils.showPinnedMessage(textView.context, pinnedMessageData)
}

@BindingAdapter("bindChatRoomName")
fun bindChatRoomName(textView: TextView, chatRoom: ChatRoom) {
    val chatRoomName = chatRoom.chatroomName
    textView.text = if (chatRoomName.isNullOrEmpty()) {
        textView.context.getString(if (chatRoom.chatRoomType == ChatRoomType.Group.type) R.string.chatroom else  R.string.user)
    } else {
        chatRoomName
    }
}
