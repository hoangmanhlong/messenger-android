package com.android.kotlin.familymessagingapp.data.local.room

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey

@Dao
interface MessageDao {
}

@Entity("message")
data class MessageEntity(
    @PrimaryKey
    val messageId: String,
    val fromId: String,
    val toId: String,
    val text: String?,
    val photo: String?,
    val video: String?,
    val audio: String?,
    val timestamp: Long,
    val status: Int,
    val type: Int
)