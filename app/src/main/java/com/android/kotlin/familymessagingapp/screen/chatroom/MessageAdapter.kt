package com.android.kotlin.familymessagingapp.screen.chatroom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.LayoutReceiverMessageBinding
import com.android.kotlin.familymessagingapp.databinding.LayoutSenderMessageBinding
import com.android.kotlin.familymessagingapp.model.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MessageAdapter(
    private val onMessageLongClick: (Message) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    private val ICON_MARGIN = 16

    private val sender = 1

    private val receiver = 2

    inner class SenderMessageViewHolder(
        private val binding: LayoutSenderMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.hasText = message.text != null
            binding.hasImage = message.photo != null
            binding.message = message
        }
    }

    inner class ReceiverMessageViewHolder(
        private val binding: LayoutReceiverMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.hasText = message.text != null
            binding.hasImage = message.photo != null
            binding.message = message
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).fromId == Firebase.auth.uid)
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
        val viewHolder = if (viewType == sender) {
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

//        if (viewType == sender) viewHolder.itemView.setOnLongClickListener {
//            showMenu(viewHolder.itemView.context, it, R.menu.menu_message)
//            onMessageLongClick(getItem(viewHolder.adapterPosition))
//            true
//        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SenderMessageViewHolder) holder.bind(getItem(position))
        if (holder is ReceiverMessageViewHolder) holder.bind(getItem(position))
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