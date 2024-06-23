package com.android.kotlin.familymessagingapp.screen.login

import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
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

    private val _authenticationStatus= MutableLiveData(false)
    val authenticationStatus: LiveData<Boolean> = _authenticationStatus

    fun signInWithActivityResult(activityResult: ActivityResult) {
        updateLoadingStatus(true)
        viewModelScope.launch {
            val result = firebaseServiceRepository
                .firebaseGoogleService
                .signInWithIntent(activityResult.data ?: return@launch)
            when(result) {
                is Result.Success<Boolean> -> _authenticationStatus.value = true
                is Result.Error -> _authenticationStatus.value = false
            }
        }
    }

    fun launchGoogleSignIn(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        updateLoadingStatus(true)
        viewModelScope.launch {
            launcher.launch(
                IntentSenderRequest
                    .Builder(signIn() ?: return@launch)
                    .build()
            )
            updateLoadingStatus(false)
        }
    }

    private fun updateLoadingStatus(isLoading: Boolean) {
        _loadingStatus.value = isLoading
    }

    private suspend fun signIn(): IntentSender? =
        firebaseServiceRepository.firebaseGoogleService.signIn()

    fun signInWithFacebook(token: AccessToken) {
        viewModelScope.launch {
            firebaseServiceRepository.facebookService.handleFacebookAccessToken(token)
        }
    }
}
