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
 * @property pinnedMessages Pinned Messages List
 * @property chatRoomType Type of chat room. include [ChatRoomType]
 * @property chatRoomActivity - activity of chatroom: latest activity of chatroom
 * @property chatRoomImage - inference attribute: image of chatroom: get from user
 * @property chatRoomName - inference attribute: name of chatroom: get from user
 * @property membersData - inference attribute: list of user data in chatroom
 */
@Parcelize
@IgnoreExtraProperties
data class ChatRoom(
    val chatRoomId: String? = null,
    val members: List<String>? = null,
    val messages: Map<String, Message>? = null,
    val pinnedMessages: Map<String, PinnedMessage>? = null,
    val chatRoomType: String? = null,
    var chatRoomActivity: ChatRoomActivity? = null,
    var chatRoomImage: String? = null,
    var chatRoomName: String? = null,
    @Exclude val membersData: List<UserData>? = null,
    @Exclude val roleInChatRoom: String? = null
) : Parcelable {

    companion object {
        const val CHAT_ROOM_ID = "chatRoomId"
        const val MESSAGES = "messages"
        const val PINNED_MESSAGES = "pinnedMessages"
        const val CHAT_ROOM_IMAGE = "chatRoomImage"
        const val CHAT_ROOM_NAME = "chatRoomName"
        const val CHAT_ROOM_TYPE = "chatRoomType"
        const val CHAT_ROOM_ACTIVITY = "chatRoomActivity"
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
        }
            ?.filter {
                it.text == it.pinnedMessageData?.text || it.pinnedMessageData?.medias?.contains(
                    it.pinnedMediaData
                ) == true
            }
            ?.sortedByDescending { it.pinTime } ?: emptyList()
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
    fun getDataOfUserPerformingLatestActivity() {
        chatRoomActivity?.let { activity ->
            this.chatRoomActivity = this.chatRoomActivity!!.copy(
                dataOfUserPerformingTheActivity = membersData?.firstOrNull { userData ->
                    userData.uid == activity.performedByUser
                }
            )
        }
    }
}