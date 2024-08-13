package com.android.kotlin.familymessagingapp.screen.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.model.PrivateUserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    localDatabaseRepository: LocalDatabaseRepository
) : ViewModel() {

    companion object {
        val TAG: String = ProfileViewModel::class.java.simpleName
    }

    private var currentEnabledAIStatus = false

    val currentUserLiveData = firebaseServiceRepository
        .firebaseRealtimeDatabaseService
        .publicUserData

    val privateUserData: LiveData<PrivateUserData?> = firebaseServiceRepository
        .firebaseRealtimeDatabaseService
        .privateUserData

    val isTheEnglishLanguageDisplayed = localDatabaseRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)
        .asLiveData()

    val areNotificationsEnabledLiveData = localDatabaseRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.ARE_NOTIFICATION_ENABLED, true)
        .asLiveData()

    val authenticationStatus: LiveData<Boolean?> = firebaseServiceRepository.authenticateState

    init {
        privateUserData.observeForever { userdata ->
            currentEnabledAIStatus = userdata?.mobileConfig?.turnOnSuggestedAnswers ?: false
        }
    }

    fun enableAI(enabled: Boolean) {
        if (currentEnabledAIStatus != enabled) {
            viewModelScope.launch {
                firebaseServiceRepository
                    .firebaseRealtimeDatabaseService
                    .updateEnabledAIUserData(enabled)
            }
        }
    }

    fun logout() = firebaseServiceRepository.signOut()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun deleteAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            firebaseServiceRepository.deleteAccount()
            _isLoading.value = false
        }
    }
}