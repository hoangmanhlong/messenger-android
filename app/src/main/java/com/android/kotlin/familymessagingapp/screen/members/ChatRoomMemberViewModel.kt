package com.android.kotlin.familymessagingapp.screen.members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.remote.ServerCode
import com.android.kotlin.familymessagingapp.data.remote.socket.AddNewChatRoomMembersSocketEventbus
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Contact
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.BackendServiceRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@HiltViewModel
class ChatRoomMemberViewModel @Inject constructor(
    private val backendServiceRepository: BackendServiceRepository,
    private val firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    private var keyword = ""

    private var initializedForTheFirstTime = true

    private var chatRoom: ChatRoom? = null

    private val _chatRoomMembers: MutableLiveData<List<UserData>?> = MutableLiveData(emptyList())

    private val _isAddMemberStatus: MutableLiveData<Boolean> = MutableLiveData(false)
    val isAddMemberStatus: LiveData<Boolean> = _isAddMemberStatus

    private val _displayedChatRoomMembers: MutableLiveData<List<UserData>?> = MutableLiveData(null)
    val displayedChatRoomMembers: LiveData<List<UserData>?> = _displayedChatRoomMembers

    private val _clearInputButtonStatus: MutableLiveData<Boolean> = MutableLiveData(isInputValid())
    val clearInputButtonStatus: LiveData<Boolean> = _clearInputButtonStatus

    private val _addButtonVisibilityState: MutableLiveData<Boolean> = MutableLiveData(false)
    val addButtonVisibilityState: LiveData<Boolean> = _addButtonVisibilityState

    private val _contacts: MutableLiveData<List<Contact>> = MutableLiveData(null)

    private val _selectedContacts: MutableLiveData<MutableList<Contact>> =
        MutableLiveData(mutableListOf())

    private val _displayedContacts: MutableLiveData<List<Contact>> = MutableLiveData(null)
    val displayedContacts: LiveData<List<Contact>> = _displayedContacts

    private val _saveNewChatRoomMemberSuccess: MutableLiveData<Boolean?> = MutableLiveData(null)
    val saveNewChatRoomMemberSuccess: LiveData<Boolean?> = _saveNewChatRoomMemberSuccess

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private fun isValidChatRoom(): Boolean = _selectedContacts.value?.size?.let { it >= 1 } ?: false

    fun setChatRoom(chatRoom: ChatRoom?) {
        this.chatRoom = chatRoom
        val chatRoomMembers = chatRoom?.membersData
        if (_chatRoomMembers.value?.size != chatRoomMembers?.size) {
            _chatRoomMembers.value = chatRoomMembers
            _displayedChatRoomMembers.value = chatRoomMembers
            initializedForTheFirstTime = false
        }
    }

    private fun isInputValid(): Boolean = keyword.isNotEmpty()

    fun setKeyword(keyword: String) {
        this.keyword = keyword
        _clearInputButtonStatus.value = isInputValid()
        if (keyword.isEmpty()) {
            _displayedChatRoomMembers.value = _chatRoomMembers.value
            _displayedContacts.value = _contacts.value
        }
    }

    fun updateAddMemberStatus(status: Boolean) {
        _isAddMemberStatus.value = status
        if (_isAddMemberStatus.value == true) {
            viewModelScope.launch {
                val contacts =
                    firebaseServiceRepository.firebaseRealtimeDatabaseService.getContacts()
                        .toMutableList()

                contacts.removeIf { _chatRoomMembers.value?.contains(it.contactData) == true }

                _contacts.value = contacts
                _displayedContacts.value = _contacts.value
            }
        }
    }

    fun updateMember(contact: Contact) {
        _selectedContacts.value?.apply {
            if (contains(contact)) remove(contact) else add(contact)
        }
        _addButtonVisibilityState.value = isValidChatRoom()
    }

    fun searchMembers() {
        if (isAddMemberStatus.value == true) {
            if (keyword.isEmpty()) {
                _displayedContacts.value = _contacts.value
                return
            }
            _displayedContacts.value = _contacts.value?.filter { contact ->
                contact.contactData?.email?.contains(keyword) == true
                        || contact.contactData?.username?.contains(keyword) == true
                        || contact.contactData?.phoneNumber?.contains(keyword) == true
                        || contact.contactData?.uid == keyword

            }
        } else {
            if (keyword.isEmpty()) {
                _displayedChatRoomMembers.value = _chatRoomMembers.value
                return
            }
            _displayedChatRoomMembers.value = _chatRoomMembers.value?.filter { userData ->
                userData.email?.contains(keyword) == true
                        || userData.username?.contains(keyword) == true
                        || userData.phoneNumber?.contains(keyword) == true
                        || userData.uid == keyword
            }
        }
    }

    fun saveNewChatRoomMember() {
        viewModelScope.launch {
            val chatRoomId = chatRoom?.chatRoomId
            if(chatRoomId.isNullOrEmpty() || _selectedContacts.value == null || _selectedContacts.value?.size == 0) {
                _saveNewChatRoomMemberSuccess.value = false
                return@launch
            }
            _isLoading.value = true
            EventBus.getDefault().register(this@ChatRoomMemberViewModel)
            backendServiceRepository.addNewChatRoomMembers(
                chatRoomId,
                _selectedContacts.value!!.toList()
            )
        }
    }

    @Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN, sticky = true)
    fun onAddNewChatRoomMember(event: AddNewChatRoomMembersSocketEventbus) {
        _isLoading.value = false
        _saveNewChatRoomMemberSuccess.value = event.responseStatusCode == ServerCode.SUCCESS.code
        val chatRoomId = chatRoom?.chatRoomId
        val numberOfMembers = _selectedContacts.value?.size ?: 1
        if (_saveNewChatRoomMemberSuccess.value == true && !chatRoomId.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                firebaseServiceRepository.firebaseRealtimeDatabaseService
                    .updateChatRoomActivityWhenNewMemberJoined(chatRoomId, numberOfMembers)
            }
        }
        EventBus.getDefault().unregister(this@ChatRoomMemberViewModel)
        EventBus.getDefault().removeStickyEvent(event)
    }
}