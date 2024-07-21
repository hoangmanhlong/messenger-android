package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Message(
    val messageId: String? = null,
    val senderId: String? = null,
    val text: String? = null,
    val photo: String? = null,
    val video: String? = null,
    val audio: String? = null,
    val timestamp: Long? = null,
    val status: Int? = null,
    val type: Int? = null,
    val emoticon: String? = null
) : Parcelable {

    @Exclude
    fun isEmoticonEmpty(): Boolean = emoticon.isNullOrEmpty()

    @Exclude
    fun isPhotoEmpty(): Boolean = photo.isNullOrEmpty()

    @Exclude
    fun isTextEmpty(): Boolean = text.isNullOrEmpty()

    companion object {
        const val EMOTICON = "emoticon"
    }
}
