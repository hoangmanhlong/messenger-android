package com.android.kotlin.familymessagingapp.screen.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistoryEntity
import com.android.kotlin.familymessagingapp.databinding.LayoutSearchHistoryBinding

class SearchHistoryAdapter(
    private val onItemClicked: (SearchHistoryEntity) -> Unit,
    private val onDeleteItem: (SearchHistoryEntity) -> Unit,
    private val onPushItem: (SearchHistoryEntity) -> Unit
) : ListAdapter<SearchHistoryEntity, SearchHistoryAdapter.SearchHistoryViewHolder>(DiffCallback) {

    inner class SearchHistoryViewHolder(
        val binding: LayoutSearchHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchHistoryEntity: SearchHistoryEntity) {
            binding.searchHistory = searchHistoryEntity
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        val viewHolder = SearchHistoryViewHolder(
            LayoutSearchHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }

        holder.itemView.setOnLongClickListener {
            onDeleteItem(item)
            true
        }

        holder.binding.ivPush.setOnClickListener {
            onPushItem(item)
        }

        holder.bind(item)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SearchHistoryEntity>() {
        override fun areItemsTheSame(oldItem: SearchHistoryEntity, newItem: SearchHistoryEntity): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SearchHistoryEntity, newItem: SearchHistoryEntity): Boolean {
            return oldItem == newItem
        }
    }
}