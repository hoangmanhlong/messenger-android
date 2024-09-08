package com.android.kotlin.familymessagingapp.screen.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.android.kotlin.familymessagingapp.databinding.FragmentSendOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SendOptionsFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG: String = SendOptionsFragment::class.java.simpleName
    }

    private val viewModel: ChatRoomViewModel by activityViewModels()

    private var _binding: FragmentSendOptionsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendOptionsBinding.inflate(inflater, container, false)

        binding.selectPhotoView.setOnClickListener {
            viewModel.openPhotoPicker(true)
            dismiss()
        }

        binding.cameraView.setOnClickListener {
            viewModel.openTakePhoto(true)
            dismiss()
        }

        binding.uploadFileView.setOnClickListener {
            viewModel.openUploadFile(true)
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}