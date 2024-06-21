package com.android.kotlin.familymessagingapp.screen.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.firebase_services.realtime_database.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    dataMemoryRepository: DataMemoryRepository
) : ViewModel() {

    val currentUserLiveData = firebaseServiceRepository
        .appRealtimeDatabaseService
        .currentUserDataFlow
        .asLiveData()

    val isTheEnglishLanguageDisplayed = dataMemoryRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)
        .asLiveData()

    private val _authenticateTypeLiveData = dataMemoryRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_AUTHENTICATE_BY_EMAIL, true)
        .asLiveData()

    val areNotificationsEnabledLiveData = dataMemoryRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.ARE_NOTIFICATION_ENABLED, true)
        .asLiveData()

    val authenticationStatus: LiveData<Boolean> =
        firebaseServiceRepository.authenticated.asLiveData()

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_authenticateTypeLiveData.value == true) {
                firebaseServiceRepository.firebaseEmailService.signOut()
            } else {
                firebaseServiceRepository.firebaseGoogleService.signOut()
            }
        }
    }
}