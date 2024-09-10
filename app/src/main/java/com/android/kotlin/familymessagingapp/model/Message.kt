package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.android.kotlin.familymessagingapp.data.remote.socket.BackendEvent
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

data class Reaction(
    val emoji: String,
    val reactedBy: List<String>
)

@Parcelize
@Serializable
data class MediaData(
    val type: String? = null,
    val url: String? = null,
    val fileName: String? = null,
) : Parcelable

@Parcelize
@IgnoreExtraProperties
data class Message(
    val messageId: String? = null,
    val senderId: String? = null,
    val text: String? = null,
    val photo: String? = null,
    val timestamp: Long? = null,
    val reactions: Map<String, Map<String, Boolean>>? = null,
    val replyMessageId: String? = null,
    val removedBy: String? = null,
    val medias: List<MediaData>? = null,
    @Exclude var pinned: Boolean? = null,
    @Exclude var replyMessage: Message? = null,
    @Exclude @Contextual var senderData: UserData? = null,
    @Exclude var fileDataList: List<FileData>? = null
) : Parcelable {

    @Exclude
    fun isTextEmpty(): Boolean = text.isNullOrEmpty()

    @Exclude
    fun isReplyMessageEmpty(): Boolean = replyMessageId.isNullOrEmpty()

    @Exclude
    fun removedByIsEmpty(): Boolean = removedBy.isNullOrEmpty()

    companion object {
        const val REACTIONS = "reactions"
    }
}

fun Message.toMessageSocketEvent(): BackendEvent.Message = BackendEvent.Message(
    messageId = messageId,
    senderId = senderId,
    text = text,
    photo = photo,
    timestamp = timestamp.toString(),
    senderName = senderData?.username
)
