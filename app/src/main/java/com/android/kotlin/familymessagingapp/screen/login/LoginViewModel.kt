package com.android.kotlin.familymessagingapp.screen.login

import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication.FindIntentSenderResult
import com.facebook.AccessToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainViewModel designed to store and manage UI-related data in a lifecycle conscious way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param firebaseServiceRepository ...
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    localDatabaseRepository: LocalDatabaseRepository
) : ViewModel() {

    val isTheEnglishLanguageDisplayedLiveData =
        localDatabaseRepository.isTheEnglishLanguageDisplayedFlow.asLiveData()

    private val _loadingStatus = MutableLiveData(false)
    val loadingStatus: LiveData<Boolean> = _loadingStatus

    private val _authenticationStatus: MutableLiveData<Boolean?> = MutableLiveData(null)
    val authenticationStatus: LiveData<Boolean?> = _authenticationStatus

    private val _findIntentSenderStatus: MutableLiveData<FindIntentSenderResult?> =
        MutableLiveData(null)
    val findIntentSenderStatus: LiveData<FindIntentSenderResult?> = _findIntentSenderStatus

    fun signInWithActivityResult(activityResult: ActivityResult) {
        viewModelScope.launch {
            updateLoadingStatus(true)
            val intent = activityResult.data
            if (intent != null) {
                val result = firebaseServiceRepository
                    .firebaseGoogleService
                    .signInWithIntent(intent)
                updateLoadingStatus(false)
                when (result) {
                    is Result.Success<Boolean> -> _authenticationStatus.value = true
                    is Result.Error -> _authenticationStatus.value = false
                }
            } else {
                updateLoadingStatus(false)
                _authenticationStatus.value = false
            }
        }
    }

    fun setAuthenticationStatus(status: Boolean?) {
        _authenticationStatus.value = status
    }

    fun setFindIntentSenderStatus(status: FindIntentSenderResult?) {
        _findIntentSenderStatus.value = status
    }

    fun launchGoogleSignIn() {
        updateLoadingStatus(true)
        viewModelScope.launch {
            _findIntentSenderStatus.value = firebaseServiceRepository.firebaseGoogleService.signIn()
            updateLoadingStatus(false)
        }
    }

    private fun updateLoadingStatus(isLoading: Boolean) {
        _loadingStatus.value = isLoading
    }

    fun signInWithFacebook(token: AccessToken) {
        viewModelScope.launch {
            firebaseServiceRepository.facebookService.handleFacebookAccessToken(token)
        }
    }
}
