package com.android.kotlin.familymessagingapp.screen.create_group_chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.android.kotlin.familymessagingapp.model.Contact
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class CreateGroupChatViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    companion object {
        val TAG: String = CreateGroupChatViewModel::class.java.simpleName
    }

    private var keyword: String? = null

    private val _selectedContacts: MutableLiveData<MutableList<Contact>> = MutableLiveData(mutableListOf())
    val selectedContacts: LiveData<MutableList<Contact>> = _selectedContacts

    private val _chatRoom: MutableLiveData<ChatRoom> =
        MutableLiveData(ChatRoom(chatRoomType = ChatRoomType.Group.type))
    val chatRoom: LiveData<ChatRoom> = _chatRoom

    private val _contacts: MutableLiveData<List<Contact>> = MutableLiveData(null)
    val contacts: LiveData<List<Contact>> = _contacts

    private val _selectedImageUri = MutableLiveData<Uri?>(null)
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    private val _clearSearchInputState: MutableLiveData<Boolean> =
        MutableLiveData(!keyword.isNullOrEmpty())
    val clearSearchInputState: LiveData<Boolean> = _clearSearchInputState

    private val _createGroupButtonVisibilityState: MutableLiveData<Boolean> =
        MutableLiveData(isValidChatRoom())
    val createGroupButtonVisibilityState: LiveData<Boolean> = _createGroupButtonVisibilityState

    init {
        viewModelScope.launch {
            _contacts.value =
                firebaseServiceRepository.firebaseRealtimeDatabaseService.getContacts()
        }
    }

    fun updateKeyword(keyword: String?) {
        this.keyword = keyword
        _clearSearchInputState.value = !keyword.isNullOrEmpty()
    }

    fun updateSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun updateChatRoomName(chatRoomName: String?) {
        _chatRoom.value = _chatRoom.value?.copy(chatroomName = chatRoomName)
    }

    fun updateMember(contact: Contact) {
        _selectedContacts.value?.apply {
            if (contains(contact)) remove(contact) else add(contact)
        }
        _createGroupButtonVisibilityState.value = isValidChatRoom()
    }

    private fun isValidChatRoom(): Boolean = _selectedContacts.value?.size?.let { it >= 2 } ?: false

    fun createChatRoom() {
        viewModelScope.launch {
            if (_chatRoom.value == null) return@launch
            _chatRoom.value = chatRoom.value?.copy(
                chatRoomImage = if (selectedImageUri.value == null) null else selectedImageUri.value.toString(),
            )
            EventBus.getDefault().register(this@CreateGroupChatViewModel)
            firebaseServiceRepository.firebaseRealtimeDatabaseService.createChatRoom(
                chatRoom = _chatRoom.value!!,
                message = null
            )
        }
    }
}