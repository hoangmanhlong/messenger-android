package com.android.kotlin.familymessagingapp.screen.chatroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutEmojiBinding

class EmojiAdapter(
    private val onEmojiClicked: (String) -> Unit
) : ListAdapter<String, EmojiAdapter.EmojiViewHolder>(DiffCallback) {

    inner class EmojiViewHolder(val binding: LayoutEmojiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            binding.tvEmoji.setOnClickListener {
                onEmojiClicked(item)
            }
            binding.emoji = item
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiAdapter.EmojiViewHolder {
        return EmojiViewHolder(
            LayoutEmojiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: EmojiAdapter.EmojiViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

