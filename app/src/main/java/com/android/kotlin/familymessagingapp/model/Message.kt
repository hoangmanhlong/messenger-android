package com.android.kotlin.familymessagingapp.model

data class Message(
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

enum class MessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO
}
