package com.android.kotlin.familymessagingapp.screen.edit_chatroom

import android.net.Uri
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditChatRoomViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    var initializedForTheFirstTime = true

    private var currentChatRoom: ChatRoom? = null

    var draftChatRoom = ChatRoom()

    private val _saveButtonStatus = MutableLiveData(isInputValid())
    val saveButtonStatus: LiveData<Boolean> = _saveButtonStatus

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveResult: MutableLiveData<Result<Boolean>?> = MutableLiveData(null)
    val saveResult: LiveData<Result<Boolean>?> = _saveResult

    fun updateChatRoomName(name: String) {
        draftChatRoom.chatRoomName = name
        _saveButtonStatus.value = isInputValid()
    }

    fun updateSaveResult(result: Result<Boolean>?) {
        _saveResult.value = result
    }

    fun updateChatRoom(chatRoom: ChatRoom?) {
        currentChatRoom = chatRoom
    }

    fun updateChatRoomDescription(description: String) {
        draftChatRoom = draftChatRoom.copy(chatRoomDescription = description)
    }

    fun updateChatRoomImage(image: Uri?) {
        if (image == null) return
        draftChatRoom = draftChatRoom.copy(chatRoomImage = image.toString())
    }

    private fun isInputValid(): Boolean = !draftChatRoom.chatRoomName.isNullOrEmpty()

    fun updateChatRoomData() {
        if (currentChatRoom == null || currentChatRoom?.chatRoomId.isNullOrEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            val chatRoomId = currentChatRoom?.chatRoomId
            if (chatRoomId.isNullOrEmpty()) return@launch
            val updatedChatRoom = draftChatRoom.copy(chatRoomId = chatRoomId)
            _saveResult.value = firebaseServiceRepository.firebaseRealtimeDatabaseService
                .updateChatRoomData(updatedChatRoom)
            _isLoading.value = false
        }
    }
}