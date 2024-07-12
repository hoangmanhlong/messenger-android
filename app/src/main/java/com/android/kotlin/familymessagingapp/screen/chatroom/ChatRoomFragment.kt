package com.android.kotlin.familymessagingapp.screen.chatroom

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentChatRoomBinding
import com.android.kotlin.familymessagingapp.screen.message_options.MessageOptionsFragment
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatRoomFragment : Fragment() {

    private val _viewModel: ChatRoomViewModel by viewModels()

    private var _binding: FragmentChatRoomBinding? = null

    private var selectedItemAdapter: SelectedItemAdapter? = null

    private lateinit var messageOptionsFragment: MessageOptionsFragment

    //     Registers a photo picker activity launcher in multi-select mode.
//     In this example, the app lets the user select up to 5 media files.
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
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
        messageAdapter = MessageAdapter {
//            showMenu(binding.root, R.menu.menu_message)
//            openMessageOptions()
        }

        messageRecyclerview = binding.messageRecyclerview
        messageRecyclerview?.layoutManager = LinearLayoutManager(activity).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        messageRecyclerview?.adapter = messageAdapter

        selectedItemAdapter = SelectedItemAdapter {
            _viewModel.setImageUri(null)
        }

        selectedItemsRecyclerview = binding.selectedItemsRecyclerview
        selectedItemsRecyclerview?.adapter = selectedItemAdapter
//        selectedItemsRecyclerview?.visibility = View.GONE

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

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                _viewModel.setTextMessage(s.toString().trim())
                _viewModel.clearEditText(false)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { HideKeyboard.setupHideKeyboard(binding.messagesView, it) }
        getSharedData()

        _viewModel.isInputValid.observe(this.viewLifecycleOwner) {
            binding.btSendMessage.isEnabled = it
        }

        _viewModel.addMessageOberservable.observe(this.viewLifecycleOwner) {
            if(it) {
                _viewModel.messages?.observe(this.viewLifecycleOwner) {
                    binding.isMessageEmpty = it.isNullOrEmpty()
                    messageAdapter?.submitList(it) {
                        messageRecyclerview?.scrollToPosition(it.size - 1)
                    }
                    _viewModel.updateMessagesInChatRoom(it)
                }
            }
        }

        _viewModel.chatRoom.observe(this.viewLifecycleOwner) {
            it?.let {chatroom ->
                binding.chatroom = chatroom
                val messages = chatroom.messages
                binding.isMessageEmpty = messages.isNullOrEmpty()
                messageAdapter?.submitList(messages) {
                    if (!messages.isNullOrEmpty())
                        messageRecyclerview?.scrollToPosition(messages.size - 1)
                }
            }
        }

//        _viewModel.messages?.observe(this.viewLifecycleOwner) {
//            binding.isMessageEmpty = it.isNullOrEmpty()
//            messageAdapter?.submitList(it) {
//                messageRecyclerview?.scrollToPosition(it.size - 1)
//            }
//            _viewModel.updateMessagesInChatRoom(it)
//        }

        _viewModel.clearEdiText.observe(this.viewLifecycleOwner) {
            if (it) binding.etMessage.setText("")
        }

        _viewModel.selectedItems.observe(this.viewLifecycleOwner) {
            selectedItemAdapter?.submitList(it)
            selectedItemsRecyclerview?.visibility =
                if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        _viewModel.sendMessageStatus.observe(this.viewLifecycleOwner) {
            when (it) {
                SendMessageStatus.SENDING -> {
                    binding.inputView.isClickable = false
                    binding.btSendMessage.text = getString(R.string.sending)
                }

                SendMessageStatus.SUCCESS -> {
                    binding.inputView.isClickable = true
                    binding.btSendMessage.text = getString(R.string.send)
                }

                SendMessageStatus.ERROR -> {
                    binding.inputView.isClickable = true
                    binding.btSendMessage.text = getString(R.string.send)
                }
            }

        }
    }

    private fun getSharedData() {
        val chatroom = args.chatroom
        val userdata = args.userdata
        if (chatroom == null && userdata == null) {
            findNavController().navigateUp()
            return
        }

        chatroom?.let { _viewModel.setChatRoom(chatroom) }
        userdata?.let { _viewModel.setUserData(userdata) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _viewModel.removeMessageListener()
    }

    fun openMessageOptions() {
        messageOptionsFragment = MessageOptionsFragment()
        messageOptionsFragment.show(this.parentFragmentManager, MessageOptionsFragment.TAG)
    }

}