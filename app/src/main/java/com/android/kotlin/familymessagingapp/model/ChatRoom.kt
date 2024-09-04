package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.ktx.Firebase
import kotlinx.parcelize.Parcelize

/**
 * @property chatRoomId unique identifier of chat room
 * @property members store uid of users in chatroom
 * @property messages message list of chat room
 * @property lastMessage latest message
 * @property latestTime Last operating time
 * @property chatRoomImage - inference attribute: image of chatroom: get from user
 * @property chatRoomName - inference attribute: name of chatroom: get from usere
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
    var chatRoomImage: String? = null,
    var chatRoomName: String? = null,
    @Exclude val membersData: List<UserData>? = null,
    @Exclude val roleInChatRoom: String? = null
) : Parcelable {

    companion object {
        const val CHAT_ROOM_ID = "chatRoomId"
        const val MESSAGES = "messages"
        const val LAST_MESSAGE = "lastMessage"
        const val LATEST_TIME = "latestTime"
        const val PINNED_MESSAGES = "pinnedMessages"
        const val CHAT_ROOM_IMAGE = "chatRoomImage"
        const val CHAT_ROOM_NAME = "chatRoomName"
        const val MEMBERS_DATA = "membersData"
        const val ACTIVE = "isActive"
        const val CHAT_ROOM_TYPE = "chatRoomType"
    }

    @Exclude
    fun getChatRoomNameAndImage() {
        if (members.isNullOrEmpty() || membersData.isNullOrEmpty()) return

        when (chatRoomType) {
            ChatRoomType.Double.type -> {
                val otherUserUid = members.firstOrNull { it != Firebase.auth.uid }
                val userdata = membersData.firstOrNull { it.uid == otherUserUid }
                userdata?.let {
                    this.chatRoomName = it.username
                    this.chatRoomImage = it.userAvatar
                }

            }

            ChatRoomType.Group.type -> {
                if (chatRoomName.isNullOrEmpty()) {
                    val membersName =
                        membersData.filter { members.contains(it.uid) }.map { it.username }
                    this.chatRoomName = when {
                        members.size > 3 -> "${membersName[0]}, ${membersName[1]}, ${membersName[2]}, ..."
                        members.size == 3 -> "${membersName[0]}, ${membersName[1]}, ${membersName[2]}"
                        else -> membersName.joinToString(", ")
                    }
                }
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
    fun getSenderDataOfMessage() {
        messages?.values?.forEach { message ->
            message.senderId?.let { senderId ->
                message.senderData = membersData?.firstOrNull { it.uid == senderId }
            }
        }
    }

    @Exclude
    fun getLatestMessageData() {
        lastMessage?.let { message ->
            message.senderData = membersData?.firstOrNull { it.uid == message.senderId }
        }
    }

}