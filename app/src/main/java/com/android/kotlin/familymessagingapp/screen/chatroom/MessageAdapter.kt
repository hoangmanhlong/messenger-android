package com.android.kotlin.familymessagingapp.screen.chatroom

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.LayoutReceiverMessageBinding
import com.android.kotlin.familymessagingapp.databinding.LayoutSenderMessageBinding
import com.android.kotlin.familymessagingapp.model.ChatRoomType
import com.android.kotlin.familymessagingapp.model.Message
import com.android.kotlin.familymessagingapp.model.Reaction
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.android.kotlin.familymessagingapp.utils.bindNormalImage
import com.android.kotlin.familymessagingapp.utils.bindPhotoMessage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MessageAdapter(
    private val onMessageContentViewClick: () -> Unit,
    private val onMessageLongClick: (Boolean, Message) -> Unit,
    private val onImageMessageClick: (Drawable, Message) -> Unit,
    private val onReplyMessageClick: (Message) -> Unit,
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

        init {
            reactionsAdapter = ReactionsAdapter {

            }
            binding.reactionsRecyclerView.adapter = reactionsAdapter
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(message: Message) {

            if (!message.isReplyMessageEmpty()) {
                val replyMessage = message.replyMessage
                if (replyMessage != null) {
                    binding.tvSenderNameReplyMessage.text = replyMessage.senderData?.username
                    if (replyMessage.removedByIsEmpty()) {

                        if (!replyMessage.text.isNullOrEmpty() || !replyMessage.photo.isNullOrEmpty()) {
                            binding.tvTextReplyMessage.context?.let {
                                binding.tvTextReplyMessage.text =
                                    if (replyMessage.text.isNullOrEmpty()) it.getString(R.string.sent_an_image) else replyMessage.text
                            }
                        }
                        if (!replyMessage.photo.isNullOrEmpty()) {
                            binding.replyMessageImageView.visibility = View.VISIBLE
                            bindNormalImage(binding.replyMessageImageView, replyMessage.photo)
                        } else {
                            binding.replyMessageImageView.visibility = View.GONE
                        }
                    } else {
                        binding.replyMessageImageView.visibility = View.GONE
                        binding.tvTextReplyMessage.text =
                            binding.tvTextReplyMessage.context.getString(R.string.message_removed)
                    }

                    binding.replyMessageContainer.visibility = View.VISIBLE
                    binding.textMessageContainer.background =
                        binding.textMessageContainer.context.getDrawable(R.drawable.bg_sender_text_message_has_reply_message)
                    binding.removedMessageView.background =
                        binding.removedMessageView.context.getDrawable(R.drawable.bg_removed_message_has_reply_message)

                    binding.replyMessageContainer.setOnClickListener {
                        onReplyMessageClick(replyMessage)
                    }
                } else {
                    binding.replyMessageContainer.visibility = View.GONE
                    binding.textMessageContainer.background =
                        binding.textMessageContainer.context.getDrawable(R.drawable.bg_sender_message_single)

                    binding.removedMessageView.background =
                        binding.removedMessageView.context.getDrawable(R.drawable.bg_removed_message)
                }
            } else {
                binding.replyMessageContainer.visibility = View.GONE
                binding.textMessageContainer.background =
                    binding.textMessageContainer.context.getDrawable(R.drawable.bg_sender_message_single)

                binding.removedMessageView.background =
                    binding.removedMessageView.context.getDrawable(R.drawable.bg_removed_message)
            }

            if (!message.removedByIsEmpty()) {
                binding.textMessageContainer.visibility = View.GONE
                binding.imageMessageCardView.visibility = View.GONE
                binding.reactionsRecyclerView.visibility = View.GONE
                binding.removedMessageView.visibility = View.VISIBLE
                binding.removedMessageView.setOnClickListener {
                    handleTextMessageClick(bindingAdapterPosition)
                }
            } else {
                binding.removedMessageView.visibility = View.GONE
                val reactions = message.reactions?.entries?.map {
                    Reaction(it.key, it.value.entries.map { it.key })
                }
                binding.reactionsRecyclerView.visibility =
                    if (reactions == null) View.GONE else View.VISIBLE
                reactionsAdapter?.submitList(reactions)
                if (!message.isTextEmpty()) {
                    binding.textMessageContainer.visibility = View.VISIBLE
                    binding.messageText.text = message.text
                    binding.textMessageContainer.setOnLongClickListener {
                        onMessageLongClick(true, message)
                        false
                    }
                    binding.textMessageContainer.setOnClickListener {
                        handleTextMessageClick(bindingAdapterPosition)
                    }
                    updateTextMessageOfSenderBackground(binding, message, bindingAdapterPosition)
                } else {
                    binding.textMessageContainer.visibility = View.GONE
                }
                if (!message.isPhotoEmpty()) {
                    bindPhotoMessage(binding.image, message.photo)
                    binding.imageMessageCardView.visibility = View.VISIBLE
                    binding.imageMessageCardView.setOnClickListener {
                        onImageMessageClick(binding.image.drawable, message)
                    }
                    binding.imageMessageCardView.setOnLongClickListener {
                        onMessageLongClick(true, message)
                        false
                    }
                } else {
                    binding.imageMessageCardView.visibility = View.GONE
                }
            }
            binding.tvMessageTime.text = StringUtils.formatTime(message.timestamp!!, false)
            binding.tvMessageTime.visibility =
                if (bindingAdapterPosition == expandedMessagePosition) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateTextMessageOfSenderBackground(
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
            !isPreviousSameSender && isNextSameSender -> R.drawable.bg_sender_message_first

            // Middle item in a series from the receiver
            isPreviousSameSender && isNextSameSender -> R.drawable.bg_sender_message_middle

            // Last item in a series from the receiver
            isPreviousSameSender && !isNextSameSender -> R.drawable.bg_sender_message_last

            // Single isolated item
            !isPreviousSameSender && !isNextSameSender -> R.drawable.bg_sender_message_single

            else -> R.drawable.bg_sender_message_single // Fallback
        }

        // Apply the determined background drawable
        binding.textMessageContainer.background = binding
            .textMessageContainer
            .context
            .getDrawable(backgroundResId)
    }

    inner class ReceiverMessageViewHolder(
        private val binding: LayoutReceiverMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var reactionsAdapter: ReactionsAdapter? = null

        init {
            reactionsAdapter = ReactionsAdapter {

            }
            binding.reactionsRecyclerView.adapter = reactionsAdapter
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(message: Message, isFirstReceiverMessage: Boolean) {

            updateVisibilityReceiverNameAndImageMessage(binding, message, isFirstReceiverMessage)

            if (!message.isReplyMessageEmpty()) {

                val replyMessage = message.replyMessage
                if (replyMessage != null) {

                    binding.tvSenderNameReplyMessage.text = replyMessage.senderData?.username

                    if (replyMessage.removedByIsEmpty()) {

                        if (!replyMessage.text.isNullOrEmpty() || !replyMessage.photo.isNullOrEmpty()) {
                            binding.tvTextReplyMessage.context?.let {
                                binding.tvTextReplyMessage.text =
                                    if (replyMessage.text.isNullOrEmpty()) it.getString(R.string.sent_an_image) else replyMessage.text
                            }
                        }
                        if (!replyMessage.photo.isNullOrEmpty()) {
                            binding.replyMessageImageView.visibility = View.VISIBLE
                            bindNormalImage(binding.replyMessageImageView, replyMessage.photo)
                        } else {
                            binding.replyMessageImageView.visibility = View.GONE
                        }
                    } else {
                        binding.replyMessageImageView.visibility = View.GONE
                        binding.tvTextReplyMessage.text =
                            binding.tvTextReplyMessage.context.getString(R.string.message_removed)
                    }

                    binding.replyMessageContainer.visibility = View.VISIBLE
                    binding.textMessageContainer.background = binding
                        .textMessageContainer
                        .context
                        .getDrawable(R.drawable.bg_receiver_text_message_has_reply_message)

                    binding.removedMessageView.background = binding
                        .removedMessageView
                        .context
                        .getDrawable(R.drawable.bg_receiver_removed_message_has_reply_message)

                    binding.replyMessageContainer.setOnClickListener {
                        onReplyMessageClick(replyMessage)
                    }
                } else {
                    binding.replyMessageContainer.visibility = View.GONE
                    binding.textMessageContainer.background =
                        binding.textMessageContainer.context.getDrawable(R.drawable.bg_receiver_message_single)

                    binding.removedMessageView.background =
                        binding.removedMessageView.context.getDrawable(R.drawable.bg_removed_message)
                }
            } else {
                binding.replyMessageContainer.visibility = View.GONE
                binding.textMessageContainer.background =
                    binding.textMessageContainer.context.getDrawable(R.drawable.bg_receiver_message_single)

                binding.removedMessageView.background =
                    binding.removedMessageView.context.getDrawable(R.drawable.bg_removed_message)
            }

            if (!message.removedByIsEmpty()) {
                binding.textMessageContainer.visibility = View.GONE
                binding.imageMessageCardView.visibility = View.GONE
                binding.reactionsRecyclerView.visibility = View.GONE
                binding.removedMessageView.visibility = View.VISIBLE
                binding.removedMessageView.setOnClickListener {
                    handleTextMessageClick(bindingAdapterPosition)
                }
            } else {
                binding.removedMessageView.visibility = View.GONE
                val reactions = message.reactions?.entries?.map {
                    Reaction(it.key, it.value.entries.map { it.key })
                }
                binding.reactionsRecyclerView.visibility =
                    if (reactions == null) View.GONE else View.VISIBLE
                reactionsAdapter?.submitList(reactions)

                if (!message.isTextEmpty()) {
                    binding.textMessageContainer.visibility = View.VISIBLE

                    binding.messageText.text = message.text
                    binding.textMessageContainer.setOnLongClickListener {
                        onMessageLongClick(false, message)
                        false
                    }
                    binding.textMessageContainer.setOnClickListener {
                        handleTextMessageClick(bindingAdapterPosition)
                    }
                    updateTextMessageOfReceiverBackground(binding, message, bindingAdapterPosition)
                } else {
                    binding.textMessageContainer.visibility = View.GONE
                }

                if (!message.isPhotoEmpty()) {
                    bindPhotoMessage(binding.image, message.photo)
                    binding.imageMessageCardView.visibility = View.VISIBLE
                    binding.imageMessageCardView.setOnClickListener {
                        onImageMessageClick(binding.image.drawable, message)
                    }
                    binding.imageMessageCardView.setOnLongClickListener {
                        onMessageLongClick(false, message)
                        false
                    }
                } else {
                    binding.imageMessageCardView.visibility = View.GONE
                }
            }

            binding.tvMessageTime.text = StringUtils.formatTime(message.timestamp!!, false)
            binding.tvMessageTime.visibility =
                if (bindingAdapterPosition == expandedMessagePosition) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateTextMessageOfReceiverBackground(
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
            !isPreviousSameSender && isNextSameSender -> R.drawable.bg_receiver_message_first

            // Middle item in a series from the receiver
            isPreviousSameSender && isNextSameSender -> R.drawable.bg_receiver_message_middle

            // Last item in a series from the receiver
            isPreviousSameSender && !isNextSameSender -> R.drawable.bg_receiver_message_last

            // Single isolated item
            !isPreviousSameSender && !isNextSameSender -> R.drawable.bg_receiver_message_single

            else -> R.drawable.bg_receiver_message_single // Fallback
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
        val layoutParams = binding.ivAvatar.layoutParams as ViewGroup.MarginLayoutParams

        if (chatRoomType == ChatRoomType.Group.type && isFirstReceiverMessage) {

            binding.tvReceiverName.visibility = View.VISIBLE
            binding.ivAvatar.visibility = View.VISIBLE

            bindNormalImage(binding.ivSenderAvatar, message.senderData?.userAvatar)

            val senderName = message.senderData?.username
                ?: binding.tvReceiverName.context.getString(R.string.app_user)

            binding.tvReceiverName.text = senderName

            layoutParams.topMargin = binding.ivAvatar
                .context
                .resources
                .getDimensionPixelSize(R.dimen.margin_24dp)

        } else if (chatRoomType == ChatRoomType.Group.type && !isFirstReceiverMessage) {
            binding.tvReceiverName.visibility = View.GONE
            binding.ivAvatar.visibility = View.INVISIBLE
            layoutParams.topMargin = 0
        } else {
            binding.tvReceiverName.visibility = View.GONE
            binding.ivAvatar.visibility = View.GONE
            layoutParams.topMargin = 0
        }

        // Apply the updated layout parameters
        binding.ivAvatar.layoutParams = layoutParams
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
        val isFirstReceiverMessage = if (position > 0) {
            val previousMessage = getItem(position - 1)
            previousMessage.senderId != item.senderId
        } else {
            true // Item đầu tiên luôn hiển thị ảnh và tên
        }
        if (holder is SenderMessageViewHolder || holder is ReceiverMessageViewHolder) {
            holder.itemView.setOnClickListener { onMessageContentViewClick() }
            if (holder is SenderMessageViewHolder) holder.bind(item)
            if (holder is ReceiverMessageViewHolder) holder.bind(item, isFirstReceiverMessage)
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