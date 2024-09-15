package com.android.kotlin.familymessagingapp.screen.chatroom

import android.app.Activity
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.AndroidDownloader
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.android.kotlin.familymessagingapp.model.CountExceededException
import com.android.kotlin.familymessagingapp.model.FileData
import com.android.kotlin.familymessagingapp.model.MediaData
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.ObjectAlreadyExistException
import com.android.kotlin.familymessagingapp.model.PinnedMessage
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.AppRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.android.kotlin.familymessagingapp.services.gemini.GeminiModel
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

sealed class SendMessageStatus {
    data object Success : SendMessageStatus()
    data class Error(val exception: Exception?) : SendMessageStatus()
    data object Sending : SendMessageStatus()
}

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    private val geminiModel: GeminiModel,
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val androidDownloader: AndroidDownloader,
    private val appRepository: AppRepository
) : ViewModel() {

    var selectedMessageIsPinnedMessage: Boolean? = null
        private set

    private var selectedMessageIsMessageOfMe: Boolean? = null

    var initializedForTheFirstTime = false
        private set

    private val _replyingMessage: MutableLiveData<Message?> = MutableLiveData(null)
    val replyingMessage: LiveData<Message?> = _replyingMessage

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

    private val _selectedMessage: MutableLiveData<Message?> = MutableLiveData(null)
    val selectedMessage: LiveData<Message?> = _selectedMessage

    private val _pinnedMessages: MutableLiveData<List<PinnedMessage>> = MutableLiveData(emptyList())
    val pinnedMessages: LiveData<List<PinnedMessage>> = _pinnedMessages

    private val _saveImageState: MutableLiveData<Boolean?> = MutableLiveData(null)
    val saveImageState: LiveData<Boolean?> = _saveImageState

    var selectedImage: Any? = null
        private set

    private val _chatRoom: MutableLiveData<ChatRoom?> = MutableLiveData(ChatRoom())
    val chatRoom: LiveData<ChatRoom?> = _chatRoom

    private var chatroomLiveData: LiveData<ChatRoom?>? = null

    private val _AIGeneratedText: MutableLiveData<String?> = MutableLiveData(null)
    val AIGeneratedText: LiveData<String?> = _AIGeneratedText

    private val _AICreating = MutableLiveData(false)
    val AICreating: LiveData<Boolean> = _AICreating

    private var message = Message()

    private val _isInputValid = MutableLiveData(isInputValid())
    val isInputValid: LiveData<Boolean> = _isInputValid

    private val _selectedItems = MutableLiveData<List<FileData>>(emptyList())
    val selectedItems: LiveData<List<FileData>> = _selectedItems

    private val _clearInputMessage = MutableLiveData(false)
    val clearInputMessage: LiveData<Boolean> = _clearInputMessage

    private val _replying = MutableLiveData(false)
    val replying: LiveData<Boolean> = _replying

    private val _openPhotoPicker = MutableLiveData(false)
    val openPhotoPicker: LiveData<Boolean> = _openPhotoPicker

    private val _openUploadFile = MutableLiveData(false)
    val openUploadFile: LiveData<Boolean> = _openUploadFile

    private val _openTakePhoto = MutableLiveData(false)
    val openTakePhoto: LiveData<Boolean> = _openTakePhoto

    private val uriOfTakenPhotos = mutableListOf<Uri>()

    private val _selectedMediaData: MutableLiveData<MediaData?> = MutableLiveData(null)
    val selectedMediaData: LiveData<MediaData?> = _selectedMediaData

    var goToSettingToGrantWriteStoragePermission = false

    var goToSettingToGrantCameraPermission = false

    fun resetIsOpenFromNotificationFlag() {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabaseRepository.appDataStore.saveString(
                key = AppDataStore.CHAT_ROOM_ID_FROM_NOTIFICATION,
                value = ""
            )
        }
    }

    fun createUriOfTheImageBeingCapturedByTheCamera(context: Context): Uri? {
        return MediaUtils.createTempImageFile(context).also { uri ->
            if (uri == null) return null
            uriOfTakenPhotos.add(uri)
        }
    }

    fun deleteUriOfTheImageBeingCapturedByTheCamera() {
        if (uriOfTakenPhotos.lastOrNull() == null) return
        deleteTakenPhotoFromCamera(listOf(uriOfTakenPhotos.last()))
    }

    fun resetState() {
        firebaseServiceRepository.firebaseRealtimeDatabaseService.removeCurrentChatRoomListener()
        chatroomLiveData = null
        initializedForTheFirstTime = false
        selectedMessageIsPinnedMessage = null
        selectedMessageIsMessageOfMe = null
        _isExpandPinnedMessage.value = false
        _imageDetailShown.value = false
        _isLoading.value = false
        _pinMessageStatus.value = null
        _emojiPickerVisible.value = false
        _sendMessageStatus.value = null
        _selectedMessage.value = null
        _pinnedMessages.value = emptyList()
        _saveImageState.value = null
        selectedImage = null
        _chatRoom.value = ChatRoom()
        _AIGeneratedText.value = null
        _AICreating.value = false
        _isInputValid.value = isInputValid()
        _selectedItems.value = emptyList()
        _clearInputMessage.value = false
        _replying.value = false
        message = Message()
        _replyingMessage.value = null
        _openPhotoPicker.value = false
        _openUploadFile.value = false
        _openTakePhoto.value = false
        deleteTakenPhotoFromCamera(uriOfTakenPhotos)
        uriOfTakenPhotos.clear()
        _selectedMediaData.value = null
        goToSettingToGrantWriteStoragePermission = false
        goToSettingToGrantCameraPermission = false
    }

    fun setSelectMediaData(mediaData: MediaData?) {
        _selectedMediaData.value = mediaData
    }

    fun openPhotoPicker(isOpen: Boolean) {
        _openPhotoPicker.value = isOpen
    }

    fun openUploadFile(isOpen: Boolean) {
        _openUploadFile.value = isOpen
    }

    fun openTakePhoto(isOpen: Boolean) {
        _openTakePhoto.value = isOpen
    }

    fun changeEmojiPickerVisibleStatus() {
        _emojiPickerVisible.value = !_emojiPickerVisible.value!!
    }

    fun setReplyingMessage(isReplying: Boolean) {
        _replyingMessage.value = if (isReplying) _selectedMessage.value else null
        _replying.value = isReplying
        message = message.copy(replyMessageId = _replyingMessage.value?.messageId)
    }

    fun setExpandPinnedMessage() {
        _isExpandPinnedMessage.value = !_isExpandPinnedMessage.value!!
    }

    fun hideLessPinnedMessage() {
        if (_isExpandPinnedMessage.value == true) _isExpandPinnedMessage.value = false
    }

    fun <T> setImageDetailShown(shown: Boolean, image: T?, mediaData: MediaData?) {
        selectedImage = image
        _imageDetailShown.value = shown
        _selectedMediaData.value = mediaData
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

    fun setSavingImageState(state: Boolean?) {
        _saveImageState.value = state
    }

    fun setSelectedMessage(message: Message) {
        _selectedMessage.value = message
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

    fun shareImage(context: Context, drawable: Drawable) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.shareBitmap(context, (drawable as BitmapDrawable).bitmap)
        }
    }

    /**
     * Case: Chatroom opened from Search
     * use username, user avatar of user is chatroom name, image.
     * Check chatroom exist => true => add messages listener
     */
    fun setUserData(userData: UserData) {
        _chatRoom.value = _chatRoom.value?.copy(
            chatRoomName = userData.username,
            chatRoomImage = userData.userAvatar,
            members = listOf(userData.uid!!, Firebase.auth.uid!!),
            chatRoomType = ChatRoomType.Double.type
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
//                    _isLoading.value = false
                    //TODO: chatroom not exist
                }
            }
        }
    }

    fun deleteMessage() {
        viewModelScope.launch {
            if (_selectedMessage.value?.senderId == Firebase.auth.uid && _chatRoom.value?.chatRoomId != null) {
                firebaseServiceRepository.firebaseRealtimeDatabaseService.deleteMessage(
                    _chatRoom.value!!.chatRoomId!!,
                    selectedMessage.value!!
                )
            }
        }
    }

    fun pinMessage() {
        viewModelScope.launch {
            if (_chatRoom.value == null
                || _chatRoom.value?.chatRoomId == null
                || selectedMessage.value == null
                || _selectedMessage.value?.messageId == null
                || selectedMessageIsPinnedMessage == null
                || Firebase.auth.uid.isNullOrEmpty()
            ) {
                return@launch
            }
            if (selectedMessageIsPinnedMessage == false) {
                val pinnedMessage = _chatRoom.value!!.pinnedMessages ?: emptyMap()
                if (pinnedMessage.keys.size < 3) {
                    if (pinnedMessage.keys.contains(_selectedMessage.value!!.messageId)) {
                        _pinMessageStatus.value = Result.Error(ObjectAlreadyExistException())
                        return@launch
                    }
                    val newPinnedMessage = PinnedMessage(
                        messageId = _selectedMessage.value!!.messageId,
                        senderId = Firebase.auth.uid!!,
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
                    .deletePinnedMessage(_chatRoom.value!!, _selectedMessage.value!!.messageId!!)
            }
        }
    }

    fun setChatRoom(chatRoom: ChatRoom) {
        _chatRoom.value = _chatRoom.value?.copy(
            chatRoomId = chatRoom.chatRoomId,
            chatRoomName = chatRoom.chatRoomName,
            messages = chatRoom.messages,
            chatRoomImage = chatRoom.chatRoomImage,
            members = chatRoom.members,
            pinnedMessages = chatRoom.pinnedMessages,
            membersData = chatRoom.membersData,
            chatRoomType = chatRoom.chatRoomType,
            chatRoomActivity = chatRoom.chatRoomActivity
        )
        _chatRoom.value?.getReplyMessages()
        _pinnedMessages.value = _chatRoom.value?.getPinnedMessagesData()
        initChatRoomListener()
    }

    private fun initChatRoomListener() {
        initializedForTheFirstTime = true
        val chatRoomId = _chatRoom.value?.chatRoomId
        if (!chatRoomId.isNullOrEmpty()) {
            chatroomLiveData = firebaseServiceRepository
                .firebaseRealtimeDatabaseService
                .addChatRoomByIdListener(chatRoomId)
                .asLiveData()

            chatroomLiveData?.observeForever { chatRoom ->
                _chatRoom.value = _chatRoom.value?.copy(
                    chatRoomId = chatRoom?.chatRoomId,
                    messages = chatRoom?.messages,
                    chatRoomActivity = chatRoom?.chatRoomActivity,
                    members = chatRoom?.members,
                    pinnedMessages = chatRoom?.pinnedMessages,
                    membersData = chatRoom?.membersData,
                    chatRoomType = chatRoom?.chatRoomType
                )
                _chatRoom.value?.getReplyMessages()
                _pinnedMessages.value = _chatRoom.value?.getPinnedMessagesData()
                if (chatRoom != null) {
                    viewModelScope.launch {
                        val lastMessage = chatRoom.messages?.values?.maxByOrNull { it.timestamp!! }
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

    private fun checkMessageIsPinnedMessage(messageId: String?): Boolean {
        return if (_chatRoom.value != null && !messageId.isNullOrEmpty()) {
            val pinnedMessages = _chatRoom.value!!.pinnedMessages
            pinnedMessages != null && pinnedMessages.containsKey(messageId)
        } else {
            false
        }
    }

    fun updateMessageEmoji(emoji: String) {
        viewModelScope.launch {
            val chatRoomId = _chatRoom.value?.chatRoomId
            val messageId = _selectedMessage.value?.messageId
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

    private fun addSelectedItems(items: List<FileData>) {
        val currentItems = _selectedItems.value?.toMutableList() ?: mutableListOf()

        // Add only new items to the list
        val newItems = items.filterNot { currentItems.contains(it) }

        // If there are any new items, add them to the list and update LiveData
        if (newItems.isNotEmpty()) {
            currentItems.addAll(newItems)
            _selectedItems.value = currentItems
        }
        message = message.copy(fileDataList = _selectedItems.value)

        _isInputValid.value = isInputValid()
    }

    fun addSelectedUris(items: List<Uri>) {
        addSelectedItems(localDatabaseRepository.updateFileDataFromUri(items))
    }

    fun removeItemInSelectedItems(fileData: FileData) {
        // Delete taken photo from device when user remove it from selected items

        // Check fileData have in uriOfTakenPhotos. if so take it out
        val uriInUriOfTakenPhotos = uriOfTakenPhotos.firstOrNull { uri -> uri.toString() == fileData.uri.toString() }

        // If it exists then clear it from cache
        if (uriInUriOfTakenPhotos != null) deleteTakenPhotoFromCamera(listOf(uriInUriOfTakenPhotos))

        // Remove fileData from selected items
        _selectedItems.value = _selectedItems.value?.toMutableList()?.apply { remove(fileData) }

        message = message.copy(fileDataList = _selectedItems.value)

        _isInputValid.value = isInputValid()
    }

    fun addUriOfTheImageBeingCapturedByTheCameraInSelectedItems() {
        if (uriOfTakenPhotos.isNotEmpty()) addSelectedUris(listOf(uriOfTakenPhotos.last()))
    }

    private fun isInputValid(): Boolean = !message.text.isNullOrEmpty() || !message.fileDataList.isNullOrEmpty()

    fun clearEditText(clear: Boolean) {
        _clearInputMessage.value = clear
    }

    fun sendMessage() {
        viewModelScope.launch {
            _sendMessageStatus.value = SendMessageStatus.Sending
            val sendMessage = Message().copy(
                text = message.text,
                fileDataList = message.fileDataList,
                replyMessageId = message.replyMessageId
            )
            clearInput()
            if (_chatRoom.value?.chatRoomId.isNullOrEmpty()) {
                EventBus.getDefault().register(this@ChatRoomViewModel)
                firebaseServiceRepository
                    .firebaseRealtimeDatabaseService
                    .createChatRoom(_chatRoom.value!!, sendMessage)
            } else {
                val sendResult = try {
                    firebaseServiceRepository.firebaseRealtimeDatabaseService.pushMessageToChatRoom(
                        _chatRoom.value!!,
                        sendMessage
                    )
                } catch (e: Exception) {
                    Result.Error(e)
                }
                when (sendResult) {
                    is Result.Success -> {
                        _sendMessageStatus.value = SendMessageStatus.Success
                    }

                    is Result.Error -> {
                        _sendMessageStatus.value = SendMessageStatus.Error(sendResult.exception)
                    }
                }
            }
        }
    }

    private fun clearInput() {
        clearEditText(true)
        message = Message()
        deleteTakenPhotoFromCamera(uriOfTakenPhotos)
        _selectedItems.value = emptyList()
        setReplyingMessage(false)
    }

    private fun deleteTakenPhotoFromCamera(list: List<Uri>) {
        localDatabaseRepository.deleteTakenPhotoFromCamera(list)
    }

    fun copyMessage(activity: Activity) {
        if (!_selectedMessage.value?.text.isNullOrEmpty()) {
            KeyBoardUtils.copyTextToClipBoard(
                activity,
                selectedMessage.value!!.text!!
            )
        }
    }

    fun downloadFile() {
        if (_selectedMediaData.value?.url.isNullOrEmpty()) return
        androidDownloader.downloadFile(_selectedMediaData.value!!)
    }

    fun leaveChatRoom() {

    }

    companion object {
        val TAG: String = ChatRoomViewModel::class.java.simpleName
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onCreateNewChatRoomListener(newChatRoomEventBus: NewChatRoomEventBus) {
        when (val result = newChatRoomEventBus.result) {
            is Result.Success -> {
                _sendMessageStatus.value = SendMessageStatus.Success
                _chatRoom.value =
                    _chatRoom.value?.copy(chatRoomId = result.data.chatRoomId)
                initChatRoomListener()
            }

            is Result.Error -> {
                _sendMessageStatus.value = SendMessageStatus.Error(result.exception)
            }
        }
        EventBus.getDefault().unregister(this@ChatRoomViewModel)
        EventBus.getDefault().removeStickyEvent(newChatRoomEventBus)
    }
}

private fun <T> MutableLiveData<List<T>>.getCurrentItems(): MutableList<T> {
    return this.value?.toMutableList() ?: mutableListOf()
}