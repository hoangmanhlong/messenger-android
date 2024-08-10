package com.android.kotlin.familymessagingapp.screen.scan_qr_code

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.databinding.FragmentScanQrCodeBinding
import com.android.kotlin.familymessagingapp.model.QRCodeInvalidException
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// doc: https://github.com/android/camera-samples/tree/main/CameraX-MLKit
@AndroidEntryPoint
class ScanQRCodeFragment : Fragment() {

    private val viewModel: ScanQRCodeViewModel by viewModels()

    private var _binding: FragmentScanQrCodeBinding? = null

    private val binding get() = _binding!!

    private var barcodeScanner: BarcodeScanner? = null

    private var cameraExecutor: ExecutorService? = null

    private var previewView: PreviewView? = null

    private var cameraController: LifecycleCameraController? = null

    private var scanQRErrorDialog: AlertDialog? = null

    private var netWorkDialog: MaterialAlertDialogBuilder? = null

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (permissionGranted) startCamera()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanQrCodeBinding.inflate(inflater, container, false)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        previewView = binding.viewFinder
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }
        context?.let {
            netWorkDialog = DialogUtils.showNetworkNotAvailableDialog(
                context = it,
                onPositiveClick = {
                    restartCamera()
                },
                onCancelListener = {
                    restartCamera()
                },
                onNegativeClick = {
                    restartCamera()
                },
                cancelable = false
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        viewModel.isLoading.observe(viewLifecycleOwner) {
            (activity as MainActivity).isShowLoadingDialog(it)
        }

        viewModel.scanQRResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (result) {
                    is Result.Success -> {
                        navigateToChatRoom(result.data)
                    }

                    is Result.Error -> {
                        var errorMessage = R.string.error_occurred
                        if (result.exception is QRCodeInvalidException) {
                            errorMessage = R.string.qr_invalid_message
                        }
                        context?.let {
                            if (scanQRErrorDialog == null) {
                                scanQRErrorDialog = DialogUtils.showNotificationDialog(
                                    context = it,
                                    cancelable = false,
                                    message = errorMessage,
                                    title = R.string.scan_qr_error,
                                    onOkButtonClick = { _, _ ->
                                        restartCamera()
                                    }
                                )
                            }
                            scanQRErrorDialog?.show()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera() {
        if (activity == null) return
        cameraController = LifecycleCameraController(requireActivity())

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        cameraController?.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(requireContext()),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(requireContext())
            ) { result: MlKitAnalyzer.Result? ->
                barcodeScanner?.let {
                    val barcodeResults = result?.getValue(barcodeScanner!!)
                    if ((barcodeResults == null) ||
                        (barcodeResults.size == 0) ||
                        (barcodeResults.first() == null)
                    ) {
                        previewView?.overlay?.clear()
                        previewView?.setOnTouchListener { _, _ -> false } //no-op
                        return@MlKitAnalyzer
                    }

                    context?.let {
                        if(NetworkChecker.isNetworkAvailable(it)) {
                            viewModel.scanQRCode(barcodeResults[0].rawValue.toString())
                        } else {
                            netWorkDialog?.show()
                        }
                    }
                    stopCamera()

//                    val qrCodeViewModel = QrCodeViewModel(barcodeResults[0])
//                    val qrCodeDrawable = QrCodeDrawable(qrCodeViewModel)
//
//                    previewView?.setOnTouchListener(qrCodeViewModel.qrCodeTouchCallback)
//                    previewView?.overlay?.apply {
//                        clear()
//                        add(qrCodeDrawable)
//                    }
                }
            }
        )

        restartCamera()
        previewView?.controller = cameraController
    }

    private fun stopCamera() {
        cameraController?.unbind()
    }

    private fun restartCamera() {
        cameraController?.bindToLifecycle(this)
    }

    private fun navigateToChatRoom(userdata: UserData) {
        findNavController().apply {
            previousBackStackEntry?.savedStateHandle?.set(Constant.USER_DATA_KEY, userdata)
            popBackStack()
        }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        context?.let { context ->
            ContextCompat.checkSelfPermission(
                context, it
            ) == PackageManager.PERMISSION_GRANTED
        } ?: false

    }

    fun toggleFlashlight(enable: Boolean) {
        cameraController?.enableTorch(enable)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setScanQRResult(null)
        viewModel.setLoadState(false)
        barcodeScanner?.close()
        cameraExecutor?.shutdown()
        barcodeScanner = null
        cameraExecutor = null
        previewView = null
        cameraController = null
        scanQRErrorDialog = null
        _binding = null
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    fun scanQRCodeFromBitmap(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner: BarcodeScanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        onSuccess(rawValue)
                        return@addOnSuccessListener
                    }
                }
                onFailure(Exception("No QR code found"))
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    companion object {
        val TAG: String = ScanQRCodeFragment::class.java.simpleName
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(Manifest.permission.CAMERA).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}