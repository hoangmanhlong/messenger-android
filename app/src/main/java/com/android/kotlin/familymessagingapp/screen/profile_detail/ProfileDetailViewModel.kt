package com.android.kotlin.familymessagingapp.screen.profile_detail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.services.firebase_services.realtime_database.FirebaseRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.utils.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    private val firebaseRealtimeDatabaseService: FirebaseRealtimeDatabaseService
) : ViewModel() {

    var initializedForTheFirstTime = true

    var userData: UserData = UserData()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSaveSuccess: MutableLiveData<Boolean?> = MutableLiveData(null)
    val isSaveSuccess: LiveData<Boolean?> = _isSaveSuccess

    private val _saveButtonStatus: MutableLiveData<Boolean> = MutableLiveData(validInput())
    val saveButtonStatus: LiveData<Boolean> = _saveButtonStatus

    fun setDisplayName(displayName: String) {
        userData = userData.copy(username = displayName)
        _saveButtonStatus.value = validInput()
    }

    fun updateImage(imageUri: Uri?) {
        if (imageUri != null) {
            userData = userData.copy(userAvatar = imageUri.toString())
            _saveButtonStatus.value = validInput()
        }
    }

    fun setPhoneNumber(phoneNumber: String) {
        userData = userData.copy(phoneNumber = phoneNumber)
        _saveButtonStatus.value = validInput()
    }

    private fun validInput(): Boolean {
        val userName = userData.username
        val phoneNumber = userData.phoneNumber
        return !userName.isNullOrEmpty() && (phoneNumber.isNullOrEmpty() || StringUtils.isNumber(
            phoneNumber
        ) && phoneNumber.length >= 10)
    }

    fun saveUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            val saveUserDataResult = firebaseRealtimeDatabaseService.saveUserData(userData)
            _isSaveSuccess.value = saveUserDataResult
            _isLoading.value = false
        }
    }
}