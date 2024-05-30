package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import com.android.kotlin.familymessagingapp.utils.singleArgViewModelFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    firebaseAuthenticationRepository: FirebaseAuthenticationRepository
) : ViewModel() {

    companion object {
        val FACTORY = singleArgViewModelFactory(::HomeViewModel)
    }

    val authenticated: LiveData<Boolean> =
        firebaseAuthenticationRepository.authenticated.asLiveData()

}