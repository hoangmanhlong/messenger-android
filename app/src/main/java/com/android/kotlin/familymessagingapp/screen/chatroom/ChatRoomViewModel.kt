package com.android.kotlin.familymessagingapp.screen.chatroom

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.android.kotlin.familymessagingapp.services.gemini.GeminiModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SendMessageStatus { SUCCESS, ERROR, SENDING }

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    private val geminiModel: GeminiModel,
    private val localDatabaseRepository: LocalDatabaseRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _emojiPickerVisible: MutableLiveData<Boolean> = MutableLiveData(false)
    val emojiPickerVisible: LiveData<Boolean> = _emojiPickerVisible

    private val _sendMessageStatus: MutableLiveData<SendMessageStatus?> = MutableLiveData(null)
    val sendMessageStatus: LiveData<SendMessageStatus?> = _sendMessageStatus

    lateinit var messages: LiveData<List<Message>>

    var selectedMessage: Message? = null
        private set

    private val _chatRoom: MutableLiveData<ChatRoom> = MutableLiveData(ChatRoom())
    val chatRoom: LiveData<ChatRoom> = _chatRoom

    private val _AIGeneratedText: MutableLiveData<String?> = MutableLiveData(null)
    val AIGeneratedText: LiveData<String?> = _AIGeneratedText

    private val _AICreating = MutableLiveData(false)
    val AICreating: LiveData<Boolean> = _AICreating

    private var message = Message()

    private val _isInputValid = MutableLiveData(isInputValid())
    val isInputValid: LiveData<Boolean> = _isInputValid

    private val _selectedItems = MutableLiveData<List<Uri>>(emptyList())
    val selectedItems: LiveData<List<Uri>> = _selectedItems

    private val _clearEdiText = MutableLiveData(false)
    val clearEdiText: LiveData<Boolean> = _clearEdiText

    val startObservingMessages = MutableLiveData(false)

    fun changeEmojiPickerVisibleStatus() {
        _emojiPickerVisible.value = !_emojiPickerVisible.value!!
    }

    fun setAIGeneratedText(text: String?) {
        _AIGeneratedText.value = text
    }

    fun setSelectedMessage(message: Message) {
        selectedMessage = message
    }

    fun hideEmojiPicker() {
        if (_emojiPickerVisible.value == true)
            _emojiPickerVisible.value = false
    }

    fun setSendMessageStatus(status: SendMessageStatus?) {
        _sendMessageStatus.value = status
    }

    /**
     * Case: Chatroom opened from Search
     * use username, user avatar of user is chatroom name, image.
     * Check chatroom exist => true => add messages listener
     */
    fun setUserData(userData: UserData) {
        _chatRoom.value = _chatRoom.value?.copy(
            chatroomName = userData.username,
            chatRoomImage = userData.userAvatar,
            members = listOf(userData.uid!!)
        )
        viewModelScope.launch {
            if(!userData.uid.isNullOrEmpty()) {
                val chatroomID = firebaseServiceRepository
                    .firebaseRealtimeDatabaseService
                    .checkChatRoomExist(userData.uid)
                if (!chatroomID.isNullOrEmpty()) {
                    _chatRoom.value = _chatRoom.value?.copy(chatRoomId = chatroomID)
                    initMessageListener()
                }
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
        if (!chatRoomId.isNullOrEmpty()) {
            messages = firebaseServiceRepository
                .firebaseRealtimeDatabaseService
                .addChatRoomMessageListener(chatRoomId)
                .asLiveData()

            // start message listener
            startObservingMessages.value = true

            messages.observeForever {
                viewModelScope.launch {
                    _chatRoom.value = _chatRoom.value?.copy(messages = it)
                    val lastMessage = it.lastOrNull()
                    if (lastMessage != null
                        && lastMessage.fromId != Firebase.auth.uid
                        && localDatabaseRepository.appDataStore.getBooleanPreferenceFlow(
                            AppDataStore.ENABLED_AI,
                            false
                        ).first() == true
                    ) {
                        _AICreating.value = true
                        _AIGeneratedText.value = geminiModel.generateContent(lastMessage)
                        _AICreating.value = false
                    } else {
                        _AIGeneratedText.value = null
                    }
                }
            }
        }
    }

    fun removeMessageListener() {
        firebaseServiceRepository.firebaseRealtimeDatabaseService.removeMessageListener()
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
        viewModelScope.launch {
            _sendMessageStatus.value = SendMessageStatus.SENDING
            val sendResult = firebaseServiceRepository
                .firebaseRealtimeDatabaseService
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