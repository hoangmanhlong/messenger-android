package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

/**
 * @property chatRoomId unique identifier of chat room
 * @property members store uid of users in chatroom
 * @property messages message list of chat room
 * @property lastMessage latest message
 * @property latestTime Last operating time
 * @property chatRoomImage - inference attribute: image of chatroom: get from user
 * @property chatroomName - inference attribute: name of chatroom: get from user
 */
@Parcelize
@IgnoreExtraProperties
data class ChatRoom(
    val chatRoomId: String? = null,
    val members: List<String>? = null,
    val messages: Map<String, Message>? = null,
    val latestTime: Long? = null,
    val isActive: Boolean? = null,
    val lastMessage: String? = null,
    val pinned: Boolean? = null,
    val chatRoomImage: String? = null,
    val chatroomName: String? = null
) : Parcelable {
    companion object {
        const val CHAT_ROOM_ID = "chatRoomId"
        const val MESSAGES = "messages"
        const val LAST_MESSAGE = "lastMessage"
        const val LATEST_TIME = "latestTime"
    }
}