package com.android.kotlin.familymessagingapp.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target

// Rule : The first parameter in the binding function is always view
// If using object class, add fields @JvmStatic before the method

@BindingAdapter("bindUserAvatar")
fun <T> bindUserAvatar(imageView: ImageView, photo: T?) {
    MediaUtils.loadImageFollowImageViewSize(
        imageView = imageView,
        photo = photo,
        fallback = R.drawable.ic_user_default,
        placeholder = R.drawable.ic_user_default,
        scaleType = ImageView.ScaleType.CENTER_CROP
    )
}

@BindingAdapter("bindPhotoMessage")
fun <T> bindPhotoMessage(imageView: ImageView, photo: T?) {
    photo?.let {
        val density = imageView.context.resources.displayMetrics.density

        Glide.with(imageView.context)
            .load(photo)
            .error(R.drawable.ic_broken_message_image)
            .placeholder(R.drawable.image_placeholder)
            .override(Target.SIZE_ORIGINAL, (300 * density).toInt())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }
}

@BindingAdapter("bindChatRoomImage")
fun bindChatRoomImage(imageView: ImageView, chatRoom: ChatRoom) {
    val chatRoomImageUrl = chatRoom.chatRoomImage
    if (chatRoomImageUrl.isNullOrEmpty()) {
        val defaultImageRes = when (chatRoom.chatRoomType) {
            ChatRoomType.Group.type -> R.drawable.group
            else -> R.drawable.ic_user_default
        }
        imageView.setImageResource(defaultImageRes)
        return
    }

    MediaUtils.loadImageFollowImageViewSize(
        imageView = imageView,
        photo = chatRoomImageUrl,
        scaleType = ImageView.ScaleType.CENTER_CROP
    )
}

fun <T> loadImageFollowImageViewSize(imageView: ImageView, image: T?) {
    if (image == null) return

    val density = imageView.context.resources.displayMetrics.density
    val imageViewWidth = imageView.width
    val imageViewHeight = imageView.height

    // Giới hạn kích thước hình ảnh dựa trên mật độ màn hình và kích thước của ImageView
    val targetWidth = (imageViewWidth * density).toInt()
    val targetHeight = (imageViewHeight * density).toInt()

    Glide.with(imageView.context)
        .load(image)
        .error(R.drawable.ic_broken_image)
        .placeholder(R.drawable.loading_animation)
        .override(targetWidth, targetHeight)
        .fitCenter()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(imageView)
}

@BindingAdapter("bindChatRoomTypeStatus")
fun bindChatRoomTypeStatus(textView: TextView, chatRoom: ChatRoom) {

}

@BindingAdapter("visibleViewGroupChatRoom")
fun visibleViewGroupChatRoom(view: View, chatRoomType: String?) {
    if (chatRoomType.isNullOrEmpty()) return
    view.visibility = if (chatRoomType == ChatRoomType.Group.type) View.VISIBLE else View.GONE
}

@BindingAdapter("visibleViewDoubleChatRoom")
fun visibleViewDoubleChatRoom(view: View, chatRoomType: String?) {
    if (chatRoomType.isNullOrEmpty()) return
    view.visibility = if (chatRoomType == ChatRoomType.Double.type) View.VISIBLE else View.GONE
}

@BindingAdapter("bindChatRoomName")
fun bindChatRoomName(textView: TextView, chatRoom: ChatRoom) {
    val chatRoomName = chatRoom.chatRoomName
    textView.text = if (chatRoomName.isNullOrEmpty()) {
        textView.context.getString(if (chatRoom.chatRoomType == ChatRoomType.Group.type) R.string.chatroom else R.string.user)
    } else {
        chatRoomName
    }
}
