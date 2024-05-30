package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val firebaseAuthenticationRepository: FirebaseAuthenticationRepository,
    dataMemoryRepository: DataMemoryRepository
) : ViewModel() {

    private val _authenticateTypeLiveData = dataMemoryRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_AUTHENTICATE_BY_EMAIL)
        .asLiveData()

    val areNotificationsEnabledLiveData = dataMemoryRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.ARE_NOTIFICATION_ENABLED)
        .asLiveData()

    val authenticationStatus: LiveData<Boolean> =
        firebaseAuthenticationRepository.authenticated.asLiveData()

    fun logout() {
        viewModelScope.launch {
            if (_authenticateTypeLiveData.value == true) {
                firebaseAuthenticationRepository.firebaseEmailService.signOut()
            } else {
                firebaseAuthenticationRepository.firebaseGoogleService.signOut()
            }
        }
    }
}