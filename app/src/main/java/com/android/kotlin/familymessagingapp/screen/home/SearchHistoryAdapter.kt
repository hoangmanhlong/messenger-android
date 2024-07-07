package com.android.kotlin.familymessagingapp.screen.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistory
import com.android.kotlin.familymessagingapp.databinding.LayoutSearchHistoryBinding

class SearchHistoryAdapter(
    private val onItemClicked: (SearchHistory) -> Unit,
    private val onDeleteItem: (SearchHistory) -> Unit
) : ListAdapter<SearchHistory, SearchHistoryAdapter.SearchHistoryViewHolder>(DiffCallback) {

    inner class SearchHistoryViewHolder(
        val binding: LayoutSearchHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(searchHistory: SearchHistory) {
            binding.searchHistory = searchHistory
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
        viewHolder.itemView.setOnClickListener {
            onItemClicked(getItem(viewHolder.adapterPosition))
        }

        viewHolder.itemView.setOnLongClickListener {
            onDeleteItem(getItem(viewHolder.adapterPosition))
            true
        }

        viewHolder.binding.remove.setOnClickListener {
            onDeleteItem(getItem(viewHolder.adapterPosition))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SearchHistory>() {
        override fun areItemsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
            return oldItem == newItem
        }
    }
}