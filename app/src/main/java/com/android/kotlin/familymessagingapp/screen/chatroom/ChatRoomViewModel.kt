package com.android.kotlin.familymessagingapp.screen.chatroom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    lateinit var messages: LiveData<List<Message>>

    private var chatroom = ChatRoom()

    private var message = Message()

    private val _isInputValid = MutableLiveData(isInputValid())
    val isInputValid: LiveData<Boolean> = _isInputValid

    private val _clearEdiText = MutableLiveData(false)
    val clearEdiText: LiveData<Boolean> = _clearEdiText

    fun setChatRoom(chatRoom: ChatRoom) {
        this.chatroom = this.chatroom.copy(
            chatRoomId = chatRoom.chatRoomId,
            chatroomName = chatRoom.chatroomName,
            messages = chatRoom.messages,
            lastMessage = chatRoom.lastMessage,
            latestTime = chatRoom.latestTime,
            isActive = chatRoom.isActive,
            chatRoomImage = chatRoom.chatRoomImage,
            members = chatRoom.members,
            pinned = chatRoom.pinned
        )
        initMessageListener()
    }

    private fun initMessageListener() {
        this.chatroom.chatRoomId?.let {
            messages = firebaseServiceRepository
                .appRealtimeDatabaseService
                .addChatRoomMessageListener(this.chatroom.chatRoomId!!)
                .asLiveData()
        }
    }

    fun removeMessageListener() {
        firebaseServiceRepository.appRealtimeDatabaseService.removeMessageListener()
    }

    fun setTextMessage(text: String) {
        message = message.copy(text = text)
        _isInputValid.value = isInputValid()
    }

    private fun isInputValid(): Boolean =
        !message.text.isNullOrEmpty()
                || !message.photo.isNullOrEmpty()
                || !message.audio.isNullOrEmpty()
                || !message.video.isNullOrEmpty()

    fun clearEdtText(clear: Boolean) {
        _clearEdiText.value = clear
    }

    fun sendMessage() {
        viewModelScope.launch {
            firebaseServiceRepository.appRealtimeDatabaseService
                .updateNewMessage(chatRoom = chatroom, message = message)
            _clearEdiText.value = true
        }
    }
}