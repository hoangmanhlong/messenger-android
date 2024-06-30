package com.android.kotlin.familymessagingapp.screen.chatroom

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

class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    private val sender = 1

    private val receiver = 2

    inner class SenderMessageViewHolder(
        private val binding: LayoutSenderMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.message = message
        }
    }

    inner class ReceiverMessageViewHolder(
        private val binding: LayoutReceiverMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
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
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SenderMessageViewHolder) holder.bind(getItem(position))
        if (holder is ReceiverMessageViewHolder) holder.bind(getItem(position))
    }
}