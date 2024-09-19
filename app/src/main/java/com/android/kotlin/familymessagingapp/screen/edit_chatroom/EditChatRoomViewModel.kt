package com.android.kotlin.familymessagingapp.screen.edit_chatroom

import android.net.Uri
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlin.familymessagingapp.model.ChatRoom

class EditChatRoomViewModel : ViewModel() {

    var initializedForTheFirstTime = true

    var draftChatRoom = ChatRoom()

    private val _saveButtonStatus = MutableLiveData(isInputValid())
    val saveButtonStatus: LiveData<Boolean> = _saveButtonStatus

    fun updateChatRoomName(name: String) {
        draftChatRoom.chatRoomName = name
        _saveButtonStatus.value = isInputValid()
    }

    fun updateChatRoomDescription(description: String) {
        draftChatRoom = draftChatRoom.copy(chatRoomDescription = description)
    }

    fun updateChatRoomImage(image: Uri?) {
        if (image == null) return
        draftChatRoom = draftChatRoom.copy(chatRoomImage = image.toString())
    }

    private fun isInputValid(): Boolean = !draftChatRoom.chatRoomName.isNullOrEmpty()
}