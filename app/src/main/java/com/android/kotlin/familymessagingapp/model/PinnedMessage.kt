package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class PinnedMessage(
    val messageId: String? = null,
    val senderId: String? = null,
    val pinTime: Long? = null,
    @Exclude var pinnedMessageData: Message? = null,
    @Exclude var senderName: String? = null
) : Parcelable
