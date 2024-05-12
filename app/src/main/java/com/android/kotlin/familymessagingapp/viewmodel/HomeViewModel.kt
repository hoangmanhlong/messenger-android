package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlin.familymessagingapp.firebase_services.FirebaseEmailService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    firebaseEmailService: FirebaseEmailService
) : ViewModel() {

    private val _authenticated: MutableLiveData<Boolean> =
        MutableLiveData(firebaseEmailService.hasUser())
    val authenticated: LiveData<Boolean> = _authenticated

}