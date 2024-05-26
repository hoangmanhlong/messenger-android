package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlin.familymessagingapp.firebase_services.email_services.FirebaseEmailService
import com.android.kotlin.familymessagingapp.repository.MessengerRepository
import com.android.kotlin.familymessagingapp.utils.singleArgViewModelFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: MessengerRepository
) : ViewModel() {

    companion object {
        val FACTORY = singleArgViewModelFactory(::HomeViewModel)
    }

    private val _authenticated: MutableLiveData<Boolean> = MutableLiveData(repository.hasUser())
    val authenticated: LiveData<Boolean> = _authenticated

}