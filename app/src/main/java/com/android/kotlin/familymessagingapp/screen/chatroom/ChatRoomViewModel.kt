package com.android.kotlin.familymessagingapp.screen.chatroom

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.work.AppWorkManager
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.CountExceededException
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.ObjectAlreadyExistException
import com.android.kotlin.familymessagingapp.model.PinnedMessage
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.android.kotlin.familymessagingapp.services.gemini.GeminiModel
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.android.kotlin.familymessagingapp.utils.StringUtils
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
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val workManager: AppWorkManager
) : ViewModel() {

    var selectedMessageIsPinnedMessage: Boolean? = null
        private set

    var selectedMessageIsMessageOfMe: Boolean? = null
        private set

    private val _isExpandPinnedMessage: MutableLiveData<Boolean> = MutableLiveData(false)
    val isExpandPinnedMessage: LiveData<Boolean> = _isExpandPinnedMessage

    private val _imageDetailShown = MutableLiveData(false)
    val imageDetailShown: LiveData<Boolean> = _imageDetailShown

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _pinMessageStatus: MutableLiveData<Result<Boolean>?> = MutableLiveData(null)
    val pinMessageStatus: LiveData<Result<Boolean>?> = _pinMessageStatus

    private val _emojiPickerVisible: MutableLiveData<Boolean> = MutableLiveData(false)
    val emojiPickerVisible: LiveData<Boolean> = _emojiPickerVisible

    private val _sendMessageStatus: MutableLiveData<SendMessageStatus?> = MutableLiveData(null)
    val sendMessageStatus: LiveData<SendMessageStatus?> = _sendMessageStatus

    var selectedMessage: Message? = null
        private set

    private val _pinnedMessages: MutableLiveData<List<PinnedMessage>> = MutableLiveData(emptyList())
    val pinnedMessages: LiveData<List<PinnedMessage>> = _pinnedMessages

    var imageMessageDrawable: Drawable? = null
        private set

    private val _chatRoom: MutableLiveData<ChatRoom?> = MutableLiveData(ChatRoom())
    val chatRoom: LiveData<ChatRoom?> = _chatRoom

    private lateinit var chatroomLiveData: LiveData<ChatRoom?>

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

    private val currentUserData = firebaseServiceRepository.firebaseRealtimeDatabaseService.currentUserData

    init {
        currentUserData.observeForever {
            Log.d(TAG, "currentuseradata: $currentUserData")
        }
    }

    fun changeEmojiPickerVisibleStatus() {
        _emojiPickerVisible.value = !_emojiPickerVisible.value!!
    }

    fun setExpandPinnedMessage() {
        _isExpandPinnedMessage.value = !_isExpandPinnedMessage.value!!
    }

    fun hideLessPinnedMessage() {
        if (_isExpandPinnedMessage.value == true) _isExpandPinnedMessage.value = false
    }

    fun setImageDetailShown(shown: Boolean, drawable: Drawable?) {
        imageMessageDrawable = drawable
        _imageDetailShown.value = shown
    }

    fun setIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun setAIGeneratedText(text: String?) {
        _AIGeneratedText.value = text
    }

    fun setPinMessageStatus(status: Result<Boolean>?) {
        _pinMessageStatus.value = status
    }

    fun setSelectedMessage(message: Message) {
        selectedMessage = message
        selectedMessageIsPinnedMessage = checkMessageIsPinnedMessage(message.messageId)
        selectedMessageIsMessageOfMe = message.senderId == Firebase.auth.uid
    }

    fun hideEmojiPicker() {
        if (_emojiPickerVisible.value == true)
            _emojiPickerVisible.value = false
    }

    fun setSendMessageStatus(status: SendMessageStatus?) {
        _sendMessageStatus.value = status
    }

    fun saveImageToDeviceStorage() {
        imageMessageDrawable?.let {
            viewModelScope.launch {
                workManager.saveImageToDeviceStorage((imageMessageDrawable as BitmapDrawable).bitmap)
            }
        }
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
            if (!userData.uid.isNullOrEmpty()) {
                val chatroomID = firebaseServiceRepository
                    .firebaseRealtimeDatabaseService
                    .checkChatRoomExist(userData.uid)
                if (!chatroomID.isNullOrEmpty()) {
                    _chatRoom.value = _chatRoom.value?.copy(chatRoomId = chatroomID)
                    initChatRoomListener()
                } else {
                    //TODO: chatroom not exist
                }
            }
        }
    }

    fun deleteMessage() {
        viewModelScope.launch {
            if (selectedMessage?.senderId == Firebase.auth.uid && _chatRoom.value?.chatRoomId != null) {
                firebaseServiceRepository.firebaseRealtimeDatabaseService.deleteMessage(
                    _chatRoom.value!!.chatRoomId!!,
                    selectedMessage!!.messageId!!
                )
            }
        }
    }

    fun pinMessage() {
        viewModelScope.launch {
            if (_chatRoom.value?.chatRoomId == null
                || selectedMessage == null
                || selectedMessage?.messageId == null
                || selectedMessageIsPinnedMessage == null) {
                return@launch
            }
            if (selectedMessageIsPinnedMessage == false) {
                val pinnedMessage = _chatRoom.value!!.pinnedMessages ?: emptyMap()
                if (pinnedMessage.keys.size < 3) {
                    if (pinnedMessage.keys.contains(selectedMessage!!.messageId)) {
                        _pinMessageStatus.value = Result.Error(ObjectAlreadyExistException())
                        return@launch
                    }
                    val newPinnedMessage = PinnedMessage(
                        messageId = selectedMessage!!.messageId,
                        senderId = selectedMessage!!.senderId,
                        pinTime = StringUtils.getCurrentTime()
                    )
                    val result = firebaseServiceRepository
                        .firebaseRealtimeDatabaseService
                        .addNewPinnedMessage(_chatRoom.value!!, newPinnedMessage)
                    _pinMessageStatus.value = result
                } else {
                    _pinMessageStatus.value = Result.Error(CountExceededException())
                }
            } else {
                firebaseServiceRepository.firebaseRealtimeDatabaseService
                    .deletePinnedMessage(_chatRoom.value!!, selectedMessage!!.messageId!!)
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
            pinnedMessages = chatRoom.pinnedMessages,
            membersData = chatRoom.membersData
        )
        initChatRoomListener()
    }

    private fun initChatRoomListener() {
        val chatRoomId = _chatRoom.value?.chatRoomId
        if (!chatRoomId.isNullOrEmpty()) {
            chatroomLiveData = firebaseServiceRepository
                .firebaseRealtimeDatabaseService
                .addChatRoomListener(chatRoomId)
                .asLiveData()

            chatroomLiveData.observeForever { chatRoom ->
                _chatRoom.value = _chatRoom.value?.copy(
                    pinnedMessages = chatRoom?.pinnedMessages,
                    messages = chatRoom?.messages,
                    membersData = _chatRoom.value?.membersData
                )
                _pinnedMessages.value = _chatRoom.value?.getPinnedMessagesData()
                if (chatRoom != null) {
                    viewModelScope.launch {
                        val lastMessage = chatRoom.messages?.values?.lastOrNull()
                        if (lastMessage != null
                            && lastMessage.senderId != Firebase.auth.uid
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
    }

    fun checkMessageIsPinnedMessage(messageId: String?): Boolean {
        return if (_chatRoom.value != null && !messageId.isNullOrEmpty()) {
            val pinnedMessages = _chatRoom.value!!.pinnedMessages
            pinnedMessages != null && pinnedMessages.containsKey(messageId)
        } else {
            false
        }
    }

    fun removeMessageListener() {
        firebaseServiceRepository.firebaseRealtimeDatabaseService.removeChatRoomListener()
    }

    fun updateMessageEmoji(emoji: String) {
        viewModelScope.launch {
            val chatRoomId = _chatRoom.value?.chatRoomId
            val messageId = selectedMessage?.messageId
            if (!chatRoomId.isNullOrEmpty() && !messageId.isNullOrEmpty()) {
                firebaseServiceRepository
                    .firebaseRealtimeDatabaseService
                    .updateEmojiMessage(chatRoomId, messageId, emoji)
            }
        }
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
                        initChatRoomListener()
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

    fun copyMessage(activity: Activity) {
        if (!selectedMessage?.text.isNullOrEmpty()) {
            KeyBoardUtils.copyTextToClipBoard(
                activity,
                selectedMessage!!.text!!
            )
        }
    }

    companion object {
        val TAG: String = ChatRoomViewModel::class.java.simpleName
    }
}