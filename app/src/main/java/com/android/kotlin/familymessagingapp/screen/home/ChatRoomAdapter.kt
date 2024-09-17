package com.android.kotlin.familymessagingapp.screen.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutChatroomBinding
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.android.kotlin.familymessagingapp.utils.bindChatRoomImage

class ChatRoomAdapter(
    private val onChatRoomClick: (ChatRoom) -> Unit,
    private val onChatRoomLongClick: (ChatRoom) -> Unit
) : ListAdapter<ChatRoom, ChatRoomAdapter.ChatRoomViewHolder>(DiffCallback) {

    inner class ChatRoomViewHolder(
        private val binding: LayoutChatroomBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatRoom: ChatRoom) {

            bindChatRoomImage(binding.ivChatRoom, chatRoom)
            binding.tvChatRoomName.text = chatRoom.chatRoomName

            StringUtils.getFormattedMessageOfLatestActivityInChatRoom(
                binding.tvLatestActivity,
                chatRoom
            )

            val latestActiveTime = chatRoom.chatRoomActivity?.latestActiveTime
            if (latestActiveTime != null) {
                binding.tvLatestActiveTime.text = StringUtils.formatTime(latestActiveTime, true)
                binding.tvLatestActiveTime.visibility = ViewGroup.VISIBLE
            } else {
                binding.tvLatestActiveTime.visibility = ViewGroup.GONE
            }

            binding.root.setOnClickListener {
                onChatRoomClick(chatRoom)
            }
            binding.root.setOnLongClickListener {
                onChatRoomLongClick(chatRoom)
                false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder(
            LayoutChatroomBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem.chatRoomId == newItem.chatRoomId
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }
    }
}