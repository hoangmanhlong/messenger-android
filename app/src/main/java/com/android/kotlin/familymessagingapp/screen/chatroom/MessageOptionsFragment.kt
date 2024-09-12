package com.android.kotlin.familymessagingapp.screen.chatroom

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.data.local.defaultEmoji
import com.android.kotlin.familymessagingapp.databinding.FragmentMessageOptionsBinding
import com.android.kotlin.familymessagingapp.utils.DeviceUtils
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.PermissionUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MessageOptionsFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG: String = MessageOptionsFragment::class.java.simpleName
        const val WRITE_STORAGE_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    private val viewModel: ChatRoomViewModel by activityViewModels()

    private var _binding: FragmentMessageOptionsBinding? = null

    private var emojiRecyclerView: RecyclerView? = null

    private var emojiAdapter: EmojiAdapter? = null

    private val binding get() = _binding!!

    private var writeStoragePermissionRequiredDialog: AlertDialog? = null

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Handle Permission granted/rejected
            if (granted) downloadFile()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageOptionsBinding.inflate(inflater, container, false)
        emojiRecyclerView = binding.emojiRecyclerView
        emojiAdapter = EmojiAdapter { emoji ->
            viewModel.updateMessageEmoji(emoji)
            dismiss()
        }
        emojiRecyclerView?.adapter = emojiAdapter
        emojiAdapter?.submitList(defaultEmoji)

        binding.copyMessageView.setOnClickListener {
            viewModel.copyMessage(requireActivity())
            dismiss()
        }

        binding.pinMessageView.setOnClickListener {
            viewModel.pinMessage()
            dismiss()
        }

        binding.deleteMessageView.setOnClickListener {
            viewModel.deleteMessage()
            dismiss()
        }

        binding.replyMessageView.setOnClickListener {
            viewModel.setReplyingMessage(true)
            dismiss()
        }

        binding.downloadFile.setOnClickListener {
            checkPermission()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedMessage.observe(viewLifecycleOwner) {
            if (it == null) {
                dismiss()
                return@observe
            } else {
                binding.isMessageOfMe = it.senderId == Firebase.auth.uid
                binding.downloadFile.visibility = if(it.medias.isNullOrEmpty() && viewModel.selectedMediaData.value == null) View.GONE else View.VISIBLE
                binding.copyMessageView.visibility = if (viewModel.selectedMediaData.value == null) View.VISIBLE else View.GONE
            }
        }

        binding.tvPin.text =
            if (viewModel.selectedMessageIsPinnedMessage == true)
                getString(R.string.unpin)
            else getString(R.string.pin)
    }

    private fun checkPermission() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) || writeStoragePermissionGranted()) {
            downloadFile()
        } else {
            requestPermissions()
        }
    }

    private fun writeStoragePermissionGranted(): Boolean = PermissionUtils.permissionsGranted(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun downloadFile() {
        viewModel.downloadFile()
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.goToSettingToGrantCameraPermission && writeStoragePermissionGranted()) {
            viewModel.goToSettingToGrantCameraPermission = false
            downloadFile()
        }
    }

    private fun requestPermissions() {
        val permissionsDenied = arrayOf(WRITE_STORAGE_PERMISSION).filter {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it).not()
        }

        if (permissionsDenied.isNotEmpty()) {
            showPermissionDeniedDialog()
            return
        }

        activityResultLauncher.launch(WRITE_STORAGE_PERMISSION)
    }

    private fun showPermissionDeniedDialog() {
        if (writeStoragePermissionRequiredDialog == null) {
            writeStoragePermissionRequiredDialog = DialogUtils.writeStoragePermissionRequiredDialog(
                context = requireContext(),
                onPositiveClick = { goToSetting() },
                onNegativeClick = { }
            )
        }
        writeStoragePermissionRequiredDialog?.show()
    }

    private fun goToSetting() {
        if (context == null) return
        viewModel.goToSettingToGrantCameraPermission = true
        DeviceUtils.openApplicationInfo(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        emojiRecyclerView = null
        emojiAdapter = null
        writeStoragePermissionRequiredDialog = null
    }
}