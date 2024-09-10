package com.android.kotlin.familymessagingapp.screen.chatroom

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutSelectedItemBinding
import com.android.kotlin.familymessagingapp.utils.FileType
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.loadImageFollowImageViewSize

class SelectedItemAdapter(
    private val onItemRemove: (Uri) -> Unit,
    private val onPhotoItemClick: (Drawable) -> Unit
) : ListAdapter<Uri, SelectedItemAdapter.SelectedItemViewHolder>(DiffCallback) {

    inner class SelectedItemViewHolder(
        val binding: LayoutSelectedItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Uri) {
            val fileType = MediaUtils.getFileType(binding.root.context, item)

            if (fileType == FileType.IMAGE) {
                binding.fileContainerView.visibility = View.GONE
                binding.photoView.visibility = View.VISIBLE
                loadImageFollowImageViewSize(binding.ivImage, item)
                binding.photoView.setOnClickListener { onPhotoItemClick(binding.ivImage.drawable) }
            } else {
                val fileName = MediaUtils.getFileName(binding.root.context, item)
                binding.tvFileName.visibility = if (fileName.isNullOrEmpty()) View.GONE else View.VISIBLE

                binding.ivFile.setImageResource(MediaUtils.getFileDrawableRes(fileType))
                binding.tvFileName.text = fileName

                binding.fileContainerView.visibility = View.VISIBLE
                binding.photoView.visibility = View.GONE
            }

            binding.btClearItem.setOnClickListener {
                onItemRemove(item)
            }
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
            LayoutSelectedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: SelectedItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}