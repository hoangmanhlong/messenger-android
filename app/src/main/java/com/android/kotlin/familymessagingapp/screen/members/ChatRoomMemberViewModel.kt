package com.android.kotlin.familymessagingapp.screen.members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlin.familymessagingapp.model.UserData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatRoomMemberViewModel @Inject constructor() : ViewModel() {

    private var keyword = ""

    private var initializedForTheFirstTime = true

    private val _chatRoomMembers: MutableLiveData<List<UserData>?> = MutableLiveData(emptyList())

    private val _displayedChatRoomMembers: MutableLiveData<List<UserData>?> = MutableLiveData(null)
    val displayedChatRoomMembers: LiveData<List<UserData>?> = _displayedChatRoomMembers

    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean> = _isSearching

    private val _clearInputButtonStatus: MutableLiveData<Boolean> = MutableLiveData(isInputValid())
    val clearInputButtonStatus: LiveData<Boolean> = _clearInputButtonStatus

    fun setChatRoomMembers(chatRoomMembers: List<UserData>?) {
        if (initializedForTheFirstTime) {
            val members = chatRoomMembers?.toMutableList()
            members?.removeIf { it.uid == Firebase.auth.uid }
            _chatRoomMembers.value = members
            _displayedChatRoomMembers.value = members
            initializedForTheFirstTime = false
        }
    }

    private fun isInputValid(): Boolean = keyword.isNotEmpty()

    fun setKeyword(keyword: String) {
        this.keyword = keyword
        _clearInputButtonStatus.value = isInputValid()
        if (keyword.isEmpty()) _displayedChatRoomMembers.value = _chatRoomMembers.value
    }

    fun updateSearchStatus() {
        _isSearching.value = !(_isSearching.value ?: false)
        if (_isSearching.value == false) {
            _displayedChatRoomMembers.value = _chatRoomMembers.value
        }
    }

    fun searchMembers() {
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