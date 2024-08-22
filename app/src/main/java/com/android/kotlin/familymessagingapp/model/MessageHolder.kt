package com.android.kotlin.familymessagingapp.model

import android.graphics.drawable.Drawable
import com.google.firebase.database.Exclude

data class MessageHolder(
    val messageId: String? = null,
    val text: String? = null,
    val photo: Drawable? = null
) {
    fun isPhotoEmpty(): Boolean = photo == null

    fun isTextEmpty(): Boolean = text.isNullOrEmpty()
}
