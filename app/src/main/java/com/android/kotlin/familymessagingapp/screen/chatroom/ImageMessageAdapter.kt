package com.android.kotlin.familymessagingapp.screen.chatroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.ItemImageMessageBinding
import com.android.kotlin.familymessagingapp.model.MediaData
import com.android.kotlin.familymessagingapp.utils.bindPhotoMessage

class ImageMessageAdapter: ListAdapter<MediaData, ImageMessageAdapter.ImageMessageViewHolder>(DiffCallback) {

    companion object DiffCallback: DiffUtil.ItemCallback<MediaData>() {
        override fun areItemsTheSame(oldItem: MediaData, newItem: MediaData): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: MediaData, newItem: MediaData): Boolean {
            return oldItem == newItem
        }

    }

    inner class ImageMessageViewHolder(
        val binding: ItemImageMessageBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(mediaData: MediaData) {
            bindPhotoMessage(binding.image, mediaData.url)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageMessageViewHolder {
        return ImageMessageViewHolder(
            ItemImageMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageMessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}