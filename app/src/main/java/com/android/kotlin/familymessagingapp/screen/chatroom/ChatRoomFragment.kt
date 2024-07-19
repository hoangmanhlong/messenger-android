package com.android.kotlin.familymessagingapp.screen.chatroom

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentChatRoomBinding
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

/**
 * ChatRoom hiển thị data với 2 trường hợp
 *
 *  Case 1: ChatRoom được mở từ Danh sách chatroom ở màn Home(HomeFragment)
 *
 *      - Lấy dữ liệu chatroom được truyền từ Home kiểm tra xem có null không(99% là không null, nếu null thì back về màn Home)
 *      - Hiển thị toàn bộ dữ liệu chatroom được truyền từ Home lên màn hình
 *      - Bắt đầu trình nghe tin nhắn theo chatroom ID
 *
 *  Case 2: ChatRoom được mở khi click từ User trong tìm kiếm
 *
 *      - Trong case này chatroom sẽ null và có userdata
 *      - Lấy tên và ảnh của user đó đặt làm ảnh cho chatroom
 *      - Thực hiện kiểm tra xem chatroom có tồn tại bằng cách: Vì chatroomID được đặt theo quy ước trước đó(có 2TH user1___user2 hoặc user2___user1)
 *          + Nếu chatroom ID tồn tại: Bắt đầu trình nghe tin nhắn theo chatroom ID
 *          + Nếu không tồn tại: Danh sách tin nhắn sẽ hiển thị rỗng. Khi người dùng nhắn tin nhắn đầu tiên chatroom sẽ được tạo. Nếu chatroom được tạo thành công. Chatroom sẽ được lưu vào userdata của 2 người dùng. Bắt đầu trình nghe tin nhắn theo chatroom ID
 */
// TODO: block screen capture
@AndroidEntryPoint
class ChatRoomFragment : Fragment() {

    companion object {
        val TAG: String = ChatRoomFragment::class.java.simpleName
    }

    private val _viewModel: ChatRoomViewModel by viewModels()

    private var _binding: FragmentChatRoomBinding? = null

    private var selectedItemAdapter: SelectedItemAdapter? = null

    private lateinit var messageOptionsFragment: MessageOptionsFragment

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            _viewModel.setImageUri(uri)
            if (Lifecycle.State.STARTED == lifecycle.currentState) {
                if (binding.etMessage.hasFocus()) {
                    KeyBoardUtils.showKeyboard(binding.etMessage)
                }
            }
        }

    private val binding get() = _binding!!

    private val args: ChatRoomFragmentArgs by navArgs()

    private var messageAdapter: MessageAdapter? = null

    private var messageRecyclerview: RecyclerView? = null

    private var selectedItemsRecyclerview: RecyclerView? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)

//        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        messageRecyclerview = binding.messageRecyclerview
        (messageRecyclerview?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        messageRecyclerview?.layoutManager = LinearLayoutManager(activity).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        messageAdapter = MessageAdapter(
            onMessageContentViewClick = {
                if (messageRecyclerview != null) KeyBoardUtils.hideKeyboard(messageRecyclerview!!)
            },
            onTextMessageClick = { isSender, message ->
                _viewModel.setSelectedMessage(message)
                openMessageOptions(isSender)
            }
        )

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

        binding.btSelectPhoto.setOnClickListener {
            // Launch the photo picker and let the user choose only images.
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            _viewModel.hideEmojiPicker()
        }

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: " + s.toString().trim())
                _viewModel.setTextMessage(s.toString().trim())
                _viewModel.clearEditText(false)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })

        binding.emojiPicker.setOnEmojiPickedListener { binding.etMessage.append(it.emoji) }

        binding.ivOpenEmojiPicker.setOnClickListener { _viewModel.changeEmojiPickerVisibleStatus() }

//        binding.messagesView.setOnClickListener {
//            _viewModel.hideEmojiPicker()
//        }

