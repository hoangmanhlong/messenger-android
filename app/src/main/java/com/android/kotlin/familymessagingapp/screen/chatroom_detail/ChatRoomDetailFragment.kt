package com.android.kotlin.familymessagingapp.screen.chatroom_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentChatRoomDetailBinding
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.android.kotlin.familymessagingapp.screen.chatroom.ChatRoomViewModel
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.bindChatRoomImage


class ChatRoomDetailFragment : Fragment() {

    private val viewModel: ChatRoomViewModel by activityViewModels()

    private var _binding: FragmentChatRoomDetailBinding? = null

    private val binding get() = _binding!!

    private var leaveChatRoomDialog: AlertDialog? = null

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let {
                MediaUtils.loadImageFollowImageViewSize(
                    imageView = binding.ivChatRoomImage,
                    photo = it,
                    scaleType = ImageView.ScaleType.CENTER_CROP
                )
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

        binding.editConversationInformationView.setOnClickListener {
            findNavController().navigate(R.id.action_chatRoomDetailFragment_to_editChatRoomFragment)
        }

//        binding.ivAvatar.setOnClickListener {
//            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.chatRoom.observe(this.viewLifecycleOwner) {
            it?.let { chatRoom ->
                bindChatRoomImage(binding.ivChatRoomImage, chatRoom)
                binding.tvChatRoomName.text = chatRoom.chatRoomName
                if (chatRoom.chatRoomType == ChatRoomType.Group.type) {
                    if (chatRoom.chatRoomDescription.isNullOrEmpty()) {
                        binding.tvChatRoomDescription.visibility = View.GONE
                    } else {
                        binding.tvChatRoomDescription.visibility = View.VISIBLE
                        binding.tvChatRoomDescription.text = chatRoom.chatRoomDescription
                    }
                    val members = chatRoom.members
                    if (members.isNullOrEmpty()) {
                        binding.tvChatRoomMembers.visibility = View.GONE
                    } else {
                        binding.tvChatRoomMembers.text = getString(
                            R.string.format_chatroom_members,
                            members.size.toString()
                        )
                        binding.tvChatRoomMembers.visibility = View.VISIBLE
                    }

                    binding.editConversationInformationView.visibility = View.VISIBLE
                    binding.addMembersView.visibility = View.VISIBLE
                    binding.leaveChatRoomView.visibility = View.VISIBLE
                    binding.blockView.visibility = View.GONE
                    binding.chatRoomMembersView.visibility = View.VISIBLE
                } else {
                    binding.tvChatRoomDescription.visibility = View.GONE
                    binding.editConversationInformationView.visibility = View.GONE
                    binding.addMembersView.visibility = View.GONE
                    binding.leaveChatRoomView.visibility = View.GONE
                    binding.blockView.visibility = View.VISIBLE
                    binding.chatRoomMembersView.visibility = View.GONE
                }
            }
        }
    }
}