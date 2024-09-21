package com.android.kotlin.familymessagingapp.screen.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainViewModel
import com.android.kotlin.familymessagingapp.databinding.FragmentChatRoomMemberBinding
import com.android.kotlin.familymessagingapp.screen.chatroom.ChatRoomViewModel
import com.android.kotlin.familymessagingapp.screen.create_group_chat.ContactAdapter
import com.android.kotlin.familymessagingapp.screen.home.UserAdapter
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatRoomMemberFragment : Fragment() {

    private val chatRoomMemberViewModel: ChatRoomMemberViewModel by viewModels()

    private val chatRoomViewModel: ChatRoomViewModel by activityViewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentChatRoomMemberBinding? = null

    private val binding get() = _binding!!

    private var memberAdapter: UserAdapter? = null

    private var chatRoomMemberRecyclerView: RecyclerView? = null

    private var contactsRecyclerView: RecyclerView? = null

    private var contactAdapter: ContactAdapter? = null

    private var etSearch: EditText? = null

    private var errorDialog: AlertDialog? = null

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

        contactAdapter = ContactAdapter { chatRoomMemberViewModel.updateMember(it) }
        contactsRecyclerView = binding.contactsRecyclerView
        contactsRecyclerView?.adapter = contactAdapter

        etSearch = binding.etSearch

        binding.btNavigateUp.setOnClickListener {
            findNavController().navigateUp()
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

        binding.btAddMember.setOnClickListener {
            chatRoomMemberViewModel.updateAddMemberStatus(true)
        }

        binding.btClose.setOnClickListener {
            chatRoomMemberViewModel.updateAddMemberStatus(false)
        }

        binding.btSave.setOnClickListener {
            etSearch?.clearFocus()
            hideKeyboard()
            activity?.let {
                NetworkChecker.checkNetwork(it) {
                    chatRoomMemberViewModel.saveNewChatRoomMember()
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // When pressing the back button on the device, if the add member status is true then change status
        activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner) {
            if (chatRoomMemberViewModel.isAddMemberStatus.value == true)
                chatRoomMemberViewModel.updateAddMemberStatus(false)
            else findNavController().navigateUp()
        }

        chatRoomViewModel.chatRoom.observe(viewLifecycleOwner) {
            chatRoomMemberViewModel.setChatRoom(it)
        }

        chatRoomMemberViewModel.isAddMemberStatus.observe(viewLifecycleOwner) {
            etSearch?.clearFocus()
            hideKeyboard()
            if (it) {
                binding.tvScreenTitle.text = getString(R.string.add_members)
                contactsRecyclerView?.visibility = View.VISIBLE
                chatRoomMemberRecyclerView?.visibility = View.GONE
                binding.btClose.visibility = View.VISIBLE
                binding.btNavigateUp.visibility = View.GONE
                binding.btSave.visibility = View.VISIBLE
                binding.btAddMember.visibility = View.GONE
                binding.chatRoomMemberRecyclerView.visibility = View.GONE
                binding.contactsView.visibility = View.VISIBLE
            } else {
                binding.tvScreenTitle.text = getString(R.string.members)
                contactsRecyclerView?.visibility = View.GONE
                chatRoomMemberRecyclerView?.visibility = View.VISIBLE
                binding.btClose.visibility = View.GONE
                binding.btNavigateUp.visibility = View.VISIBLE
                binding.btSave.visibility = View.GONE
                binding.btAddMember.visibility = View.VISIBLE
                binding.chatRoomMemberRecyclerView.visibility = View.VISIBLE
                binding.contactsView.visibility = View.GONE
            }
        }

//        chatRoomMemberViewModel.isSearching.observe(viewLifecycleOwner) {
//            if (it) {
//                binding.searchUserView.visibility = View.VISIBLE
//                binding.contactsRecyclerView
//                etSearch?.requestFocus()
//                showKeyboard()
//            } else {
//                hideKeyboard()
//                binding.searchUserView.visibility = View.GONE
//            }
//        }

        chatRoomMemberViewModel.clearInputButtonStatus.observe(viewLifecycleOwner) {
            binding.ivClearSearchInput.visibility = if (it) View.VISIBLE else View.GONE
        }

        chatRoomMemberViewModel.displayedChatRoomMembers.observe(viewLifecycleOwner) {
            memberAdapter?.submitList(it)
            binding.tvNoResult.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        chatRoomMemberViewModel.displayedContacts.observe(viewLifecycleOwner) {
            contactAdapter?.submitList(it)
            binding.tvContactEmpty.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        chatRoomMemberViewModel.addButtonVisibilityState.observe(viewLifecycleOwner) {
            binding.btSave.isEnabled = it
        }

        chatRoomMemberViewModel.saveNewChatRoomMemberSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    chatRoomMemberViewModel.updateAddMemberStatus(false)
                } else {
                    showErrorDialog()
                }
            }
        }

        chatRoomMemberViewModel.isLoading.observe(viewLifecycleOwner) {
            mainViewModel.setIsLoading(it)
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

    private fun showErrorDialog() {
        if (activity == null) return

        if (errorDialog == null) {
            errorDialog = DialogUtils.showNotificationDialog(
                context = requireActivity(),
                message = R.string.error_occurred,
                cancelable = false,
                onOkButtonClick = null
            )
        }
        errorDialog?.show()
    }
}