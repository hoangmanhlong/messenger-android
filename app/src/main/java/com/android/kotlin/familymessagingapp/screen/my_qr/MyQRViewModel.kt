package com.android.kotlin.familymessagingapp.screen.my_qr

import androidx.lifecycle.ViewModel
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyQRViewModel @Inject constructor(
    firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    val currentUserData = firebaseServiceRepository
        .firebaseRealtimeDatabaseService
        .currentUserData
}