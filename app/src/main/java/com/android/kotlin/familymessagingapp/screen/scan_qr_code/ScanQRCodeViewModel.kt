package com.android.kotlin.familymessagingapp.screen.scan_qr_code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.QRCodeInvalidException
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.ScanQRCodeException
import com.android.kotlin.familymessagingapp.utils.StringUtils

@HiltViewModel
class ScanQRCodeViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    private val _scanQRResult: MutableLiveData<Result<UserData>?> = MutableLiveData(null)
    val scanQRResult: MutableLiveData<Result<UserData>?> = _scanQRResult

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: MutableLiveData<Boolean> = _isLoading

    fun setScanQRResult(result: Result<UserData>?) {
        _scanQRResult.value = result
    }

    fun setLoadState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun scanQRCode(qrCode: String) {
        viewModelScope.launch {
            _scanQRResult.value = null
            if (StringUtils.isValidQrCode(qrCode)) {
                _isLoading.value = true
                val uid = StringUtils.getUidFromFormattedQrCode(qrCode)
                val result = firebaseServiceRepository.firebaseRealtimeDatabaseService.search(uid, true)
                _isLoading.value = false
                _scanQRResult.value = if (result.isNotEmpty()) Result.Success(result[0]) else Result.Error(ScanQRCodeException())

            } else {
                _scanQRResult.value = Result.Error(QRCodeInvalidException())
            }
        }
    }
}