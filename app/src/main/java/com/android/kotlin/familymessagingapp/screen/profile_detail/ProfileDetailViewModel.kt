package com.android.kotlin.familymessagingapp.screen.profile_detail

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.firebase_services.realtime_database.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    private val appRealtimeDatabaseService: AppRealtimeDatabaseService
) : ViewModel() {

    private var imageUri: Uri? = null

    private var userData: UserData? = null

    private val _isEditingStatus = MutableLiveData(false)
    val isEditingStatus = _isEditingStatus

    private val _isSaveSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val isSaveSuccess = _isSaveSuccess

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
            userAvatar = userData.userAvatar
        )
    }

    fun saveUserData() {
        viewModelScope.launch {
            _isSaveSuccess.value = appRealtimeDatabaseService.saveUserData(userData!!, imageUri)
        }
    }

}