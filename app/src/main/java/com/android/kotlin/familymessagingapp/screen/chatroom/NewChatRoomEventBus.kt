package com.android.kotlin.familymessagingapp.screen.chatroom

import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Result

data class NewChatRoomEventBus(val result: Result<ChatRoom>)
