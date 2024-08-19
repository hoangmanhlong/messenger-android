package com.android.kotlin.familymessagingapp.screen.create_group_chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentCreateGroupChatBinding
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateGroupChatFragment : Fragment() {

    private val viewModel: CreateGroupChatViewModel by viewModels()

    private var _binding: FragmentCreateGroupChatBinding? = null

    private val binding get() = _binding!!

    private var selectMemberRecyclerView: RecyclerView? = null

    private var contactAdapter: ContactAdapter? = null

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let {
                viewModel.updateSelectedImageUri(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateGroupChatBinding.inflate(inflater, container, false)
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

        selectMemberRecyclerView = binding.selectMemberRecyclerView
        contactAdapter = ContactAdapter { viewModel.updateMember(it) }
        selectMemberRecyclerView?.adapter = contactAdapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateKeyword(s.toString().trim())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })

        binding.etGroupChatName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateChatRoomName(s.toString().trim())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })

        binding.chatRoomImageContainerView.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.ivClearSearchInput.setOnClickListener {
            binding.etSearch.text = null
        }

        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                activity?.let { KeyBoardUtils.hideSoftKeyboard(it) }

            }
            false
        }

        binding.btCreateGroupChat.setOnClickListener {
            viewModel.createChatRoom()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { KeyBoardUtils.setupHideKeyboard(view, it) }

        viewModel.createGroupButtonVisibilityState.observe(this.viewLifecycleOwner) {
            binding.btCreateGroupChat.isEnabled = it
        }

        viewModel.selectedImageUri.observe(this.viewLifecycleOwner) {
            if (it == null) {
                binding.chatRoomImageView.setImageResource(R.drawable.group)
            } else {
                binding.chatRoomImageView.setImageURI(it)
            }
        }

        viewModel.clearSearchInputState.observe(this.viewLifecycleOwner) {
            binding.ivClearSearchInput.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.contacts.observe(this.viewLifecycleOwner) {
            it?.let {
                binding.isEmptyContacts = it.isEmpty()
                contactAdapter?.submitList(it)
            }
        }
    }
}