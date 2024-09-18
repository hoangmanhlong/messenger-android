package com.android.kotlin.familymessagingapp.screen.chatroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.LayoutPinnedMessageBinding
import com.android.kotlin.familymessagingapp.model.FileType
import com.android.kotlin.familymessagingapp.model.PinnedMessage
import com.android.kotlin.familymessagingapp.model.getFileDrawableRes
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.StringUtils

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
            val context = binding.root.context
            binding.tvPinnedBy.text = context.getString(
                R.string.pinned_by,
                pinnedMessage.senderName,
                StringUtils.formatTime(context, pinnedMessage.pinTime!!, false)
            )

            val text = pinnedMessage.text
            val mediaData = pinnedMessage.pinnedMediaData

            if (!text.isNullOrEmpty()) {
                binding.mediaDataImageContainer.visibility = View.GONE
                binding.tvPinnedMessageText.text = text
            }

            if (mediaData != null) {
                if (mediaData.type == FileType.IMAGE.value) {
                    MediaUtils.loadImageFollowImageViewSize(
                        imageView = binding.ivMediaData,
                        photo = mediaData.url,
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    )
                    binding.tvPinnedMessageText.text = context.getString(R.string.photo_last_message)
                } else {
                    val fileName = mediaData.fileName

                    binding.ivMediaData.setImageResource(
                        getFileDrawableRes(mediaData.type)
                    )
                    binding.tvPinnedMessageText.text = fileName ?: context.getString(R.string.file)
                }

                binding.mediaDataImageContainer.visibility = View.VISIBLE
            }

            binding.root.setOnClickListener {
                onPinnedMessageClick(pinnedMessage)
            }
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