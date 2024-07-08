package com.android.kotlin.familymessagingapp.screen.chatroom

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.services.gemini.GeminiModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SendMessageStatus { SUCCESS, ERROR, SENDING }

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    private val geminiModel: GeminiModel
) : ViewModel() {

    private val _sendMessageStatus = MutableLiveData(SendMessageStatus.SUCCESS)
    val sendMessageStatus: LiveData<SendMessageStatus> = _sendMessageStatus

    var messages: LiveData<List<Message>>? = null

    private var chatroom = ChatRoom()

    private var message = Message()

    private val _isInputValid = MutableLiveData(isInputValid())
    val isInputValid: LiveData<Boolean> = _isInputValid

    private val _selectedItems = MutableLiveData<List<Uri>>(emptyList())
    val selectedItems: LiveData<List<Uri>> = _selectedItems

    private val _clearEdiText = MutableLiveData(false)
    val clearEdiText: LiveData<Boolean> = _clearEdiText

    fun updateMessagesInChatRoom(messages: List<Message>?) {
        chatroom = chatroom.copy(messages = messages)
    }

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
//                .also { currentMessages ->
//                    Log.d(TAG, "initMessageListener: ")
//                    val lastMessage =
//                        currentMessages.value.orEmpty().sortedBy { it.timestamp }.lastOrNull()
//                    lastMessage?.let {
//                        if (lastMessage.fromId != Firebase.auth.uid) {
//                            val response = geminiModel.model.generateContentStream(
//                                content { lastMessage.text }
//                            )
//                            Log.d(TAG, "initMessageListener: $response")
//                        }
//                    }
//                }
        }
    }

    fun removeMessageListener() {
        firebaseServiceRepository.appRealtimeDatabaseService.removeMessageListener()
    }

    fun setTextMessage(text: String) {
        message = message.copy(text = text.ifEmpty { null })
        _isInputValid.value = isInputValid()
    }

    fun setImageUri(uri: Uri?) {
        _selectedItems.value = uri?.let { listOf(it) }
        message = message.copy(photo = uri?.toString())
        _isInputValid.value = isInputValid()
    }

    private fun isInputValid(): Boolean =
        !message.text.isNullOrEmpty()
                || !message.photo.isNullOrEmpty()
                || !message.audio.isNullOrEmpty()
                || !message.video.isNullOrEmpty()

    fun clearEditText(clear: Boolean) {
        _clearEdiText.value = clear
    }

    fun sendMessage() {
        _sendMessageStatus.value = SendMessageStatus.SENDING
        viewModelScope.launch {
            val sendResult = firebaseServiceRepository.appRealtimeDatabaseService
                .updateNewMessage(chatRoom = chatroom, message = message)
            clearInput()
            _sendMessageStatus.value =
                if (sendResult) SendMessageStatus.SUCCESS else SendMessageStatus.ERROR
        }
    }

    private fun clearInput() {
        clearEditText(true)
        setImageUri(null)
    }

    companion object {
        const val TAG = "ChatRoomViewModel"
    }
}