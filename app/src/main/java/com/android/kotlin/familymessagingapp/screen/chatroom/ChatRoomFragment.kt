package com.android.kotlin.familymessagingapp.screen.chatroom

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentChatRoomBinding
import com.android.kotlin.familymessagingapp.model.CountExceededException
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.ObjectAlreadyExistException
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.screen.Screen
import com.android.kotlin.familymessagingapp.screen.profile_detail.AppOpenMultipleDocuments
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.DeviceUtils
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.bindNormalImage
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
class ChatRoomFragment : Fragment() {

    companion object {
        val TAG: String = ChatRoomFragment::class.java.simpleName
        val CAMERA_PERMISSION = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
    }

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    private val _viewModel: ChatRoomViewModel by activityViewModels()

    private var _binding: FragmentChatRoomBinding? = null

    private var selectedItemAdapter: SelectedItemAdapter? = null

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { list ->
            if (binding.etMessage.hasFocus()) {
                binding.etMessage.clearFocus()
                hideKeyboard()
            }
            if (context != null && !list.isNullOrEmpty()) {
                _viewModel.addSelectedItems(requireContext(), list)
//                } else {
//                    Snackbar.make(
//                        requireContext(),
//                        binding.inputViewContainer,
//                        requireContext().getString(
//                            R.string.file_size_exceed_limit,
//                            Constant.MAXIMUM_FILE_SIZE_MB.toString()
//                        ),
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                }
            }
        }

    private val openDocument = registerForActivityResult(AppOpenMultipleDocuments()) {
        if (it == null && context != null) return@registerForActivityResult
        _viewModel.addSelectedItems(requireContext(), it)
    }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // Photo taken and saved successfully
                _viewModel.addUriOfTheImageBeingCapturedByTheCameraInSelectedItems()
            } else {
                // User cancelled or error occurred
                if (context != null) {
                    // Delete Uri if photo is not taken
                    _viewModel.deleteUriOfTheImageBeingCapturedByTheCamera(requireContext())
                }
            }
        }

    private val cameraLaunch =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in CAMERA_PERMISSION && !it.value)
                    permissionGranted = false
            }
            if (permissionGranted) takePhoto()
        }

    private val binding get() = _binding!!

    private val args: ChatRoomFragmentArgs by navArgs()

    private var messageAdapter: MessageAdapter? = null

    private var messageRecyclerview: RecyclerView? = null

    private var selectedItemsRecyclerview: RecyclerView? = null

    private var pinnedMessageAdapter: PinnedMessageAdapter? = null

    private var pinnedMessageRecyclerview: RecyclerView? = null

    private var sendOptionsFragment: SendOptionsFragment? = null

    private var cameraPermissionRequiredDialog: AlertDialog? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)

        messageRecyclerview = binding.messageRecyclerview
        (messageRecyclerview?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        val messageLayoutManager = LinearLayoutManager(activity).apply {
            stackFromEnd = true
//            reverseLayout = false
        }
        messageRecyclerview?.layoutManager = messageLayoutManager

        messageAdapter = MessageAdapter(
            onMessageContentViewClick = {
                hideKeyboard()
                _viewModel.hideEmojiPicker()
            },
            onMessageLongClick = { isSender, message ->
                _viewModel.setSelectedMessage(message)
                openMessageOptions()
            },
            onImageMessageClick = { drawable, message ->
                hideKeyboard()
                _viewModel.setImageDetailShown(true, drawable)
            },
            onReplyMessageClick = {
                if (messageRecyclerview != null && messageAdapter != null && it.messageId != null) {
                    messageRecyclerview!!.scrollToPosition(messageAdapter!!.getPositionById(it.messageId))
                }
            }
        )

        pinnedMessageRecyclerview = binding.pinnedMessageRecyclerview
        (pinnedMessageRecyclerview?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false

        // Initialize your pinned message adapter with the scroll functionality
        pinnedMessageAdapter = PinnedMessageAdapter {
            if (messageRecyclerview != null && messageAdapter != null && it.messageId != null) {
                messageRecyclerview!!.scrollToPosition(messageAdapter!!.getPositionById(it.messageId))
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

        selectedItemAdapter = SelectedItemAdapter(
            onItemRemove = { _viewModel.removeItemInSelectedItems(it, context) },
            onPhotoItemClick = { _viewModel.setImageDetailShown(true, it) }
        )

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

        binding.emojiPicker.setOnEmojiPickedListener { binding.etMessage.append(it.emoji) }

        binding.ivOpenEmojiPicker.setOnClickListener { _viewModel.changeEmojiPickerVisibleStatus() }

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
                _viewModel.shareImage(requireActivity(), binding.imageDetailImageView.drawable)
            }
        }

        binding.ivExpandMorePinnedMessage.setOnClickListener {
            _viewModel.setExpandPinnedMessage()
        }

        binding.ivCloseReplyMessage.setOnClickListener {
            _viewModel.setReplyingMessage(false)
        }

        binding.messagesBlankView.setOnClickListener {
            hideKeyboard()
            _viewModel.hideEmojiPicker()
        }

        binding.btInfo.setOnClickListener {
            findNavController().navigate(Screen.ChatRoomDetail.screenId)
        }

        binding.btMoreAction.setOnClickListener {
            if (sendOptionsFragment == null) sendOptionsFragment = SendOptionsFragment()
            sendOptionsFragment?.show(childFragmentManager, SendOptionsFragment.TAG)
        }

        return binding.root
    }

    @SuppressLint("IntentReset")
    @RequiresApi(Build.VERSION_CODES.Q)
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

        messageAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                messageRecyclerview?.post {
                    if (positionStart > 0) {
                        messageAdapter?.notifyItemChanged(positionStart - 1)
                    }
                    if (positionStart < messageAdapter!!.itemCount - 1) {
                        messageAdapter?.notifyItemChanged(positionStart + 1)
                    }
                }
            }
        })

        _viewModel.isLoading.observe(this.viewLifecycleOwner) {
            binding.isLoading = it
        }

        _viewModel.imageDetailShown.observe(this.viewLifecycleOwner) {
            binding.imageDetailShown = it
            if (it) binding.imageDetailImageView.setImageDrawable(_viewModel.imageMessageDrawable)
            else binding.imageDetailImageView.resetZoom()
        }

        _viewModel.replying.observe(this.viewLifecycleOwner) {
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
                messageAdapter?.setChatRoomType(chatroom.chatRoomType!!)
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
            if (it != null) {
                when (it) {
                    is SendMessageStatus.Sending -> {
                        binding.btSendMessage.isEnabled = false
                        binding.sendingProgressIndicator.visibility = View.VISIBLE
                        binding.sendingProgressIndicator.isIndeterminate = true
                    }

                    is SendMessageStatus.Success -> _viewModel.setSendMessageStatus(null)

                    is SendMessageStatus.Error -> {
                        showErrorDialog()
                        _viewModel.setSendMessageStatus(null)
                    }
                }
            } else {
                binding.sendingProgressIndicator.visibility = View.GONE
                binding.sendingProgressIndicator.isIndeterminate = false
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

        _viewModel.replyingMessage.observe(this.viewLifecycleOwner) { replyingMessage ->
            if (replyingMessage != null) {
                binding.tvSenderNameReplyMessage.text = replyingMessage.senderData?.username
                if (!replyingMessage.text.isNullOrEmpty() || !replyingMessage.photo.isNullOrEmpty()) {
                    context?.let {
                        binding.tvTextReplyMessage.text =
                            if (replyingMessage.text.isNullOrEmpty()) it.getString(R.string.sent_an_image) else replyingMessage.text
                    }
                }
                if (!replyingMessage.photo.isNullOrEmpty()) {
                    binding.replyMessageImageView.visibility = View.VISIBLE
                    bindNormalImage(binding.replyMessageImageView, replyingMessage.photo)
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

        _viewModel.openPhotoPicker.observe(this.viewLifecycleOwner) {
            if (it == true) {
                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                _viewModel.hideEmojiPicker()
                _viewModel.openPhotoPicker(false)
            }
        }

        _viewModel.openUploadFile.observe(this.viewLifecycleOwner) {
            if (it == true) {
                openDocument.launch(arrayOf("*/*"))
                _viewModel.hideEmojiPicker()
                _viewModel.openUploadFile(false)
            }
        }

        _viewModel.openTakePhoto.observe(this.viewLifecycleOwner) {
            if (it == true && context != null) takePhoto()
        }
    }

    private fun takePhoto() {
        _viewModel.openTakePhoto(false)
        if (allPermissionsGranted()) {
            openCamera(requireContext())
            _viewModel.hideEmojiPicker()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        val permissionsDenied = arrayOf(Manifest.permission.CAMERA).filter {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it).not()
        }

        if (permissionsDenied.isNotEmpty()) {
            showPermissionDeniedDialog()
            return
        }

        cameraLaunch.launch(CAMERA_PERMISSION)
    }

    private fun showPermissionDeniedDialog() {
        if (cameraPermissionRequiredDialog == null && context != null) {
            cameraPermissionRequiredDialog = DialogUtils.cameraPermissionRequiredDialog(
                context = requireContext(),
                onPositiveClick = {
                    if (cameraPermissionRequiredDialog != null) cameraPermissionRequiredDialog?.dismiss()
                    goToSetting()
                },
                onNegativeClick = {
                    if (cameraPermissionRequiredDialog != null) cameraPermissionRequiredDialog?.dismiss()
                }
            )
        }
        cameraPermissionRequiredDialog?.show()
    }

    private fun allPermissionsGranted() = CAMERA_PERMISSION.all {
        if (context == null) return false
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun openCamera(context: Context) {
        // Create Uri where to save image
        val photoUri = _viewModel.createUriOfTheImageBeingCapturedByTheCamera(context)
        if (photoUri == null) return

        // Open camera to take photo and save to created Uri
        takePicture.launch(photoUri)
    }

    private fun goToSetting() {
        if (context == null) return
        DeviceUtils.openApplicationInfo(requireContext())
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
        // If initialized for the first time then exist
        if (_viewModel.initializedForTheFirstTime) return

        val chatroom = args.chatroom
        val userdata = args.userdata
        val isOpenFromNotification = args.isOpenFromNotification
        if (isOpenFromNotification) _viewModel.resetIsOpenFromNotificationFlag()
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
        messageAdapter = null
        messageRecyclerview = null
        selectedItemAdapter = null
        selectedItemsRecyclerview = null
        pinnedMessageAdapter = null
        pinnedMessageRecyclerview = null
        sendOptionsFragment = null
        cameraPermissionRequiredDialog = null
        _viewModel.setPinMessageStatus(null)
//        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
//        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)
    }

    override fun onDestroy() {
        super.onDestroy()
        val currentDestinationId = findNavController().currentDestination?.id
        // Since ChatRoomViewModel is managed by activity, data needs to be reset when ChatRoomFragment or ChatRoomDetail Destroy
        if (currentDestinationId != Screen.ChatRoomDetail.screenId && currentDestinationId != Screen.ChatRoom.screenId) {
            _viewModel.resetState()
        }
    }

    private fun openMessageOptions() {
        MessageOptionsFragment().show(this.parentFragmentManager, MessageOptionsFragment.TAG)
    }

    private fun blockScreenCapture() {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
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