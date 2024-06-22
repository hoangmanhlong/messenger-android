package com.android.kotlin.familymessagingapp.model

import com.google.gson.annotations.SerializedName

/**
 * @property members store uid of users in chatroom
 */
data class ChatRoom(
    @SerializedName("chatroom_id") val chatRoomId: String,
    @SerializedName("members") val members: List<String>,
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("active_time") val time: Long
)