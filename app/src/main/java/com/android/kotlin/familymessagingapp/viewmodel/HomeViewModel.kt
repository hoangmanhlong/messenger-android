package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import com.android.kotlin.familymessagingapp.utils.singleArgViewModelFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val _firebaseAuthenticationRepository: FirebaseAuthenticationRepository
) : ViewModel() {

    companion object {
        val FACTORY = singleArgViewModelFactory(::HomeViewModel)
    }

    private val _authenticated: MutableLiveData<Boolean> = MutableLiveData(_firebaseAuthenticationRepository.hasUser())
    val authenticated: LiveData<Boolean> = _authenticated

}