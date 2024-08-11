package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

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
    val pinnedMessages: Map<String, PinnedMessage>? = null,
    val chatRoomType: String? = null,
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
    fun getChatRoomNameAndImage() {
        when (chatRoomType) {
            ChatRoomType.Private.type -> {
                val otherUserUid = members?.firstOrNull { it != Firebase.auth.uid }
                val userdata = membersData?.firstOrNull { it.uid == otherUserUid }
                userdata?.let {
                    this.chatroomName = it.username
                    this.chatRoomImage = it.userAvatar
                }
            }

            ChatRoomType.Group.type -> {

            }
            else -> {}
        }
    }

    @Exclude
    fun getPinnedMessagesData(): List<PinnedMessage> {
        return pinnedMessages?.mapNotNull { (messageId, pinnedMessage) ->
            messages?.get(messageId)?.let { message ->
                pinnedMessage.apply {
                    pinnedMessageData = message
                    senderName = membersData?.firstOrNull { it.uid == senderId }?.username
                }
            }
        }?.sortedByDescending { it.pinTime } ?: emptyList()
    }

    @Exclude
    fun getReplyMessages() {
        messages?.values?.forEach { message ->
            message.replyMessageId?.let { replyId ->
                message.replyMessage = messages[replyId]
            }
        }
    }

    @Exclude
    fun getSenderNameOfMessage() {
        messages?.values?.forEach { message ->
            message.senderId?.let { senderId ->
                message.senderName = membersData?.firstOrNull { it.uid == senderId }?.username
            }
        }
    }
}