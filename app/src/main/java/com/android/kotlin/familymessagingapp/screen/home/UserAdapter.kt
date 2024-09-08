package com.android.kotlin.familymessagingapp.screen.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutUserBinding
import com.android.kotlin.familymessagingapp.model.UserData

class UserAdapter(
    private val onItemClicked: (UserData) -> Unit
) : ListAdapter<UserData, UserAdapter.UserViewHolder>(DiffCallback) {

    inner class UserViewHolder(
        private val binding: LayoutUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userData: UserData) {
            binding.userdata = userData
            binding.root.setOnClickListener {
                onItemClicked(userData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<UserData>() {
        override fun areItemsTheSame(oldItem: UserData, newItem: UserData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserData, newItem: UserData): Boolean {
            return oldItem == newItem
        }
    }
}