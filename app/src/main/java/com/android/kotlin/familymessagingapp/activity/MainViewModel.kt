package com.android.kotlin.familymessagingapp.activity

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.BackendServiceRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.android.kotlin.familymessagingapp.utils.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val backendServiceRepository: BackendServiceRepository,
    private val firebaseServiceRepository: FirebaseServiceRepository,
    private val localDatabaseRepository: LocalDatabaseRepository
) : ViewModel() {

    companion object {
        val TAG: String = MainViewModel::class.java.simpleName
    }

    // Save Notification Status to local
    fun saveNotificationStatus(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabaseRepository.appDataStore.saveBoolean(
                AppDataStore.ARE_NOTIFICATION_ENABLED,
                enabled
            )
        }
    }

    val currentUserLiveData: LiveData<UserData?> = firebaseServiceRepository
        .firebaseRealtimeDatabaseService
        .publicUserData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun setIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    val isTheEnglishLanguageDisplayed: LiveData<Boolean?> = localDatabaseRepository
        .isTheEnglishLanguageDisplayedFlow
        .asLiveData()

    private var _isTheEnglishLanguageSelected = isTheEnglishLanguageDisplayed.value ?: true

    // Update selected by User
    fun isTheEnglishLanguageSelected(isTheEnglishLanguageSelected: Boolean) {
        _isTheEnglishLanguageSelected = isTheEnglishLanguageSelected
    }

    fun changeLanguage() {
        // if selected language different current language saved in Local then make the change language
        // else close fragment
        if (_isTheEnglishLanguageSelected != isTheEnglishLanguageDisplayed.value) {
            viewModelScope.launch {
                // Save to Local
                localDatabaseRepository.appDataStore.saveBoolean(
                    AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED,
                    _isTheEnglishLanguageSelected
                )
                // Change Display Language
                val appLocale = LocaleListCompat.forLanguageTags(
                    if (_isTheEnglishLanguageSelected) Constant.ENGLISH_COUNTRY_CODE
                    else Constant.VIETNAM_COUNTRY_CODE
                )
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
        }
    }

    init {
        executeTheJobOnFirstRun()
//        privateUserData.observeForever {
//            viewModelScope.launch(Dispatchers.IO) {
//                if (it != null) {
//                    localDatabaseRepository.appDataStore.saveBoolean(
//                        AppDataStore.ENABLED_AI,
//                        it.mobileConfig?.turnOnSuggestedAnswers ?: false
//                    )
//                    firebaseServiceRepository.firebaseRealtimeDatabaseService.chatroomObserver(it.chatRooms)
//                }
//            }
//        }
    }

    override fun onCleared() {
        backendServiceRepository.disconnectSocket()
        super.onCleared()
    }

    //Save data when the user runs the app for the first time
    private fun executeTheJobOnFirstRun() {
        viewModelScope.launch(Dispatchers.IO) {
            if (localDatabaseRepository.appDataStore
                    .getBooleanPreferenceFlow(AppDataStore.IS_THE_FIRST_LAUNCH, null)
                    // The terminal operator that returns the first element emitted by the flow
                    // and then cancels flow's collection
                    .first() == null
            ) {
                localDatabaseRepository.appDataStore.apply {
                    saveBoolean(AppDataStore.IS_THE_FIRST_LAUNCH, true)
                    saveCurrentlyDisplayedLanguageOfPhone()
                }
            }
        }
    }
}