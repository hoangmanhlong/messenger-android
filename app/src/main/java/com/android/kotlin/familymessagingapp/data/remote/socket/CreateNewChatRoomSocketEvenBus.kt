package com.android.kotlin.familymessagingapp.data.remote.socket

import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message

data class CreateNewChatRoomSocketEvenBus(
    val chatRoom: ChatRoom?,
    val responseStatusCode: Int?
)
