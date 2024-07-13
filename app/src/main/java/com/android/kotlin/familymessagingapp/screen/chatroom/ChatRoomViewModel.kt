package com.android.kotlin.familymessagingapp.screen.chatroom

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.services.gemini.GeminiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SendMessageStatus { SUCCESS, ERROR, SENDING }

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    private val geminiModel: GeminiModel
) : ViewModel() {

    private val _emojiPickerVisible = MutableLiveData(false)
    val emojiPickerVisible: LiveData<Boolean> = _emojiPickerVisible

    private val _sendMessageStatus = MutableLiveData(SendMessageStatus.SUCCESS)
    val sendMessageStatus: LiveData<SendMessageStatus> = _sendMessageStatus

    lateinit var messages: LiveData<List<Message>>

    private val _chatRoom: MutableLiveData<ChatRoom> = MutableLiveData(ChatRoom())
    val chatRoom: LiveData<ChatRoom> = _chatRoom

    private var message = Message()

    private val _isInputValid = MutableLiveData(isInputValid())
    val isInputValid: LiveData<Boolean> = _isInputValid

    private val _selectedItems = MutableLiveData<List<Uri>>(emptyList())
    val selectedItems: LiveData<List<Uri>> = _selectedItems

    private val _clearEdiText = MutableLiveData(false)
    val clearEdiText: LiveData<Boolean> = _clearEdiText

    val addMessageListener = MutableLiveData(false)

    fun setEmojiPickerVisible() {
        _emojiPickerVisible.value = !_emojiPickerVisible.value!!
    }

    fun hideEmojiPicker() {
        if (_emojiPickerVisible.value == true)
            _emojiPickerVisible.value = false
    }

    fun setUserData(userData: UserData) {
        _chatRoom.value = _chatRoom.value?.copy(
            chatroomName = userData.username,
            chatRoomImage = userData.userAvatar,
            members = listOf(userData.uid!!)
        )
        viewModelScope.launch {
            val chatroomID =
                firebaseServiceRepository.appRealtimeDatabaseService.checkChatRoomExist(userData.uid!!)
            if (!chatroomID.isNullOrEmpty()) {
                _chatRoom.value = _chatRoom.value?.copy(chatRoomId = chatroomID)
                initMessageListener()
            }
        }
    }

    fun setChatRoom(chatRoom: ChatRoom) {
        _chatRoom.value = _chatRoom.value?.copy(
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
        val chatRoomId = _chatRoom.value?.chatRoomId
        chatRoomId?.let {
            messages = firebaseServiceRepository
                .appRealtimeDatabaseService
                .addChatRoomMessageListener(chatRoomId)
                .asLiveData()
            messages.observeForever {
                _chatRoom.value = _chatRoom.value?.copy(messages = it)
            }
            addMessageListener.value = true
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
            val sendResult = firebaseServiceRepository
                .appRealtimeDatabaseService
                .updateNewMessage(
                    chatRoom = _chatRoom.value!!,
                    message = message
                )

            clearInput()

            when (sendResult) {
                is Result.Success -> {
                    _sendMessageStatus.value = SendMessageStatus.SUCCESS
                    val data = sendResult.data
                    if (data != null && _chatRoom.value!!.chatRoomId == null) {
                        _chatRoom.value = _chatRoom.value?.copy(chatRoomId = data.chatRoomId)
                        initMessageListener()
                    }
                }

                is Result.Error -> {
                    _sendMessageStatus.value = SendMessageStatus.ERROR
                }
            }
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