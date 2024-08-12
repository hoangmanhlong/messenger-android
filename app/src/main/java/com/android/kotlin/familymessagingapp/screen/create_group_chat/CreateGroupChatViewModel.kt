package com.android.kotlin.familymessagingapp.screen.create_group_chat

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateGroupChatViewModel @Inject constructor() : ViewModel() {

    private var keyword: String? = null

    private val memberIDs: MutableList<String> = mutableListOf()

    private val _chatRoom: MutableLiveData<ChatRoom> =
        MutableLiveData(ChatRoom(chatRoomType = ChatRoomType.Group.type))
    val chatRoom: LiveData<ChatRoom> = _chatRoom

    private val _selectedImageUri = MutableLiveData<Uri?>(null)
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    private val _clearSearchInputState: MutableLiveData<Boolean> =
        MutableLiveData(!keyword.isNullOrEmpty())
    val clearSearchInputState: LiveData<Boolean> = _clearSearchInputState

    private val _createGroupButtonVisibilityState: MutableLiveData<Boolean> =
        MutableLiveData(isValidChatRoom())
    val createGroupButtonVisibilityState: LiveData<Boolean> = _createGroupButtonVisibilityState

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

    fun addMember(memberID: String) {
        memberIDs.add(memberID)
        _createGroupButtonVisibilityState.value = isValidChatRoom()
    }

    fun removeMember(memberID: String) {
        memberIDs.remove(memberID)
        _createGroupButtonVisibilityState.value = isValidChatRoom()
    }

    private fun isValidChatRoom(): Boolean = memberIDs.size >= 2

}