package com.android.kotlin.familymessagingapp.screen.chatroom

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutEmojiMessageBinding
import com.android.kotlin.familymessagingapp.model.Reaction

class ReactionsAdapter(
    private val onEmojiClicked: (Reaction) -> Unit
) : ListAdapter<Reaction, ReactionsAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Reaction>() {
        override fun areItemsTheSame(oldItem: Reaction, newItem: Reaction): Boolean {
            return oldItem.emoji === newItem.emoji
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Reaction, newItem: Reaction): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(
        private val binding: LayoutEmojiMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reaction: Reaction) {

            binding.tvEmoji.text = "${reaction.emoji} ${reaction.reactedBy.size}"
            binding.root.setOnClickListener {
                onEmojiClicked(reaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutEmojiMessageBinding.inflate(
                android.view.LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}