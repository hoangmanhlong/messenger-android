package com.android.kotlin.familymessagingapp.screen.create_group_chat

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.LayoutContactBinding
import com.android.kotlin.familymessagingapp.databinding.LayoutUserBinding
import com.android.kotlin.familymessagingapp.model.Contact
import com.android.kotlin.familymessagingapp.model.UserData

class ContactAdapter(
    private val onItemClicked: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(DiffCallback) {

    inner class ContactViewHolder(
        private val binding: LayoutContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.checkbox.addOnCheckedStateChangedListener { checkBox, state ->
                onItemClicked(contact)
            }
            binding.root.setOnClickListener {
                binding.checkbox.isChecked = !binding.checkbox.isChecked
            }
            binding.userdata = contact.contactData
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            LayoutContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}