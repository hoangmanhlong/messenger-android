package com.android.kotlin.familymessagingapp.screen.chatroom

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.FragmentChatRoomBinding
import com.android.kotlin.familymessagingapp.screen.profile_detail.MyOpenDocumentContract
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatRoomFragment : Fragment() {

    private val _viewModel: ChatRoomViewModel by viewModels()

    private var _binding: FragmentChatRoomBinding? = null

    private val openDocument: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(MyOpenDocumentContract()) {
            it?.let {
//                binding.ivAvatar.setImageURI(it)
//                _viewModel.setImageUri(it)
            }
        }

    private var selectedItemAdapter: SelectedItemAdapter? = null

    // Registers a photo picker activity launcher in multi-select mode.
// In this example, the app lets the user select up to 5 media files.
    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        _viewModel.setImageUri(uri)
    }

    private val binding get() = _binding!!

    private val args: ChatRoomFragmentArgs by navArgs()

    private var messageAdapter: MessageAdapter? = null

    private var messageRecyclerview: RecyclerView? = null

    private var selectedItemsRecyclerview: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        messageAdapter = MessageAdapter()
        messageRecyclerview = binding.messageRecyclerview
        messageRecyclerview?.adapter = messageAdapter

        selectedItemAdapter = SelectedItemAdapter {
            _viewModel.setImageUri(null)
        }

        selectedItemsRecyclerview = binding.selectedItemsRecyclerview
        selectedItemsRecyclerview?.adapter = selectedItemAdapter
        selectedItemsRecyclerview?.visibility = View.GONE

        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }
        binding.btSendMessage.setOnClickListener {
            activity?.let {
                NetworkChecker.checkNetwork(it) {
                    _viewModel.sendMessage()
                }
            }
        }

        binding.ivLocation.setOnClickListener {

        }

        binding.btSelectPhoto.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

//        binding.btSelectPhoto.setOnClickListener {
//            openDocument.launch(arrayOf("image/*"))
//        }

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                _viewModel.setTextMessage(s.toString().trim())
                _viewModel.clearEdtText(false)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { HideKeyboard.setupHideKeyboard(binding.messagesView, it) }
        bindChatRoom()

        _viewModel.isInputValid.observe(this.viewLifecycleOwner) {
            binding.btSendMessage.isEnabled = it
        }

        _viewModel.messages?.observe(this.viewLifecycleOwner) {
            binding.isMessageEmpty = it.isNullOrEmpty()
            it?.let { messageAdapter?.submitList(it) }
            _viewModel.updateMessagesInChatRoom(it)
        }

        _viewModel.clearEdiText.observe(this.viewLifecycleOwner) {
            if (it) binding.etMessage.setText("")
        }

        _viewModel.selectedItems.observe(this.viewLifecycleOwner) {
            selectedItemAdapter?.submitList(it)
            selectedItemsRecyclerview?.visibility =
                if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun bindChatRoom() {
        val chatroom = args.chatroom
        binding.chatroom = chatroom
        val messages = chatroom.messages
        binding.isMessageEmpty = messages.isNullOrEmpty()
        messages?.let { messageAdapter?.submitList(it) }
        _viewModel.setChatRoom(chatRoom = chatroom)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _viewModel.removeMessageListener()
    }
}