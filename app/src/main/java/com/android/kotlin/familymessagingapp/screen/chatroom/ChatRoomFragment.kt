package com.android.kotlin.familymessagingapp.screen.chatroom

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.adapter.bindNormalImage
import com.android.kotlin.familymessagingapp.databinding.FragmentChatRoomBinding
import com.android.kotlin.familymessagingapp.model.CountExceededException
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.ObjectAlreadyExistException
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.DeviceUtils
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.google.android.material.snackbar.Snackbar
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
class ChatRoomFragment : Fragment(), MessageOptionsEventListener {

    companion object {
        val TAG: String = ChatRoomFragment::class.java.simpleName
    }

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    private val _viewModel: ChatRoomViewModel by viewModels()

    private var _binding: FragmentChatRoomBinding? = null

    private var selectedItemAdapter: SelectedItemAdapter? = null

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

    private var pinnedMessageAdapter: PinnedMessageAdapter? = null

    private var pinnedMessageRecyclerview: RecyclerView? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)

        messageRecyclerview = binding.messageRecyclerview
        (messageRecyclerview?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        messageRecyclerview?.layoutManager = LinearLayoutManager(activity).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        messageAdapter = MessageAdapter(
            onMessageContentViewClick = {
                hideKeyboard()
                _viewModel.hideEmojiPicker()
            },
            onMessageLongClick = { isSender, message ->
                _viewModel.setSelectedMessage(message)
                openMessageOptions(isSender, _viewModel.selectedMessageIsPinnedMessage!!)
            },
            onImageMessageClick = { drawable, message ->
                hideKeyboard()
                _viewModel.setImageDetailShown(true, drawable)
            }
        )

        pinnedMessageRecyclerview = binding.pinnedMessageRecyclerview
        pinnedMessageAdapter = PinnedMessageAdapter {
            if (messageRecyclerview != null && messageAdapter != null && it.messageId != null) {
                messageRecyclerview!!.smoothScrollToPosition(messageAdapter!!.getPositionById(it.messageId))
            }
        }
        pinnedMessageRecyclerview?.adapter = pinnedMessageAdapter

        binding.btDownloadImage.setOnClickListener {
            _viewModel.saveImageToDeviceStorage()
        }

        binding.btCloseImageDetail.setOnClickListener {
            _viewModel.setImageDetailShown(false, null)
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

        binding.btSelectPhoto.setOnClickListener {
            // Launch the photo picker and let the user choose only images.
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            _viewModel.hideEmojiPicker()
        }

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                _viewModel.setSelectPhotoVisibleStatus(s.isNullOrBlank())
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
            hideKeyboard()
            _viewModel.hideEmojiPicker()
        }

        binding.btShare.setOnClickListener {
            activity?.let {
                DeviceUtils.shareImage(requireActivity(), binding.imageDetailImageView.drawable)
            }
        }

        binding.ivExpandMorePinnedMessage.setOnClickListener {
            _viewModel.setExpandPinnedMessage()
        }

        binding.ivCloseReplyMessage.setOnClickListener {
            _viewModel.setReplyingMessage(false)
        }

        messageRecyclerview?.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getSharedData()

        // Khi nhấn nút back trên thiết bị nếu đang show Image Detail thì đóng Image Detail View
        // chứ không back về fragment trước
        activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner) {
            if (_viewModel.imageDetailShown.value == true)
                _viewModel.setImageDetailShown(false, null)
            else {
                findNavController().navigateUp()
            }
        }

        _viewModel.isLoading.observe(this.viewLifecycleOwner) {
            binding.isLoading = it
        }

        _viewModel.selectPhotoVisibleStatus.observe(this.viewLifecycleOwner) {
            binding.btSelectPhoto.visibility = if (it) View.VISIBLE else View.GONE
        }

        _viewModel.imageDetailShown.observe(this.viewLifecycleOwner) {
            binding.imageDetailShown = it
            if (it) binding.imageDetailImageView.setImageDrawable(_viewModel.imageMessageDrawable)
        }

        _viewModel.replyingMessage.observe(this.viewLifecycleOwner) {
            binding.replyingMessage = it
        }

        _viewModel.pinnedMessages.observe(this.viewLifecycleOwner) {

            // Show pinned message view nếu có có tin nhắn
            binding.pinnedMessageContainer.visibility =
                if (it.isNullOrEmpty()) View.GONE else View.VISIBLE
            // Nếu có nhiều hơn 1 tin nhắn thì show view xem thêm tin nhắn
            binding.ivExpandMorePinnedMessage.visibility =
                if (it.size > 1) View.VISIBLE else View.GONE
            // Nếu đang ở chế độ mở rộng thì show toàn bộ tin nhắn ngược lại chỉ show 1 tin nhắn
            pinnedMessageAdapter?.submitList(
                if (_viewModel.isExpandPinnedMessage.value == true) it else it.take(
                    1
                )
            )
        }

        _viewModel.isExpandPinnedMessage.observe(this.viewLifecycleOwner) {
            // Cập nhật trạng thái icon mợ rộng hoặc ẩn bớt
            binding.ivExpandMorePinnedMessage.setImageResource(if (it) R.drawable.ic_expand_less else R.drawable.ic_outline_expand_more)
            binding.isExpandMore = it

            // Nếu đang ở chế độ mở rộng thì show toàn bộ tin nhắn ngược lại chỉ show 1 tin nhắn
            val pinnedMessages = _viewModel.pinnedMessages.value
            if (!pinnedMessages.isNullOrEmpty()) {
                pinnedMessageAdapter?.submitList(if (it) pinnedMessages else pinnedMessages.take(1))
            }
        }

        _viewModel.emojiPickerVisible.observe(this.viewLifecycleOwner) {
            if (it) hideKeyboard()
            binding.isEmojiPickerVisible = it
            binding.ivOpenEmojiPicker.setImageResource(if (it) R.drawable.ic_emoji_filled else R.drawable.ic_mood)
        }

        _viewModel.isInputValid.observe(this.viewLifecycleOwner) {
            binding.btSendMessage.isEnabled = it
            if (_viewModel.sendMessageStatus.value == SendMessageStatus.Sending)
                binding.btSendMessage.isEnabled = false
        }

        _viewModel.AICreating.observe(this.viewLifecycleOwner) {
            binding.aiCreating = it
        }

        _viewModel.chatRoom.observe(this.viewLifecycleOwner) {
            it?.let { chatroom ->
                binding.chatroom = chatroom
                bindMessages(chatroom.messages)
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
                    is SendMessageStatus.Sending -> binding.btSendMessage.isEnabled = false

                    is SendMessageStatus.Success -> _viewModel.setSendMessageStatus(null)

                    is SendMessageStatus.Error -> {
                        showErrorDialog()
                        _viewModel.setSendMessageStatus(null)
                    }
                }
            }
        }

        _viewModel.pinMessageStatus.observe(this.viewLifecycleOwner) { status ->
            status?.let {
                var message = R.string.error_occurred
                when (status) {
                    is Result.Error -> {
                        if (status.exception is CountExceededException) {
                            message = R.string.max_pin_message_warning
                        }
                        if (status.exception is ObjectAlreadyExistException) {
                            message = R.string.message_has_been_pinned
                        }
                    }

                    is Result.Success -> {
                        message = R.string.pinned_successfully
                    }
                }
                Snackbar.make(
                    binding.inputView,
                    message,
                    Constant.ONE_SECOND
                ).show()
            }
        }

        _viewModel.selectedMessage.observe(this.viewLifecycleOwner) { selectedMessage ->
            if (selectedMessage != null) {
                binding.tvSenderNameReplyMessage.text = selectedMessage.senderName
                if (!selectedMessage.text.isNullOrEmpty()) {
                    context?.let {
                        binding.tvTextReplyMessage.text =
                            StringUtils.showLastMessageToChatRoomView(it, selectedMessage)
                    }
                }
                if (!selectedMessage.photo.isNullOrEmpty()) {
                    binding.replyMessageImageView.visibility = View.VISIBLE
                    bindNormalImage(binding.replyMessageImageView, selectedMessage.photo)
                } else {
                    binding.replyMessageImageView.visibility = View.GONE
                }
            }
        }

        _viewModel.saveImageState.observe(this.viewLifecycleOwner) {
            if (it != null && activity != null) {
                Toast.makeText(
                    requireActivity(),
                    if (it) R.string.photo_saved else R.string.save_photo_fail,
                    Toast.LENGTH_SHORT
                )
                    .show()
                _viewModel.setSavingImageState(null)
            }
        }
    }

    private fun hideKeyboard() {
        activity?.let { KeyBoardUtils.hideSoftKeyboard(requireActivity()) }
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

    /**
     * Update message list to RecyclerView
     *
     * @param messages data from backend(Firebase)
     */
    private fun bindMessages(messages: Map<String, Message>?) {
        val list = messages?.values?.toList()
        binding.isMessageEmpty = list.isNullOrEmpty()
        val finalMessage = list?.sortedBy { message -> message.timestamp }
        messageAdapter?.submitList(finalMessage) {
            if (!finalMessage.isNullOrEmpty())
                messageRecyclerview?.scrollToPosition(finalMessage.size - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _viewModel.setPinMessageStatus(null)
//        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
//        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.hideLessPinnedMessage()
    }

    private fun openMessageOptions(isMessageOfMe: Boolean, isPinnedMessage: Boolean) {
        MessageOptionsFragment(this, isMessageOfMe, isPinnedMessage)
            .show(this.parentFragmentManager, MessageOptionsFragment.TAG)
    }

    private fun blockScreenCapture() {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    override fun onPinMessage() {
        _viewModel.pinMessage()
    }

    override fun onDeleteMessage() {
        _viewModel.deleteMessage()
    }

    override fun onCopyMessage() {
        _viewModel.copyMessage(requireActivity())
    }

    override fun updateMessageEmoji(emoji: String) {
        _viewModel.updateMessageEmoji(emoji)
    }

    override fun onReplyMessage() {
        _viewModel.setReplyingMessage(true)
    }

    private fun showErrorDialog() {
        if (activity == null) {
            findNavController().navigateUp()
        } else {
            DialogUtils.showNotificationDialog(
                context = requireActivity(),
                message = R.string.error_occurred,
                cancelable = false,
                onOkButtonClick = { _, _ -> findNavController().navigateUp() }
            ).show()
        }
    }
}