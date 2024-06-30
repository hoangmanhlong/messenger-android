package com.android.kotlin.familymessagingapp.screen.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutChatroomBinding
import com.android.kotlin.familymessagingapp.model.ChatRoom

class ChatRoomAdapter(
    private val onChatRoomClick: (ChatRoom) -> Unit,
    private val onChatRoomLongClick: (ChatRoom) -> Unit
) : ListAdapter<ChatRoom, ChatRoomAdapter.ChatRoomViewHolder>(DiffCallback) {

    inner class ChatRoomViewHolder(
        private val binding: LayoutChatroomBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatRoom: ChatRoom) {
            binding.chatroom = chatRoom
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val viewHolder = ChatRoomViewHolder(
            LayoutChatroomBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        viewHolder.itemView.setOnClickListener {
            onChatRoomClick(getItem(viewHolder.adapterPosition))
        }

        viewHolder.itemView.setOnLongClickListener {
            onChatRoomLongClick(getItem(viewHolder.adapterPosition))
            true
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }
    }
}