//        binding.btVideoCall.setOnClickListener {
//            val action = ChatRoomFragmentDirections
//                .actionChatRoomFragmentToCallFragment(_viewModel.chatRoom.value)
//            findNavController().navigate(action)
//        }

        binding.aiGenerateView.setOnClickListener {
            binding.etMessage.text =
                Editable.Factory.getInstance().newEditable(binding.tvAItext.text)
            _viewModel.setAIGeneratedText(null)
        }

        binding.tvCloseGeneratedText.setOnClickListener {
            _viewModel.setAIGeneratedText(null)
        }

        binding.etMessage.setOnTouchListener { _, _ ->
            _viewModel.hideEmojiPicker()
            false
        }

        binding.messagesView.setOnClickListener {
            KeyBoardUtils.hideKeyboard(binding.messagesView)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        activity?.let { KeyBoardUtils.setupHideKeyboard(binding.messagesView, it) }
        getSharedData()

        _viewModel.isLoading.observe(this.viewLifecycleOwner) {
            binding.isLoading = it
        }

        _viewModel.emojiPickerVisible.observe(this.viewLifecycleOwner) {
            if (it) KeyBoardUtils.hideKeyboard(view)
            binding.isEmojiPickerVisible = it
            binding.ivOpenEmojiPicker.setImageResource(if (it) R.drawable.ic_emoji_filled else R.drawable.ic_mood)
        }

        _viewModel.isInputValid.observe(this.viewLifecycleOwner) {
            binding.btSendMessage.isEnabled = it
            if (_viewModel.sendMessageStatus.value == SendMessageStatus.SENDING)
                binding.btSendMessage.isEnabled = false
        }

        _viewModel.AICreating.observe(this.viewLifecycleOwner) {
            binding.aiCreating = it
        }

        _viewModel.startObservingMessages.observe(this.viewLifecycleOwner) {
            if (it) {
                _viewModel.messages.observe(this.viewLifecycleOwner) { messages ->
                    binding.isMessageEmpty = messages.isNullOrEmpty()
                    messageAdapter?.submitList(messages) {
                        messageRecyclerview?.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        _viewModel.chatRoom.observe(this.viewLifecycleOwner) {
            it?.let { chatroom ->
                binding.chatroom = chatroom
                val messages = chatroom.messages
                binding.isMessageEmpty = messages.isNullOrEmpty()
                messageAdapter?.submitList(messages) {
                    if (!messages.isNullOrEmpty())
                        messageRecyclerview?.scrollToPosition(messages.size - 1)
                }
            }
        }

        _viewModel.clearEdiText.observe(this.viewLifecycleOwner) {
            if (it) binding.etMessage.text = null
        }

        _viewModel.selectedItems.observe(this.viewLifecycleOwner) {
            selectedItemAdapter?.submitList(it)
            selectedItemsRecyclerview?.visibility =
                if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        _viewModel.AIGeneratedText.observe(this.viewLifecycleOwner) {
            binding.hasAiText = !it.isNullOrEmpty()
            binding.generatedText = it
        }

        _viewModel.sendMessageStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                when (it) {
                    SendMessageStatus.SENDING -> {
//                        binding.btSendMessage.text = getString(R.string.sending)
                        binding.btSendMessage.isEnabled = false
                    }

                    SendMessageStatus.SUCCESS -> {
//                        binding.btSendMessage.text = getString(R.string.send)
//                        binding.btSendMessage.isEnabled = true
                        _viewModel.setSendMessageStatus(null)
                    }

                    SendMessageStatus.ERROR -> {
//                        binding.btSendMessage.text = getString(R.string.send)
//                        binding.btSendMessage.isEnabled = true
                        _viewModel.setSendMessageStatus(null)
                    }
                }
            }
        }
    }

    /**
     * Get data passed from HomeFragment
     *
     * If both chatroom and userdata is null, back to HomeFragment
     *
     * Else set chatroom and userdata to viewModel
     */
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
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
//        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)
    }

    private fun openMessageOptions(isMessageOfMe: Boolean) {
        messageOptionsFragment = MessageOptionsFragment(this@ChatRoomFragment, isMessageOfMe)
        messageOptionsFragment.show(this.parentFragmentManager, MessageOptionsFragment.TAG)
    }

    private fun blockScreenCapture() {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun copyMessage() {
        if (activity != null && _viewModel.selectedMessage?.text != null) {
            KeyBoardUtils.copyTextToClipBoard(
                requireActivity(),
                _viewModel.selectedMessage!!.text!!
            )
        }
    }
}