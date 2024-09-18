package com.android.kotlin.familymessagingapp.screen.edit_chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.databinding.FragmentEditChatroomBinding
import com.android.kotlin.familymessagingapp.screen.chatroom.ChatRoomViewModel
import com.android.kotlin.familymessagingapp.utils.bindChatRoomImage
import com.android.kotlin.familymessagingapp.utils.bindUserAvatar
import com.google.android.material.textfield.TextInputEditText

class EditChatRoomFragment : Fragment() {

    private val viewModel: ChatRoomViewModel by activityViewModels()

    private var _binding: FragmentEditChatroomBinding? = null

    private val binding get() = _binding!!

    private var chatRoomNameTextInputEditText: TextInputEditText? = null

    private var chatRoomDescriptionTextInputEditText: TextInputEditText? = null

    private var nestedScrollView: NestedScrollView? = null

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
            }
        }

        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.chatRoom.observe(viewLifecycleOwner) {
            it?.let { chatRoom ->
                bindChatRoomImage(binding.ivChatRoomImage, chatRoom)
                chatRoomNameTextInputEditText?.setText(chatRoom.chatRoomName)
                chatRoomDescriptionTextInputEditText?.setText(chatRoom.chatRoomDescription)
            }
        }
    }

}