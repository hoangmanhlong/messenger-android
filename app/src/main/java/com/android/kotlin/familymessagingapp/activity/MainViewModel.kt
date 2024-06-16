package com.android.kotlin.familymessagingapp.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.remote.dto.res.ObjectResponse
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.AppRepository
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
//    firebaseAuthenticationRepository: FirebaseAuthenticationRepository,
    private val dataMemoryRepository: DataMemoryRepository,
//    private val appRepository: AppRepository
) : ViewModel() {

//    private val _isKeepSplashScreen: MutableLiveData<Boolean> = MutableLiveData(true)
//    val isKeepSplashScreen: LiveData<Boolean> = _isKeepSplashScreen

//    val authenticated: LiveData<Boolean> =
//        firebaseAuthenticationRepository.authenticated.asLiveData()

    // Save Notification Status to local
    fun saveNotificationStatus(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataMemoryRepository.appDataStore.saveBoolean(
                AppDataStore.ARE_NOTIFICATION_ENABLED,
                enabled
            )
        }
    }

//    init {
//        firstCheck()
//    }
//
//    private fun firstCheck() {
//        viewModelScope.launch {
//            try {
//                val fcmToken = FirebaseMessaging.getInstance().token.await()
//                dataMemoryRepository.appDataStore.saveString(AppDataStore.FCM_TOKEN, fcmToken)
//                val userToken = dataMemoryRepository.appDataStore.getStringPreferenceFlow(
//                    AppDataStore.TOKEN,
//                    null
//                ).first()
//                if (userToken != null) {
//                    val result =
//                        appRepository.sendFCMToken(userToken = userToken, fcmToken = fcmToken)
//                    when (result) {
//                        is Result.Success<ObjectResponse> -> {
//                            _isKeepSplashScreen.value = false
//                        }
//
//                        else -> _isKeepSplashScreen.value = false
//                    }
//                } else {
//                    _isKeepSplashScreen.value = false
//                }
//            } catch (e: Exception) {
//                _isKeepSplashScreen.value = false
//            }
//        }
//    }

    //Save data when the user runs the app for the first time
    fun executeTheJobOnFirstRun() {
        viewModelScope.launch(Dispatchers.IO) {
            if (dataMemoryRepository.appDataStore
                    .getBooleanPreferenceFlow(AppDataStore.IS_THE_FIRST_LAUNCH, null)
                    // The terminal operator that returns the first element emitted by the flow
                    // and then cancels flow's collection
                    .first() == null
            ) {
                dataMemoryRepository.appDataStore.apply {
                    saveBoolean(AppDataStore.IS_THE_FIRST_LAUNCH, true)
                    saveBoolean(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)
                }
            }
        }
    }
}