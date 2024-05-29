package com.android.kotlin.familymessagingapp.viewmodel

import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import com.android.kotlin.familymessagingapp.utils.singleArgViewModelFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainViewModel designed to store and manage UI-related data in a lifecycle conscious way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param _firebaseAuthenticationRepository ...
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val _firebaseAuthenticationRepository: FirebaseAuthenticationRepository
) : ViewModel() {

    companion object {
        /**
         * Factory for creating [LoginViewModel]
         *
         * @param arg the repository to pass to [LoginViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::LoginViewModel)
    }

    private val _loadingStatus: MutableLiveData<Boolean> = MutableLiveData(false)
    val loadingStatus: LiveData<Boolean> = _loadingStatus

    private val _authenticationStatus: MutableLiveData<Boolean> =
        MutableLiveData(_firebaseAuthenticationRepository.hasUser())
    val authenticationStatus: LiveData<Boolean> = _authenticationStatus

    fun signInWithActivityResult(activityResult: ActivityResult) {
        updateLoadingStatus(true)
        viewModelScope.launch {
            val signInResult = _firebaseAuthenticationRepository.firebaseGoogleService.signInWithIntent(activityResult.data ?: return@launch)
            updateLoadingStatus(false)

            _authenticationStatus.value = signInResult.data != null
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

    private suspend fun signIn(): IntentSender? = _firebaseAuthenticationRepository.firebaseGoogleService.signIn()
}
