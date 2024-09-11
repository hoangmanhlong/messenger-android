package com.android.kotlin.familymessagingapp.screen.chatroom

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.ItemFileBinding
import com.android.kotlin.familymessagingapp.model.MediaData

class FileAdapter(
    val isSender: Boolean
): ListAdapter<MediaData, FileAdapter.FileViewHolder>(DiffCallback) {

    companion object DiffCallback: DiffUtil.ItemCallback<MediaData>() {
        override fun areItemsTheSame(oldItem: MediaData, newItem: MediaData): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: MediaData, newItem: MediaData): Boolean {
            return  oldItem == newItem
        }
    }

    inner class FileViewHolder(private val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            val fileCardLayoutParams = binding.fileCard.layoutParams as ViewGroup.MarginLayoutParams
            (fileCardLayoutParams as FrameLayout.LayoutParams).gravity = if (isSender) Gravity.END else Gravity.START
        }
        fun bind(mediaData: MediaData) {
            binding.tvFileName.text = mediaData.fileName
            binding.root.context.getString(R.string.file_size_type, mediaData.size, mediaData.type)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        return FileViewHolder(ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}