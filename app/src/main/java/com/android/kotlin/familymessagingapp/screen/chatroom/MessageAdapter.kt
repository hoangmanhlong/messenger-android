package com.android.kotlin.familymessagingapp.screen.chatroom

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutReceiverMessageBinding
import com.android.kotlin.familymessagingapp.databinding.LayoutSenderMessageBinding
import com.android.kotlin.familymessagingapp.model.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MessageAdapter(
    private val onMessageContentViewClick: () -> Unit,
    private val onMessageLongClick: (Boolean, Message) -> Unit,
    private val onImageMessageClick: (Drawable, Message) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    private val ICON_MARGIN = 16

    private val sender = 1

    private val receiver = 2

    private var expandedMessagePosition: Int? = null

    inner class SenderMessageViewHolder(
        private val binding: LayoutSenderMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            if (!message.isTextEmpty()) {
                binding.textMessageConstraintLayout.setOnLongClickListener {
                    onMessageLongClick(true, message)
                    false
                }
                binding.textMessageConstraintLayout.setOnClickListener {
                    handleTextMessageClick(bindingAdapterPosition)
                }
            }
            if (!message.isPhotoEmpty()) {
                binding.imageMessageCardView.setOnClickListener {
                    onImageMessageClick(binding.image.drawable, message)
                }
                binding.imageMessageCardView.setOnLongClickListener {
                    onMessageLongClick(true, message)
                    false
                }
            }
            binding.showMessageTime = bindingAdapterPosition == expandedMessagePosition
            binding.message = message
        }
    }

    inner class ReceiverMessageViewHolder(
        private val binding: LayoutReceiverMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            if (!message.isTextEmpty()) {
                binding.textMessageConstraintLayout.setOnLongClickListener {
                    onMessageLongClick(false, message)
                    false
                }
                binding.textMessageConstraintLayout.setOnClickListener {
                    handleTextMessageClick(bindingAdapterPosition)
                }
            }
            if (!message.isPhotoEmpty()) {
                binding.imageMessageCardView.setOnClickListener {
                    onImageMessageClick(binding.image.drawable, message)
                }
                binding.imageMessageCardView.setOnLongClickListener {
                    onMessageLongClick(false, message)
                    false
                }
            }
            binding.showMessageTime = bindingAdapterPosition == expandedMessagePosition
            binding.message = message
        }
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
            if (holder is ReceiverMessageViewHolder) holder.bind(item)
        }
    }

    private fun handleTextMessageClick(position: Int) {
        val previousExpandedPosition = expandedMessagePosition
        expandedMessagePosition = if (position == expandedMessagePosition) null else position
        previousExpandedPosition?.let { notifyItemChanged(it) }
        expandedMessagePosition?.let { notifyItemChanged(it) }
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