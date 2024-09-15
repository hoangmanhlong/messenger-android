package com.android.kotlin.familymessagingapp.screen.scan_qr_code

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.QRCodeInvalidException
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.ScanQRCodeException
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.AppRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.utils.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanQRCodeViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _cameraPermissionGranted: MutableLiveData<Boolean?> = MutableLiveData(isCameraPermissionGranted())
    val cameraPermissionGranted: MutableLiveData<Boolean?> = _cameraPermissionGranted

    private val _scanQRResult: MutableLiveData<Result<UserData>?> = MutableLiveData(null)
    val scanQRResult: MutableLiveData<Result<UserData>?> = _scanQRResult

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _isFlashlightOn: MutableLiveData<Boolean> = MutableLiveData(false)
    val isFlashlightOn: MutableLiveData<Boolean> = _isFlashlightOn

    var goToSettingToGrantCameraPermission = false

    var askedForCameraPermission = false

    fun toggleFlashlight() {
        _isFlashlightOn.value = !(_isFlashlightOn.value ?: false)
    }

    fun turnOffFlashlight() {
        _isFlashlightOn.value = false
    }

    fun setScanQRResult(result: Result<UserData>?) {
        _scanQRResult.value = result
    }

    fun isCameraPermissionGranted(): Boolean = appRepository.isCameraPermissionGranted()

    fun openSetting(context: Context) = appRepository.openSystemSetting(context)

    fun setLoadState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _cameraPermissionGranted.value = granted
    }

    fun handleSelectedPhoto(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true

            // Get bitmap from image uri
            val bitmap = appRepository.convertImageUriToBitmap(uri)

            if (bitmap == null) {
                _isLoading.value = false
                _scanQRResult.value = Result.Error(QRCodeInvalidException())
                return@launch
            }

            // Read qr code from bitmap
            val result = appRepository.readQrCodeFromBitmap(bitmap)

            if (result is Result.Error) {
                _isLoading.value = false
                _scanQRResult.value = Result.Error(result.exception)
                return@launch
            }

            // Find User Data from qr string
            scanQRCode((result as Result.Success<String>).data)
        }
    }

    fun scanQRCode(qrCode: String) {
        viewModelScope.launch {
            _scanQRResult.value = null
            if (StringUtils.isValidQrCode(qrCode)) {
                _isLoading.value = true
                val uid = StringUtils.getUidFromFormattedQrCode(qrCode)

                // If you scan your own QR code, it will return QRCodeInvalidException
                if(uid == firebaseServiceRepository.getCurrentUserUid()) {
                    _isLoading.value = false
                    _scanQRResult.value = Result.Error(QRCodeInvalidException())
                    return@launch
                }

                val result = firebaseServiceRepository.firebaseRealtimeDatabaseService.search(uid, true)
                _isLoading.value = false
                _scanQRResult.value = if (result.isNotEmpty()) Result.Success(result[0]) else Result.Error(ScanQRCodeException())

            } else {
                _isLoading.value = false
                _scanQRResult.value = Result.Error(QRCodeInvalidException())
            }
        }
    }
}