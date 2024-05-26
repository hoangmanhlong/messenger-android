package com.android.kotlin.familymessagingapp.viewmodel

import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.FirebaseCallStatus
import com.android.kotlin.familymessagingapp.repository.MessengerRepository
import com.android.kotlin.familymessagingapp.utils.singleArgViewModelFactory
import com.android.kotlin.familymessagingapp.utils.RegexUtils
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainViewModel designed to store and manage UI-related data in a lifecycle conscious way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param _repository ...
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val _repository: MessengerRepository
) : ViewModel() {

    companion object {
        /**
         * Factory for creating [LoginViewModel]
         *
         * @param arg the repository to pass to [LoginViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::LoginViewModel)
    }

    private val _authenticationStatus: MutableLiveData<Boolean> =
        MutableLiveData(_repository.hasUser())
    val authenticationStatus: LiveData<Boolean> = _authenticationStatus

    fun signInWithActivityResult(activityResult: ActivityResult) {
        viewModelScope.launch {
            val signInResult = _repository.firebaseGoogleService.signInWithIntent(activityResult.data ?: return@launch)
            _authenticationStatus.value = signInResult.data != null
        }
    }

    fun launchGoogleSignIn(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        viewModelScope.launch {
            launcher.launch(
                IntentSenderRequest
                    .Builder(signIn() ?: return@launch)
                    .build()
            )
        }
    }

    private suspend fun signIn(): IntentSender? = _repository.firebaseGoogleService.signIn()
}
