package com.android.kotlin.familymessagingapp.screen.scan_qr_code

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.databinding.FragmentScanQrCodeBinding
import com.android.kotlin.familymessagingapp.model.QRCodeInvalidException
import com.android.kotlin.familymessagingapp.model.QRCodeNotFoundException
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

    private var cameraPermissionRequiredDialog: AlertDialog? = null

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Handle Permission granted/rejected
            viewModel.setCameraPermissionGranted(granted)
        }

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null && context != null) {
                stopCamera()
                NetworkChecker.checkNetwork(
                    context = requireContext(),
                    actionWhenNetworkAvailable = { viewModel.handleSelectedPhoto(uri) },
                    onCancelListener = { restartCamera() },
                    onNegativeClick = { restartCamera() }
                )
            }
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
                onPositiveClick = { restartCamera() },
                onCancelListener = { restartCamera() },
                onNegativeClick = { restartCamera() },
                cancelable = false
            )
        }

        binding.btSelectPhoto.setOnClickListener {
            pickMultipleMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        binding.btFlashlight.setOnClickListener {
            if (cameraController == null || !deviceHasFlashlight()) return@setOnClickListener
            viewModel.toggleFlashlight()
        }

        binding.btAuthorization.setOnClickListener { goToSetting() }

        return binding.root
    }

    private fun goToSetting() {
        activity?.let {
            viewModel.goToSettingToGrantCameraPermission = true
            viewModel.openSetting(it)
        }
    }

    private fun deviceHasFlashlight(): Boolean =
        cameraController?.cameraInfo?.hasFlashUnit() == true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        viewModel.isLoading.observe(viewLifecycleOwner) {
            (activity as MainActivity).isShowLoadingDialog(it)
        }

        viewModel.cameraPermissionGranted.observe(viewLifecycleOwner) {
            it?.let {
                if (it) startCamera()
                else if (!viewModel.askedForCameraPermission) requestPermissions()

                binding.cameraPermissionDeniedView.visibility = if (it) View.GONE else View.VISIBLE
                binding.ivScanQrBorder.visibility = if (it) View.VISIBLE else View.GONE
                binding.scanQrActionView.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        viewModel.scanQRResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (result) {
                    is Result.Success -> {
                        navigateToChatRoom(result.data)
                    }

                    is Result.Error -> {
                        var errorMessage = R.string.error_occurred
                        when (result.exception) {
                            is QRCodeNotFoundException -> errorMessage = R.string.qr_code_not_found

                            is QRCodeInvalidException -> errorMessage = R.string.qr_invalid_message

                            else -> {}
                        }

                        showErrorDialog(errorMessage)
                    }
                }
            }
        }
    }

    private fun showErrorDialog(@StringRes errorMessage: Int) {
        context?.let {
            if (scanQRErrorDialog == null) {
                scanQRErrorDialog = DialogUtils.showNotificationDialog(
                    context = it,
                    cancelable = false,
                    message = errorMessage,
                    title = R.string.scan_qr_error,
                    onOkButtonClick = { _, _ -> restartCamera() }
                )
            }

            if (scanQRErrorDialog?.isShowing == false) scanQRErrorDialog?.show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera() {
        if (activity == null) return
        cameraController = LifecycleCameraController(requireActivity())

        viewModel.isFlashlightOn.observe(viewLifecycleOwner) {
            updateFlashlightStatus(it)
            (binding.btFlashlight as com.google.android.material.button.MaterialButton).icon =
                if (it) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_off)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_on)
                }
        }

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
                        if (NetworkChecker.isNetworkAvailable(it)) {
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

    private fun stopCamera() = cameraController?.unbind()

    private fun restartCamera() = cameraController?.bindToLifecycle(this)

    private fun navigateToChatRoom(userdata: UserData) {
        findNavController().apply {
            previousBackStackEntry?.savedStateHandle?.set(Constant.USER_DATA_KEY, userdata)
            popBackStack()
        }
    }

    private fun requestPermissions() {
        viewModel.askedForCameraPermission = true

        val permissionsDenied = arrayOf(Manifest.permission.CAMERA).filter {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it).not()
        }

        if (permissionsDenied.isNotEmpty()) {
            showPermissionDeniedDialog()
            return
        }

        activityResultLauncher.launch(CAMERA_PERMISSION)
    }

    override fun onStop() {
        super.onStop()
        viewModel.turnOffFlashlight()
    }

    private fun showPermissionDeniedDialog() {
        if (cameraPermissionRequiredDialog == null && context != null) {
            cameraPermissionRequiredDialog = DialogUtils.cameraPermissionRequiredDialog(
                context = requireContext(),
                onPositiveClick = { goToSetting() },
                onNegativeClick = {
                    viewModel.setCameraPermissionGranted(false)
                }
            )
        }
        if (cameraPermissionRequiredDialog != null && cameraPermissionRequiredDialog?.isShowing == false) {
            cameraPermissionRequiredDialog?.show()
        }
    }

    private fun updateFlashlightStatus(enable: Boolean) {
        if (!deviceHasFlashlight()) return
        cameraController?.enableTorch(enable)
    }

    override fun onStart() {
        super.onStart()
        if (
            viewModel.isCameraPermissionGranted()
            && viewModel.goToSettingToGrantCameraPermission
            && (cameraController?.isRecording == null || cameraController?.isRecording == false)
        ) {
            viewModel.goToSettingToGrantCameraPermission = false
            viewModel.setCameraPermissionGranted(true)
            startCamera()
        }
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
        cameraPermissionRequiredDialog = null
        viewModel.turnOffFlashlight()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    companion object {
        val TAG: String = ScanQRCodeFragment::class.java.simpleName
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
}