package com.android.kotlin.familymessagingapp.screen.chatroom

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.LayoutReceiverMessageBinding
import com.android.kotlin.familymessagingapp.databinding.LayoutSenderMessageBinding
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.android.kotlin.familymessagingapp.model.FileType
import com.android.kotlin.familymessagingapp.model.MediaData
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.Reaction
import com.android.kotlin.familymessagingapp.model.getFileDrawableRes
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.android.kotlin.familymessagingapp.utils.bindPhotoMessage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MessageAdapter(
    private val onMessageContentViewClick: () -> Unit,
    private val onImageLongClick: (Boolean, MediaData, Message) -> Unit,
    private val onImageClick: (Drawable, MediaData, Message) -> Unit,
    private val onReplyMessageClick: (Message) -> Unit,
    private val onFileLongClick: (Boolean, MediaData, Message) -> Unit,
    private val onTextMessageLongClick: (Boolean, Message) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    private lateinit var chatRoomType: String

    private val ICON_MARGIN = 16

    private val sender = 1

    private val receiver = 2

    private var expandedMessagePosition: Int? = null

    fun setChatRoomType(type: String) {
        chatRoomType = type
    }

    inner class SenderMessageViewHolder(
        private val binding: LayoutSenderMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var reactionsAdapter: ReactionsAdapter? = null

        private var fileAdapter: FileAdapter? = null

        private var imageMessageAdapter: ImageMessageAdapter? = null

        init {
            reactionsAdapter = ReactionsAdapter {

            }
            binding.reactionsRecyclerView.adapter = reactionsAdapter
        }

        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
        fun bind(message: Message) {

            if (!message.removedByIsEmpty()) {
                binding.textMessageContainer.visibility = View.GONE
                binding.imageMessageCardView.visibility = View.GONE
                binding.reactionsRecyclerView.visibility = View.GONE
                binding.replyMessageContainer.visibility = View.GONE
                updateRemovedMessageOfSenderBackground(binding, message, bindingAdapterPosition)
                binding.removedMessageView.visibility = View.VISIBLE
                binding.removedMessageView.setOnClickListener {
                    handleTextMessageClick(bindingAdapterPosition)
                }
            } else {

                var isReplyMessageShown = false

                // Check if replyMessageId or replyMessage is empty/nul
                if (message.replyMessageId.isNullOrEmpty() or (message.replyMessage == null)) {
                    binding.replyMessageContainer.visibility = View.GONE
                } else {
                    val replyMessage = message.replyMessage!!
                    val context = binding.root.context
                    val you = context.getString(R.string.sender_you)

                    // Set the title of the reply message
                    binding.tvReplyMessageTitle.text = context.getString(
                        R.string.replied_to,
                        you,
                        if (replyMessage.senderData?.uid == Firebase.auth.uid) you else replyMessage.senderData?.username
                    )

                    if (replyMessage.removedByIsEmpty()) {

                        val replyMessageText = replyMessage.text
                        val replyMessageMedia = message.repliedMediaData

                        if (!replyMessageText.isNullOrEmpty() && replyMessageMedia == null) {
                            binding.tvTextReplyMessage.text = replyMessageText
                            binding.replyMessageImageContainerView.visibility = View.GONE
                        } else if(replyMessageMedia != null && message.replyMessage?.medias?.contains(replyMessageMedia) == true) {
                            if (replyMessageMedia.type == FileType.IMAGE.value) {
                                MediaUtils.loadImageFollowImageViewSize(
                                    imageView = binding.ivReplyMessageMedia,
                                    photo = replyMessageMedia.url,
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                )
                                binding.tvTextReplyMessage.text = context.getString(R.string.photo_last_message)
                            } else {
                                val fileName = replyMessageMedia.fileName
                                binding.tvTextReplyMessage.visibility =
                                    if (fileName.isNullOrEmpty()) View.GONE else View.VISIBLE

                                binding.ivReplyMessageMedia.setImageResource(
                                    getFileDrawableRes(replyMessageMedia.type)
                                )
                                binding.tvTextReplyMessage.text = fileName
                            }
                            binding.replyMessageImageContainerView.visibility = View.VISIBLE
                        } else {
                            binding.replyMessageImageContainerView.visibility = View.GONE
                            binding.tvTextReplyMessage.text = context.getString(R.string.message_removed)
                        }
                    } else {
                        binding.tvTextReplyMessage.text = context.getString(R.string.message_removed)
                    }

                    binding.replyMessageContainer.visibility = View.VISIBLE
                    binding.replyMessageContainer.setOnClickListener {
                        onReplyMessageClick(replyMessage)
                    }
                    isReplyMessageShown = true
                }

                binding.removedMessageView.visibility = View.GONE
                val reactions = message.reactions?.entries?.map {
                    Reaction(it.key, it.value.entries.map { it.key })
                }
                binding.reactionsRecyclerView.visibility =
                    if (reactions == null) View.GONE else View.VISIBLE
                reactionsAdapter?.submitList(reactions)
                if (!message.isTextEmpty()) {
                    binding.messageText.text = message.text
                    binding.textMessageContainer.visibility = View.VISIBLE
                    binding.textMessageContainer.setOnLongClickListener {
                        onTextMessageLongClick(true, message)
                        false
                    }
                    binding.textMessageContainer.setOnClickListener {
                        handleTextMessageClick(bindingAdapterPosition)
                    }
                    updateTextMessageOfSenderBackground(
                        binding,
                        message,
                        bindingAdapterPosition,
                        isReplyMessageShown
                    )
                } else {
                    binding.textMessageContainer.visibility = View.GONE
                }

                val medias = message.medias

                if (!medias.isNullOrEmpty()) {

                    val (images, files) = message.medias.partition { it.type == FileType.IMAGE.value }

                    if (images.isNotEmpty()) {
                        if (images.size == 1) {
                            val imageMediaData = images[0]
                            binding.imageRecyclerView.visibility = View.GONE
                            bindPhotoMessage(binding.image, imageMediaData.url)
                            binding.image.visibility = View.VISIBLE
                            binding.imageMessageCardView.setOnLongClickListener {
                                onImageLongClick(true, imageMediaData, message)
                                false
                            }

                            binding.imageMessageCardView.setOnClickListener {
                                onImageClick(binding.image.drawable, imageMediaData, message)
                            }

                        } else {
                            binding.image.visibility = View.GONE
                            if (imageMessageAdapter == null) {
                                imageMessageAdapter = ImageMessageAdapter(
                                    onImageLongClick = { onImageLongClick(true, it, message) },
                                    onImageClick = { drawable, mediaData ->
                                        onImageClick(drawable, mediaData, message)
                                    }
                                )

                                binding.imageRecyclerView.adapter = imageMessageAdapter
                            }
                            imageMessageAdapter?.submitList(images)
                            binding.imageRecyclerView.visibility = View.VISIBLE
                        }
                        binding.imageMessageCardView.visibility = View.VISIBLE
                    } else {
                        binding.imageRecyclerView.visibility = View.GONE
                        binding.imageMessageCardView.visibility = View.GONE
                    }

                    if (files.isNotEmpty()) {
                        if (fileAdapter == null) {
                            fileAdapter = FileAdapter(
                                isSender = true,
                                onFileLongClick = {
                                    onFileLongClick(true, it, message)
                                },
                                onFileContainerClick = {
                                    onMessageContentViewClick()
                                }
                            )
                            binding.fileRecyclerView.adapter = fileAdapter
                        }
                        fileAdapter?.submitList(files)
                        binding.fileRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.fileRecyclerView.visibility = View.GONE
                    }

                } else {
                    binding.fileRecyclerView.visibility = View.GONE
                    binding.imageMessageCardView.visibility = View.GONE
                    binding.imageRecyclerView.visibility = View.GONE
                }
            }

            binding.tvMessageTime.text = StringUtils.formatTime(
                binding.tvMessageTime.context,
                message.timestamp!!,
                false
            )
            binding.tvMessageTime.visibility =
                if (bindingAdapterPosition == expandedMessagePosition) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateTextMessageOfSenderBackground(
        binding: LayoutSenderMessageBinding,
        message: Message,
        bindingAdapterPosition: Int,
        isReplyMessageShown: Boolean
    ) {
        // Get the previous and next messages
        val previousMessage =
            if (bindingAdapterPosition > 0) getItem(bindingAdapterPosition - 1) else null
        val nextMessage =
            if (bindingAdapterPosition < itemCount - 1) getItem(bindingAdapterPosition + 1) else null

        // Determine if previous and next messages are from the same sender
        val isPreviousSameSender = previousMessage?.senderId == message.senderId
        val isNextSameSender = nextMessage?.senderId == message.senderId

        var needToCheckBackgroundUpdateIfThereIsReplyMessage = false

        var backgroundResId = when {
            // First item in a series from the receiver
            !isPreviousSameSender && isNextSameSender -> {
                needToCheckBackgroundUpdateIfThereIsReplyMessage = true
                R.drawable.bg_sender_message_first
            }

            // Middle item in a series from the receiver
            isPreviousSameSender && isNextSameSender -> R.drawable.bg_sender_message_middle

            // Last item in a series from the receiver
            isPreviousSameSender && !isNextSameSender -> R.drawable.bg_sender_message_last

            // Single isolated item
            !isPreviousSameSender && !isNextSameSender -> {
                needToCheckBackgroundUpdateIfThereIsReplyMessage = true
                R.drawable.bg_sender_message_single
            }

            else -> { // Fallback
                needToCheckBackgroundUpdateIfThereIsReplyMessage = true
                R.drawable.bg_sender_message_single
            }
        }

        if (needToCheckBackgroundUpdateIfThereIsReplyMessage && isReplyMessageShown) {
            backgroundResId = R.drawable.bg_sender_message_last
        }

        // Apply the determined background drawable
        binding.textMessageContainer.background = binding
            .textMessageContainer
            .context
            .getDrawable(backgroundResId)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateRemovedMessageOfSenderBackground(
        binding: LayoutSenderMessageBinding,
        message: Message,
        bindingAdapterPosition: Int
    ) {
        // Get the previous and next messages
        val previousMessage =
            if (bindingAdapterPosition > 0) getItem(bindingAdapterPosition - 1) else null
        val nextMessage =
            if (bindingAdapterPosition < itemCount - 1) getItem(bindingAdapterPosition + 1) else null

        // Determine if previous and next messages are from the same sender
        val isPreviousSameSender = previousMessage?.senderId == message.senderId
        val isNextSameSender = nextMessage?.senderId == message.senderId

        val backgroundResId = when {
            // First item in a series from the receiver
            !isPreviousSameSender && isNextSameSender -> R.drawable.bg_removed_message_first_of_sender

            // Middle item in a series from the receiver
            isPreviousSameSender && isNextSameSender -> R.drawable.bg_removed_message_middle_of_sender

            // Last item in a series from the receiver
            isPreviousSameSender && !isNextSameSender -> R.drawable.bg_removed_message_last_of_sender

            // Single isolated item
            !isPreviousSameSender && !isNextSameSender -> R.drawable.bg_removed_message_single

            else -> R.drawable.bg_removed_message_single // Fallback
        }

        // Apply the determined background drawable
        binding.removedMessageView.background = binding
            .textMessageContainer
            .context
            .getDrawable(backgroundResId)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateRemovedMessageOfReceiverBackground(
        binding: LayoutReceiverMessageBinding,
        message: Message,
        bindingAdapterPosition: Int
    ) {
        // Get the previous and next messages
        val previousMessage =
            if (bindingAdapterPosition > 0) getItem(bindingAdapterPosition - 1) else null
        val nextMessage =
            if (bindingAdapterPosition < itemCount - 1) getItem(bindingAdapterPosition + 1) else null

        // Determine if previous and next messages are from the same sender
        val isPreviousSameSender = previousMessage?.senderId == message.senderId
        val isNextSameSender = nextMessage?.senderId == message.senderId

        val backgroundResId = when {
            // First item in a series from the receiver
            !isPreviousSameSender && isNextSameSender -> R.drawable.bg_removed_message_first_of_receiver

            // Middle item in a series from the receiver
            isPreviousSameSender && isNextSameSender -> R.drawable.bg_removed_message_middle_of_receiver

            // Last item in a series from the receiver
            isPreviousSameSender && !isNextSameSender -> R.drawable.bg_removed_message_last_of_receiver

            // Single isolated item
            !isPreviousSameSender && !isNextSameSender -> R.drawable.bg_removed_message_single

            else -> R.drawable.bg_removed_message_single // Fallback
        }

        // Apply the determined background drawable
        binding.removedMessageView.background = binding
            .textMessageContainer
            .context
            .getDrawable(backgroundResId)
    }

    inner class ReceiverMessageViewHolder(
        private val binding: LayoutReceiverMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var reactionsAdapter: ReactionsAdapter? = null

        private var fileAdapter: FileAdapter? = null

        private var imageMessageAdapter: ImageMessageAdapter? = null

        init {
            reactionsAdapter = ReactionsAdapter {

            }
            binding.reactionsRecyclerView.adapter = reactionsAdapter
        }

        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
        fun bind(message: Message, isFirstReceiverMessage: Boolean) {

            updateVisibilityReceiverNameAndImageMessage(binding, message, isFirstReceiverMessage)

            if (!message.removedByIsEmpty()) {
                binding.textMessageContainer.visibility = View.GONE
                binding.imageMessageCardView.visibility = View.GONE
                binding.reactionsRecyclerView.visibility = View.GONE
                binding.replyMessageContainer.visibility = View.GONE
                updateRemovedMessageOfReceiverBackground(binding, message, bindingAdapterPosition)
                binding.removedMessageView.visibility = View.VISIBLE
                binding.removedMessageView.setOnClickListener {
                    handleTextMessageClick(bindingAdapterPosition)
                }
            } else {

                var isReplyMessageShown = false

                // Check if replyMessageId or replyMessage is empty/nul
                if (message.replyMessageId.isNullOrEmpty() or (message.replyMessage == null)) {
                    binding.replyMessageContainer.visibility = View.GONE
                } else {
                    val replyMessage = message.replyMessage!!
                    val context = binding.root.context
                    val you = context.getString(R.string.sender_you)

                    // Set the title of the reply message
                    binding.tvReplyMessageTitle.text = context.getString(
                        R.string.replied_to,
                        if (message.senderData?.uid == Firebase.auth.uid) you else message.senderData?.username,
                        if (replyMessage.senderData?.uid == Firebase.auth.uid) you else replyMessage.senderData?.username
                    )

                    if (replyMessage.removedByIsEmpty()) {

                        val replyMessageText = replyMessage.text
                        val replyMessageMedia = message.repliedMediaData

                        if (!replyMessageText.isNullOrEmpty() && replyMessageMedia == null) {
                            binding.tvTextReplyMessage.text = replyMessageText
                            binding.replyMessageImageContainerView.visibility = View.GONE
                        } else if(replyMessageMedia != null && message.replyMessage?.medias?.contains(replyMessageMedia) == true) {
                            if (replyMessageMedia.type == FileType.IMAGE.value) {
                                MediaUtils.loadImageFollowImageViewSize(
                                    imageView = binding.ivReplyMessageMedia,
                                    photo = replyMessageMedia.url,
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                )
                                binding.tvTextReplyMessage.text = context.getString(R.string.photo_last_message)
                            } else {
                                val fileName = replyMessageMedia.fileName
                                binding.tvTextReplyMessage.visibility =
                                    if (fileName.isNullOrEmpty()) View.GONE else View.VISIBLE

                                binding.ivReplyMessageMedia.setImageResource(
                                    getFileDrawableRes(replyMessageMedia.type)
                                )
                                binding.tvTextReplyMessage.text = fileName
                            }
                            binding.replyMessageImageContainerView.visibility = View.VISIBLE
                        } else {
                            binding.replyMessageImageContainerView.visibility = View.GONE
                            binding.tvTextReplyMessage.text = context.getString(R.string.message_removed)
                        }
                    } else {
                        binding.tvTextReplyMessage.text = context.getString(R.string.message_removed)
                    }

                    binding.replyMessageContainer.visibility = View.VISIBLE
                    binding.replyMessageContainer.setOnClickListener {
                        onReplyMessageClick(replyMessage)
                    }
                    isReplyMessageShown = true
                }

                binding.removedMessageView.visibility = View.GONE
                val reactions = message.reactions?.entries?.map {
                    Reaction(it.key, it.value.entries.map { it.key })
                }
                binding.reactionsRecyclerView.visibility =
                    if (reactions == null) View.GONE else View.VISIBLE
                reactionsAdapter?.submitList(reactions)

                if (!message.isTextEmpty()) {
                    binding.messageText.text = message.text
                    binding.textMessageContainer.visibility = View.VISIBLE
                    binding.textMessageContainer.setOnLongClickListener {
                        onTextMessageLongClick(false, message)
                        false
                    }
                    binding.textMessageContainer.setOnClickListener {
                        handleTextMessageClick(bindingAdapterPosition)
                    }
                    updateTextMessageOfReceiverBackground(
                        binding,
                        message,
                        bindingAdapterPosition,
                        isReplyMessageShown
                    )
                } else {
                    binding.textMessageContainer.visibility = View.GONE
                }

                val medias = message.medias

                if (!medias.isNullOrEmpty()) {

                    val (images, files) = message.medias.partition { it.type == FileType.IMAGE.value }

                    if (images.isNotEmpty()) {
                        if (images.size == 1) {
                            val imageMediaData = images[0]
                            binding.imageRecyclerView.visibility = View.GONE
                            bindPhotoMessage(binding.image, imageMediaData.url)
                            binding.image.visibility = View.VISIBLE
                            binding.imageMessageCardView.setOnLongClickListener {
                                onImageLongClick(false, imageMediaData, message)
                                false
                            }

                            binding.imageMessageCardView.setOnClickListener {
                                onImageClick(binding.image.drawable, imageMediaData, message)
                            }

                        } else {
                            binding.image.visibility = View.GONE
                            if (imageMessageAdapter == null) {
                                imageMessageAdapter = ImageMessageAdapter(
                                    onImageLongClick = { onImageLongClick(false, it, message) },
                                    onImageClick = { drawable, mediaData ->
                                        onImageClick(drawable, mediaData, message)
                                    }
                                )

                                binding.imageRecyclerView.adapter = imageMessageAdapter
                            }
                            imageMessageAdapter?.submitList(images)
                            binding.imageRecyclerView.visibility = View.VISIBLE
                        }
                        binding.imageMessageCardView.visibility = View.VISIBLE
                    } else {
                        binding.imageRecyclerView.visibility = View.GONE
                        binding.imageMessageCardView.visibility = View.GONE
                    }

                    if (files.isNotEmpty()) {
                        if (fileAdapter == null) {
                            fileAdapter = FileAdapter(
                                isSender = false,
                                onFileLongClick = {
                                    onFileLongClick(false, it, message)
                                },
                                onFileContainerClick = {
                                    onMessageContentViewClick()
                                }
                            )
                            binding.fileRecyclerView.adapter = fileAdapter
                        }
                        fileAdapter?.submitList(files)
                        binding.fileRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.fileRecyclerView.visibility = View.GONE
                    }

                } else {
                    binding.fileRecyclerView.visibility = View.GONE
                    binding.imageMessageCardView.visibility = View.GONE
                    binding.imageRecyclerView.visibility = View.GONE
                }
            }

            binding.tvMessageTime.text = StringUtils.formatTime(
                binding.tvMessageTime.context,
                message.timestamp!!,
                false
            )
            binding.tvMessageTime.visibility =
                if (bindingAdapterPosition == expandedMessagePosition) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateTextMessageOfReceiverBackground(
        binding: LayoutReceiverMessageBinding,
        message: Message,
        bindingAdapterPosition: Int,
        isReplyMessageShown: Boolean
    ) {
        // Get the previous and next messages
        val previousMessage =
            if (bindingAdapterPosition > 0) getItem(bindingAdapterPosition - 1) else null
        val nextMessage =
            if (bindingAdapterPosition < itemCount - 1) getItem(bindingAdapterPosition + 1) else null

        // Determine if previous and next messages are from the same sender
        val isPreviousSameSender = previousMessage?.senderId == message.senderId
        val isNextSameSender = nextMessage?.senderId == message.senderId

        var needToCheckBackgroundUpdateIfThereIsReplyMessage = false

        var backgroundResId = when {
            // First item in a series from the receiver
            !isPreviousSameSender && isNextSameSender -> {
                needToCheckBackgroundUpdateIfThereIsReplyMessage = true
                R.drawable.bg_receiver_message_first
            }

            // Middle item in a series from the receiver
            isPreviousSameSender && isNextSameSender -> R.drawable.bg_receiver_message_middle

            // Last item in a series from the receiver
            isPreviousSameSender && !isNextSameSender -> R.drawable.bg_receiver_message_last

            // Single isolated item
            !isPreviousSameSender && !isNextSameSender -> {
                needToCheckBackgroundUpdateIfThereIsReplyMessage = true
                R.drawable.bg_receiver_message_single
            }

            else -> {
                needToCheckBackgroundUpdateIfThereIsReplyMessage = true
                R.drawable.bg_receiver_message_single
            } // Fallback
        }

        if (needToCheckBackgroundUpdateIfThereIsReplyMessage && isReplyMessageShown) {
            backgroundResId = R.drawable.bg_receiver_message_last
        }

        // Apply the determined background drawable
        binding.textMessageContainer.background = binding
            .textMessageContainer
            .context
            .getDrawable(backgroundResId)
    }

    private fun updateVisibilityReceiverNameAndImageMessage(
        binding: LayoutReceiverMessageBinding,
        message: Message,
        isFirstReceiverMessage: Boolean
    ) {
        val context = binding.root.context
        val resources = context.resources

        val receiverAvatarCardViewLayoutParams =
            binding.receiverAvatarCardView.layoutParams as ViewGroup.MarginLayoutParams
        val receiverMessageContainerLayoutParams =
            binding.receiverMessageContainer.layoutParams as ViewGroup.MarginLayoutParams

        when {
            chatRoomType == ChatRoomType.Group.type && isFirstReceiverMessage -> {
                binding.tvReceiverName.visibility = View.VISIBLE
                binding.receiverAvatarCardView.visibility = View.VISIBLE
                MediaUtils.loadImageFollowImageViewSize(
                    binding.ivSenderAvatar,
                    message.senderData?.userAvatar,
                    fallback = R.drawable.ic_user_default,
                    error = R.drawable.ic_user_default,
                    placeholder = R.drawable.ic_user_default
                )

                val senderName =
                    message.senderData?.username ?: context.getString(R.string.app_user)
                binding.tvReceiverName.text = senderName

                receiverAvatarCardViewLayoutParams.topMargin =
                    resources.getDimensionPixelSize(R.dimen.margin_24dp)
                receiverMessageContainerLayoutParams.marginStart =
                    resources.getDimensionPixelSize(R.dimen.spacing_small)
            }

            chatRoomType == ChatRoomType.Group.type && !isFirstReceiverMessage -> {
                binding.tvReceiverName.visibility = View.GONE
                binding.receiverAvatarCardView.visibility = View.INVISIBLE
                receiverAvatarCardViewLayoutParams.topMargin = 0
                receiverMessageContainerLayoutParams.marginStart =
                    resources.getDimensionPixelSize(R.dimen.spacing_small)
            }

            else -> {
                binding.tvReceiverName.visibility = View.GONE
                binding.receiverAvatarCardView.visibility = View.GONE
                receiverAvatarCardViewLayoutParams.topMargin = 0
                receiverMessageContainerLayoutParams.marginStart = 0
            }
        }

        // Apply the updated layout parameters
        binding.receiverAvatarCardView.layoutParams = receiverAvatarCardViewLayoutParams
        binding.receiverMessageContainer.layoutParams = receiverMessageContainerLayoutParams
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).senderId == Firebase.auth.uid)
            sender
        else
            receiver

    companion object DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == sender) {
            SenderMessageViewHolder(
                LayoutSenderMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ReceiverMessageViewHolder(
                LayoutReceiverMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is SenderMessageViewHolder || holder is ReceiverMessageViewHolder) {
            holder.itemView.setOnClickListener { onMessageContentViewClick() }
            if (holder is SenderMessageViewHolder) holder.bind(item)
            if (holder is ReceiverMessageViewHolder) {
                val isFirstReceiverMessage =
                    position == 0 || getItem(position - 1).senderId != item.senderId
                holder.bind(item, isFirstReceiverMessage)
            }

            // Hiển thị thời gian của tin nhắn nếu là tin nhắn cuối cùng
//            // Kiểm tra nếu là item cuối cùng
//            if (position == itemCount - 1) {
//                holder.itemView.findViewById<TextView>(R.id.tvMessageTime).visibility = View.VISIBLE
//            } else if (position != expandedMessagePosition) {
//                // Ẩn tvMessageTime nếu không phải là item cuối cùng và cũng không phải là item đã mở rộng
//                holder.itemView.findViewById<TextView>(R.id.tvMessageTime).visibility = View.GONE
//            }
        }
    }

    private fun handleTextMessageClick(position: Int) {
        val previousExpandedPosition = expandedMessagePosition
        expandedMessagePosition = if (position == expandedMessagePosition) null else position
        previousExpandedPosition?.let { notifyItemChanged(it) }
        expandedMessagePosition?.let { notifyItemChanged(it) }
    }

    fun getPositionById(messageId: String): Int {
        return currentList.indexOfFirst { it.messageId == messageId }
    }

//    @SuppressLint("RestrictedApi")
//    private fun showMenu(context: Context?, v: View, @MenuRes menuRes: Int) {
//        context?.let { context ->
//            val popup = PopupMenu(context, v)
//            popup.menuInflater.inflate(menuRes, popup.menu)
//            if (popup.menu is MenuBuilder) {
//                val menuBuilder = popup.menu as MenuBuilder
//                menuBuilder.setOptionalIconsVisible(true)
//                for (item in menuBuilder.visibleItems) {
//                    val iconMarginPx =
//                        TypedValue.applyDimension(
//                            TypedValue.COMPLEX_UNIT_DIP,
//                            ICON_MARGIN.toFloat(),
//                            context.resources.displayMetrics
//                        )
//                            .toInt()
//                    if (item.icon != null) {
//                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//                            item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
//                        } else {
//                            item.icon =
//                                object :
//                                    InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
//                                    override fun getIntrinsicWidth(): Int {
//                                        return intrinsicHeight + iconMarginPx + iconMarginPx
//                                    }
//                                }
//                        }
//                    }
//                }
//            }
//            popup.gravity = Gravity.END
//            popup.show()
//        }
//    }
}