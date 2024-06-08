package com.android.kotlin.familymessagingapp.model

import com.google.gson.annotations.SerializedName

data class ChatRoom(
    @SerializedName("chatroom_id") val chatRoomId: String,
    @SerializedName("chatroom_name") val chatRoomName: String,
    @SerializedName("users") val users: List<User>,
    @SerializedName("messages") val messages: List<Message>
)