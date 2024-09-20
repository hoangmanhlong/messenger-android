package com.android.kotlin.familymessagingapp.screen.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.databinding.FragmentChatRoomMemberBinding
import com.android.kotlin.familymessagingapp.screen.chatroom.ChatRoomViewModel
import com.android.kotlin.familymessagingapp.screen.home.UserAdapter
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatRoomMemberFragment : Fragment() {

    private val chatRoomMemberViewModel: ChatRoomMemberViewModel by viewModels()

    private val chatRoomViewModel: ChatRoomViewModel by activityViewModels()

    private var _binding: FragmentChatRoomMemberBinding? = null

    private val binding get() = _binding!!

    private var memberAdapter: UserAdapter? = null

    private var chatRoomMemberRecyclerView: RecyclerView? = null

    private var etSearch: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomMemberBinding.inflate(inflater, container, false)

        chatRoomMemberRecyclerView = binding.chatRoomMemberRecyclerView
        memberAdapter = UserAdapter {
//            val action = ChatRoomMemberFragmentDirections.actionChatRoomMemberFragmentToChatRoomFragment(
//                userdata = it,
//                chatroom = null,
//                isOpenFromNotification = false
//            )
//            findNavController().navigate(action)
        }
        chatRoomMemberRecyclerView?.adapter = memberAdapter

        etSearch = binding.etSearch

        binding.btNavigateUp.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btSearch.setOnClickListener {
            chatRoomMemberViewModel.updateSearchStatus()
        }

        etSearch?.addTextChangedListener {
            chatRoomMemberViewModel.setKeyword(it.toString().trim())
        }

        binding.ivClearSearchInput.setOnClickListener {
            etSearch?.text = null
        }

        etSearch?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                chatRoomMemberViewModel.searchMembers()
            }
            false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatRoomViewModel.chatRoom.observe(viewLifecycleOwner) {
            chatRoomMemberViewModel.setChatRoomMembers(it?.membersData)
        }

        chatRoomMemberViewModel.isSearching.observe(viewLifecycleOwner) {
            if (it) {
                binding.searchUserView.visibility = View.VISIBLE
                etSearch?.requestFocus()
                showKeyboard()
            } else {
                hideKeyboard()
                binding.searchUserView.visibility = View.GONE
            }
        }

        chatRoomMemberViewModel.clearInputButtonStatus.observe(viewLifecycleOwner) {
            binding.ivClearSearchInput.visibility = if (it) View.VISIBLE else View.GONE
        }

        chatRoomMemberViewModel.displayedChatRoomMembers.observe(viewLifecycleOwner) {
            memberAdapter?.submitList(it)
            binding.tvNoResult.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE

        }
    }

    private fun hideKeyboard() {
        activity?.let { KeyBoardUtils.hideSoftKeyboard(it) }
    }

    private fun showKeyboard() {
        etSearch?.let {
            KeyBoardUtils.showSoftKeyboard(it)
        }
    }
}