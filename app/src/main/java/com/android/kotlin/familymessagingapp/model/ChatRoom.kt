package com.android.kotlin.familymessagingapp.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName

/**
 * @property members store uid of users in chatroom
 */
@IgnoreExtraProperties
data class ChatRoom(
    @SerializedName("chatroom_id") val chatRoomId: String? = null,
    @SerializedName("members") val members: List<String>? = null,
    @SerializedName("messages") val messages: List<Message>? = null,
    @SerializedName("active_time") val time: Long? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    val chatRoomImage: String? = null,
    val chatroomName: String? = null,
    @SerializedName("last_message") val lastMessage: String? = null
)