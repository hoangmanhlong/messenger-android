package com.android.kotlin.familymessagingapp.screen.chatroom_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.databinding.FragmentChatRoomDetailBinding
import com.android.kotlin.familymessagingapp.screen.chatroom.ChatRoomViewModel
import com.android.kotlin.familymessagingapp.utils.DialogUtils


class ChatRoomDetailFragment : Fragment() {

    private val viewModel: ChatRoomViewModel by activityViewModels()

    private var _binding: FragmentChatRoomDetailBinding? = null

    private val binding get() = _binding!!

    private var leaveChatRoomDialog: AlertDialog? = null

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let {
                binding.ivAvatar.setImageURI(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomDetailBinding.inflate(inflater, container, false)

        binding.btNavigateUp.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.addMembersView.setOnClickListener {

        }

        binding.leaveChatRoomView.setOnClickListener {
            activity?.let {
                if (leaveChatRoomDialog == null) {
                    leaveChatRoomDialog = DialogUtils.leaveChatRoomDialog(
                        context = requireContext(),
                        onPositiveClick = {
                            viewModel.leaveChatRoom()
                        }
                    )
                }
                leaveChatRoomDialog?.show()
            }
        }

        binding.ivAvatar.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.chatRoom.observe(this.viewLifecycleOwner) {
            it?.let { chatRoom ->
                binding.chatRoom = chatRoom
            }
        }
    }

}