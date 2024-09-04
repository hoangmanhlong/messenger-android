package com.android.kotlin.familymessagingapp.screen.chatroom

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutSelectedPhotoBinding
import com.android.kotlin.familymessagingapp.utils.loadImageFollowImageViewSize

class SelectedItemAdapter(
    private val onItemRemove: (Uri) -> Unit
) : ListAdapter<Uri, SelectedItemAdapter.SelectedItemViewHolder>(DiffCallback) {

    inner class SelectedItemViewHolder(
        val binding: LayoutSelectedPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Uri) {
            loadImageFollowImageViewSize(binding.ivImage, item)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedItemViewHolder {
        val viewHolder = SelectedItemViewHolder(
            LayoutSelectedPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: SelectedItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.btClearImage.setOnClickListener {
            onItemRemove(item)
        }
        holder.bind(item)
    }
}