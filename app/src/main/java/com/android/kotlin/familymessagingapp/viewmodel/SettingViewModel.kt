package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.repository.MessengerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val messengerRepository: MessengerRepository
) : ViewModel() {


    fun logout() {

    }
    private fun logoutEmailAccount() {
        viewModelScope.launch { messengerRepository.firebaseEmailService.signOut() }
    }

    private fun logoutGoogleAccount() {
        viewModelScope.launch { messengerRepository.firebaseGoogleService.signOut() }
    }
}