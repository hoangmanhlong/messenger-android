package com.android.kotlin.familymessagingapp.screen.create_group_chat

import android.net.Uri
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.android.kotlin.familymessagingapp.model.Contact
import com.android.kotlin.familymessagingapp.model.InvalidChatRoomException
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.screen.chatroom.NewChatRoomEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

sealed class CreateNewChatRoomStatus {
    data object Success : CreateNewChatRoomStatus()
    data class Fail(val exception: Exception) : CreateNewChatRoomStatus()
    data object Loading : CreateNewChatRoomStatus()
}

@HiltViewModel
class CreateGroupChatViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    companion object {
        val TAG: String = CreateGroupChatViewModel::class.java.simpleName
    }

    private var keyword: String? = null

    private val publicUserData = firebaseServiceRepository
        .firebaseRealtimeDatabaseService
        .publicUserData

    private val _createNewChatRoomStatus: MutableLiveData<CreateNewChatRoomStatus?> =
        MutableLiveData(null)
    val createNewChatRoomStatus: LiveData<CreateNewChatRoomStatus?> = _createNewChatRoomStatus

    private val _theListOfContactsIsBeingDisplayed: MutableLiveData<List<Contact>> =
        MutableLiveData(emptyList())
    val theListOfContactsIsBeingDisplayed: LiveData<List<Contact>> =
        _theListOfContactsIsBeingDisplayed

    private var contactOfMe: Contact? = null

    private val _selectedContacts: MutableLiveData<MutableList<Contact>> =
        MutableLiveData(mutableListOf())

    private val _chatRoom: MutableLiveData<ChatRoom> =
        MutableLiveData(ChatRoom(chatRoomType = ChatRoomType.Group.type))
    val chatRoom: LiveData<ChatRoom> = _chatRoom

    private val _contacts: MutableLiveData<List<Contact>> = MutableLiveData(null)

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
            _theListOfContactsIsBeingDisplayed.value = _contacts.value
        }

        publicUserData.observeForever {
            contactOfMe = if (it != null) {
                Contact(
                    uid = publicUserData.value!!.uid,
                    status = null,
                    contactData = publicUserData.value
                )
            } else {
                null
            }
        }
    }

    fun updateKeyword(keyword: String?) {
        this.keyword = keyword
        _clearSearchInputState.value = !keyword.isNullOrEmpty()
    }

    fun updateSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
        _chatRoom.value = _chatRoom.value?.copy(chatRoomImage = uri?.toString())
    }

    fun updateChatRoomName(chatRoomName: String?) {
        _chatRoom.value = _chatRoom.value?.copy(chatRoomName = chatRoomName)
    }

    fun updateMember(contact: Contact) {
        _selectedContacts.value?.apply {
            if (contains(contact)) remove(contact) else add(contact)
        }
        _createGroupButtonVisibilityState.value = isValidChatRoom()
    }

    fun setCreateNewChatRoomStatus(status: CreateNewChatRoomStatus?) {
        _createNewChatRoomStatus.value = status
    }

    fun searchContact(keyword: String?) {
        if (keyword.isNullOrEmpty()) {
            _theListOfContactsIsBeingDisplayed.value = _contacts.value
            return
        }
        viewModelScope.launch {
            _theListOfContactsIsBeingDisplayed.value = _contacts.value?.filter { contact ->
                contact.contactData?.email?.contains(keyword) == true
                        || contact.contactData?.username?.contains(keyword) == true
                        || contact.contactData?.phoneNumber?.contains(keyword) == true
                        || contact.contactData?.uid == keyword
            }
        }
    }

    /**
     * Chat room is valid when it has at least 2 members
     */
    private fun isValidChatRoom(): Boolean = _selectedContacts.value?.size?.let { it >= 2 } ?: false

    fun createChatRoom() {
        viewModelScope.launch {

            // check chat room is valid. If not, show error popup
            if (_chatRoom.value == null || _selectedContacts.value == null || _selectedContacts.value!!.size < 2 || contactOfMe == null) {
                _createNewChatRoomStatus.value =
                    CreateNewChatRoomStatus.Fail(InvalidChatRoomException())
                return@launch
            }

            // switch to creating state
            _createNewChatRoomStatus.value = CreateNewChatRoomStatus.Loading

            // Add current user data to update member list
            _selectedContacts.value!!.add(0, contactOfMe!!)

            // List containing uid of members in chat room
            val membersId: MutableList<String> = mutableListOf()

            // Get uid in contact
            _selectedContacts.value?.mapNotNull { it.uid?.let { it1 -> membersId.add(it1) } }

            // update members list in chat room
            _chatRoom.value = _chatRoom.value?.copy(members = membersId)

            // start send request create chatroom to server
            EventBus.getDefault().register(this@CreateGroupChatViewModel)
            firebaseServiceRepository.firebaseRealtimeDatabaseService.createChatRoom(
                chatRoom = _chatRoom.value!!,
                message = null
            )
        }
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onCreateNewChatRoomListener(newChatRoomEventBus: NewChatRoomEventBus) {
        when (val result = newChatRoomEventBus.result) {
            is Result.Success -> {
                _createNewChatRoomStatus.value = CreateNewChatRoomStatus.Success
            }

            is Result.Error -> {
                _createNewChatRoomStatus.value = CreateNewChatRoomStatus.Fail(result.exception)
            }
        }
        _selectedContacts.value?.remove(contactOfMe)
        EventBus.getDefault().unregister(this@CreateGroupChatViewModel)
        EventBus.getDefault().removeStickyEvent(newChatRoomEventBus)
    }
}