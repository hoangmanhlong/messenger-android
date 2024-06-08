package com.android.kotlin.familymessagingapp.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.kotlin.familymessagingapp.model.MessageStatus
import com.android.kotlin.familymessagingapp.model.MessageType

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val messageId: Long,
    val fromId: String,
    val toId: String,
    val text: String?,
    val photo: String?,
    val video: String?,
    val audio: String?,
    val timestamp: Long,
    val status: MessageStatus,
    val type: MessageType
)