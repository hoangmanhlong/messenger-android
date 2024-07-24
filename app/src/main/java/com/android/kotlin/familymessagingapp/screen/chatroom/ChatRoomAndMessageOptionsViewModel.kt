package com.android.kotlin.familymessagingapp.screen.chatroom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomAndMessageOptionsViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    private val _selectedMessageIsPinnedMessage: MutableLiveData<Boolean?> = MutableLiveData(null)
    val selectedMessageIsPinnedMessage: LiveData<Boolean?> = _selectedMessageIsPinnedMessage

    private val _selectedMessageIsMessageOfMe: MutableLiveData<Boolean?> = MutableLiveData(null)
    val selectedMessageIsMessageOfMe: LiveData<Boolean?> = _selectedMessageIsMessageOfMe

    private val _selectedMessage: MutableLiveData<Message?> = MutableLiveData(null)
    val selectedMessage: LiveData<Message?> = _selectedMessage

    private val _chatroom: MutableLiveData<ChatRoom?> = MutableLiveData(null)
    val chatroom: LiveData<ChatRoom?> = _chatroom

    fun setSelectedMessage(message: Message) {
        _selectedMessage.value = message
        _selectedMessageIsPinnedMessage.value = checkMessageIsPinnedMessage(message.messageId)
        _selectedMessageIsMessageOfMe.value = message.senderId == Firebase.auth.uid
    }

    fun setChatRoom(chatroom: ChatRoom) {
        _chatroom.value = chatroom
    }

    private fun checkMessageIsPinnedMessage(messageId: String?): Boolean {
        return if (_chatroom.value != null && !messageId.isNullOrEmpty()) {
            val pinnedMessages = _chatroom.value!!.pinnedMessages
            pinnedMessages != null && pinnedMessages.containsKey(messageId)
        } else {
            false
        }
    }

    fun updateMessageEmoji(emoji: String) {
        viewModelScope.launch {
            val chatRoomId = _chatroom.value?.chatRoomId
            val messageId = _selectedMessage.value?.messageId
            if (!chatRoomId.isNullOrEmpty() && !messageId.isNullOrEmpty()) {
                firebaseServiceRepository
                    .firebaseRealtimeDatabaseService
                    .updateEmojiMessage(chatRoomId, messageId, emoji)
            }
        }
    }
}