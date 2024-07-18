package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val messageId: String? = null,
    val fromId: String? = null,
    val toId: String? = null,
    val text: String? = null,
    val photo: String? = null,
    val video: String? = null,
    val audio: String? = null,
    val timestamp: Long? = null,
    val status: Int? = null,
    val type: Int? = null
) : Parcelable
