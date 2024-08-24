package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.android.kotlin.familymessagingapp.data.remote.socket.BackendEvent
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

data class Reaction(
    val emoji: String,
    val reactedBy: List<String>
)

@Parcelize
@Serializable
@IgnoreExtraProperties
data class Message(
    val messageId: String? = null,
    val senderId: String? = null,
    val text: String? = null,
    val photo: String? = null,
    val video: String? = null,
    val audio: String? = null,
    val timestamp: Long? = null,
    val status: Int? = null,
    val type: Int? = null,
    val reactions: Map<String, Map<String, Boolean>>? = null,
    val replyMessageId: String? = null,
    @Exclude var pinned: Boolean? = null,
    @Exclude var replyMessage: Message? = null,
    @Exclude @Contextual var senderData: UserData? = null
) : Parcelable {

    @Exclude
    fun isEmoticonEmpty(): Boolean = reactions.isNullOrEmpty()

    @Exclude
    fun isPhotoEmpty(): Boolean = photo.isNullOrEmpty()

    @Exclude
    fun isTextEmpty(): Boolean = text.isNullOrEmpty()

    @Exclude
    fun isPinned(): Boolean = pinned ?: false

    companion object {
        const val REACTIONS = "reactions"
    }
}

fun Message.toMessageSocketEvent(): BackendEvent.Message = BackendEvent.Message(
    messageId = messageId,
    senderId = senderId,
    text = text,
    photo = photo,
    video = video,
    audio = audio,
    timestamp = timestamp.toString()
)
