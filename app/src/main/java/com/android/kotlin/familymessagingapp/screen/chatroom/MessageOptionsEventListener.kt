package com.android.kotlin.familymessagingapp.screen.chatroom

interface MessageOptionsEventListener {
    fun onPinMessage()
    fun onDeleteMessage()
    fun onCopyMessage()
    fun updateMessageEmoji(emoji: String)
    fun onReplyMessage()
}