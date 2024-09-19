package com.android.kotlin.familymessagingapp.screen.edit_chatroom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainViewModel
import com.android.kotlin.familymessagingapp.databinding.FragmentEditChatroomBinding
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.screen.chatroom.ChatRoomViewModel
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditChatRoomFragment : Fragment() {

    companion object {
        val TAG: String = EditChatRoomFragment::class.java.simpleName
    }

    private val viewModel: ChatRoomViewModel by activityViewModels()

    private val editChatRoomViewModel: EditChatRoomViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentEditChatroomBinding? = null

    private val binding get() = _binding!!

    private var chatRoomNameTextInputEditText: TextInputEditText? = null

    private var chatRoomDescriptionTextInputEditText: TextInputEditText? = null

    private var nestedScrollView: NestedScrollView? = null

    private var errorDialog: AlertDialog? = null

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let {
                MediaUtils.loadImageFollowImageViewSize(
                    imageView = binding.ivChatRoomImage,
                    photo = it,
                    scaleType = ImageView.ScaleType.CENTER_CROP
                )
                editChatRoomViewModel.updateChatRoomImage(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditChatroomBinding.inflate(inflater, container, false)
        chatRoomNameTextInputEditText = binding.chatRoomNameTextInputEditText
        chatRoomDescriptionTextInputEditText = binding.chatRoomDescriptionTextInputEditText
        nestedScrollView = binding.nestedScrollView

        chatRoomDescriptionTextInputEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // ScrollView will scroll to the bottom when focused
                nestedScrollView?.post {
                    nestedScrollView?.smoothScrollTo(0, nestedScrollView!!.bottom)
                }
                chatRoomDescriptionTextInputEditText?.setSelection(
                    chatRoomDescriptionTextInputEditText?.text?.length ?: 0
                )
            }
        }

        chatRoomNameTextInputEditText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                chatRoomNameTextInputEditText?.setSelection(
                    chatRoomNameTextInputEditText?.text?.length ?: 0
                )
            }
        }

        binding.chatRoomImageCardView.setOnClickListener {
            pickMultipleMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

        binding.btSave.setOnClickListener {
            editChatRoomViewModel.updateChatRoomData()
        }

        chatRoomNameTextInputEditText?.addTextChangedListener {
            editChatRoomViewModel.updateChatRoomName(it.toString().trim())
        }

        chatRoomDescriptionTextInputEditText?.addTextChangedListener {
            editChatRoomViewModel.updateChatRoomDescription(it.toString().trim())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.chatRoom.observe(viewLifecycleOwner) { chatRoom ->
            editChatRoomViewModel.updateChatRoom(chatRoom)
            if (chatRoom == null || chatRoom.chatRoomId.isNullOrEmpty()) {
                showErrorDialog(true)
                return@observe
            }

            val chatRoomName = editChatRoomViewModel.draftChatRoom.chatRoomName
            val chatRoomDescription = editChatRoomViewModel.draftChatRoom.chatRoomDescription
            val chatRoomImage = editChatRoomViewModel.draftChatRoom.chatRoomImage
            val image =
                if (chatRoomImage.isNullOrEmpty()) chatRoom.chatRoomImage else chatRoomImage.toUri()
            MediaUtils.loadImageFollowImageViewSize(
                imageView = binding.ivChatRoomImage,
                photo = image,
                scaleType = ImageView.ScaleType.CENTER_CROP
            )
            chatRoomNameTextInputEditText?.setText(
                if (editChatRoomViewModel.initializedForTheFirstTime) chatRoom.chatRoomName else chatRoomName
            )
            chatRoomDescriptionTextInputEditText?.setText(
                if (editChatRoomViewModel.initializedForTheFirstTime) chatRoom.chatRoomDescription else chatRoomDescription
            )

        }

        editChatRoomViewModel.saveButtonStatus.observe(viewLifecycleOwner) {
            binding.btSave.isEnabled = it
        }

        editChatRoomViewModel.isLoading.observe(viewLifecycleOwner) {
            mainViewModel.setIsLoading(it)
        }

        editChatRoomViewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Result.Success -> {
                    if (chatRoomNameTextInputEditText?.hasFocus() == true) {
                        chatRoomDescriptionTextInputEditText?.clearFocus()
                    }
                    if (chatRoomDescriptionTextInputEditText?.hasFocus() == true) {
                        chatRoomDescriptionTextInputEditText?.clearFocus()
                    }
                    editChatRoomViewModel.updateSaveResult(null)
                }
                is Result.Error -> {
                    showErrorDialog()
                    editChatRoomViewModel.updateSaveResult(null)
                }
                else -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        editChatRoomViewModel.initializedForTheFirstTime = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatRoomNameTextInputEditText = null
        chatRoomDescriptionTextInputEditText = null
        nestedScrollView = null
        _binding = null
    }

    private fun showErrorDialog(isChatRoomEmpty: Boolean = false) {
        if (activity == null) return

        if (errorDialog == null) {
            errorDialog = DialogUtils.showNotificationDialog(
                context = requireActivity(),
                message = R.string.error_occurred,
                cancelable = false,
                onOkButtonClick = { _, _ ->
                    if (isChatRoomEmpty) {
                        findNavController().navigateUp()
                    } else {
                        errorDialog?.dismiss()
                    }
                }
            )
        }
        errorDialog?.show()
    }
}