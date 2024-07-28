package com.android.kotlin.familymessagingapp.screen.profile_detail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database.FirebaseRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.utils.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    private val firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService
) : ViewModel() {

    private var imageUri: Uri? = null

    private var userData: UserData = UserData()

    private val _isEditingStatus = MutableLiveData(false)
    val isEditingStatus = _isEditingStatus
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSaveSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val isSaveSuccess: LiveData<Boolean> = _isSaveSuccess

    private val _saveButtonStatus: MutableLiveData<Boolean> = MutableLiveData(validInput())
    val saveButtonStatus: LiveData<Boolean> = _saveButtonStatus

    fun setEditingStatus(isEditing: Boolean) {
        _isEditingStatus.value = isEditing
    }

    fun setImageUri(imageUri: Uri) {
        this.imageUri = imageUri
    }

    fun setUserData(userData: UserData) {
        this.userData = UserData().copy(
            uid = userData.uid,
            username = userData.username,
            email = userData.email,
            phoneNumber = userData.phoneNumber,
            userAvatar = userData.userAvatar,
            chatrooms = userData.chatrooms
        )
    }

    fun setDisplayName(displayName: String) {
        userData = userData.copy(username = displayName)
        _saveButtonStatus.value = validInput()
    }

    fun setPhoneNumber(phoneNumber: String) {
        userData = userData.copy(phoneNumber = phoneNumber)
        _saveButtonStatus.value = validInput()
    }

    private fun validInput(): Boolean {
        return StringUtils.isNumber(userData.phoneNumber.toString())
                && !userData.username.isNullOrEmpty()
                && userData.phoneNumber.toString().length >= 10
    }

    fun saveUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            val saveUserDataResult = firebaseRealtimeDatabaseService.saveUserData(userData, imageUri)
            _isSaveSuccess.value = saveUserDataResult
            _isEditingStatus.value = !saveUserDataResult
            _isLoading.value = false
        }
    }

}