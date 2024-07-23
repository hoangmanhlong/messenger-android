package com.android.kotlin.familymessagingapp.screen.chatroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutPinnedMessageBinding
import com.android.kotlin.familymessagingapp.model.PinnedMessage

class PinnedMessageAdapter(
    private val onPinnedMessageClick: (PinnedMessage) -> Unit
) : ListAdapter<PinnedMessage, PinnedMessageAdapter.PinnedMessageViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<PinnedMessage>() {
        override fun areItemsTheSame(oldItem: PinnedMessage, newItem: PinnedMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: PinnedMessage, newItem: PinnedMessage): Boolean {
            return oldItem == newItem
        }

    }

    inner class PinnedMessageViewHolder(
        private val binding: LayoutPinnedMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pinnedMessage: PinnedMessage) {
            binding.root.setOnClickListener {
                onPinnedMessageClick(pinnedMessage)
            }
            binding.pinnedMessage = pinnedMessage
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinnedMessageViewHolder {
        return PinnedMessageViewHolder(
            LayoutPinnedMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PinnedMessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}