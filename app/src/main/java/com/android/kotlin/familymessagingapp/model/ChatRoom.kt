package com.android.kotlin.familymessagingapp.model

import android.content.Context
import android.os.Parcelable
import com.android.kotlin.familymessagingapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * @property chatRoomId unique identifier of chat room
 * @property members store uid of users in chatroom
 * @property messages message list of chat room
 * @property lastMessage latest message
 * @property latestTime Last operating time
 * @property chatRoomImage - inference attribute: image of chatroom: get from user
 * @property chatroomName - inference attribute: name of chatroom: get from usere
 */
@Parcelize
@IgnoreExtraProperties
data class ChatRoom(
    val chatRoomId: String? = null,
    val members: List<String>? = null,
    val messages: Map<String, Message>? = null,
    val latestTime: Long? = null,
    val isActive: Boolean? = null,
    val lastMessage: Message? = null,
    val pinnedMessages: List<String>? = null,
    @Exclude var chatRoomImage: String? = null,
    @Exclude var chatroomName: String? = null,
    @Exclude val membersData: List<UserData>? = null
) : Parcelable {

    companion object {
        const val CHAT_ROOM_ID = "chatRoomId"
        const val MESSAGES = "messages"
        const val LAST_MESSAGE = "lastMessage"
        const val LATEST_TIME = "latestTime"
        const val PINNED_MESSAGES = "pinnedMessages"
    }

    @Exclude
    fun showLastMessageToChatRoomView(context: Context): String {
        var result = context.getString(R.string.connected)
        if (this.lastMessage != null) {
            if (!lastMessage.text.isNullOrEmpty() || !lastMessage.photo.isNullOrEmpty()) {
                if ((!lastMessage.text.isNullOrEmpty() && lastMessage.photo.isNullOrEmpty())
                    || (!lastMessage.text.isNullOrEmpty() && !lastMessage.photo.isNullOrEmpty())
                ) {
                    result = lastMessage.text
                }
                if (!lastMessage.photo.isNullOrEmpty() && lastMessage.text.isNullOrEmpty()) {
                    result = context.getString(R.string.photo_last_message)
                }
            }
        }
        return result
    }

    @Exclude
    fun getChatRoomNameAndImage() {
        membersData?.let {
            if (membersData.size == 2) {
                val otherUserUid = members?.first { it != Firebase.auth.uid }
                val userdata = membersData.first { userData ->  userData.uid == otherUserUid }
                this.chatroomName = userdata.username
                this.chatRoomImage = userdata.userAvatar
            }
        }
    }
}