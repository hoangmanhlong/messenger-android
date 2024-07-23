package com.android.kotlin.familymessagingapp.screen.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistory
import com.android.kotlin.familymessagingapp.databinding.FragmentHomeBinding
import com.android.kotlin.familymessagingapp.screen.Screen
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.TimeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
    }

    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private var searchBar: SearchBar? = null

    private var searchBarMenu: Menu? = null

    private var avatarMenu: MenuItem? = null

    private var loadingProgressBarMenu: MenuItem? = null

    private var chatRoomsRecyclerView: RecyclerView? = null

    private var chatroomAdapter: ChatRoomAdapter? = null

    private var usersRecyclerView: RecyclerView? = null

    private var userAdapter: UserAdapter? = null

    private var searchView: SearchView? = null

    private var searchViewEditText: EditText? = null

    private var searchHistoriesRecyclerView: RecyclerView? = null

    private var searchHistoryAdapter: SearchHistoryAdapter? = null

    private var recentSearchHistory: LinearLayout? = null

    private var storyRecyclerView: RecyclerView? = null

    private var storyAdapter: StoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        searchBar = binding.searchBar
        searchView = binding.searchView
        searchViewEditText = searchView?.editText
        searchBarMenu = searchBar?.menu
        avatarMenu = searchBarMenu?.findItem(R.id.avatarMenu)
        loadingProgressBarMenu = searchBarMenu?.findItem(R.id.loadingProgressBarMenu)

        chatRoomsRecyclerView = binding.chatroomRecyclerView
        (chatRoomsRecyclerView?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        chatroomAdapter = ChatRoomAdapter(
            onChatRoomClick = {
                // Because information about the chat room other than messages
                // usually does not change, all this information is transmitted to Chat Room Details
                val action = HomeFragmentDirections.actionHomeFragmentToChatRoomFragment(
                    chatroom = it,
                    userdata = null
                )
                findNavController().navigate(action)
            },
            onChatRoomLongClick = {

            }
        )
        chatRoomsRecyclerView?.adapter = chatroomAdapter

        searchHistoriesRecyclerView = binding.searchHistoriesRecyclerView
        recentSearchHistory = binding.recentSearchHistoryView
        searchHistoryAdapter = SearchHistoryAdapter(
            onDeleteItem = { removeSearchHistory(it, false) },
            onItemClicked = {
                searchViewEditText?.text = Editable.Factory.getInstance().newEditable(it.text)
                onActionSearch()
            },
            onPushItem = {
                searchViewEditText?.text = Editable.Factory.getInstance().newEditable(it.text)
                searchViewEditText?.setSelection(it.text.length)
            }
        )
        searchHistoriesRecyclerView?.adapter = searchHistoryAdapter

        usersRecyclerView = binding.searchResultRecyclerView
        userAdapter = UserAdapter {
            val action = HomeFragmentDirections.actionHomeFragmentToChatRoomFragment(
                chatroom = null,
                userdata = it
            )
            findNavController().navigate(action)
        }
        usersRecyclerView?.adapter = userAdapter

        searchBar?.setOnMenuItemClickListener {
            when (it.itemId) {
                avatarMenu?.itemId -> {
                    findNavController().navigate(Screen.HomeScreen.toProfile())
                    true
                }

//                R.id.QrCodeMenu -> {
//                    findNavController().navigate(R.id.scanQRCodeFragment)
//                    true
//                }

                else -> false
            }
        }

        searchViewEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) onActionSearch()
            false
        }

        searchViewEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchViewStatus(SearchViewStatus.Other)
            }
        })

        binding.btClearAll.setOnClickListener {
            removeSearchHistory(null, true)
        }

//        storyRecyclerView = binding.storyRecyclerView
//        storyRecyclerView?.layoutManager = CarouselLayoutManager()
//        val snapHelper = CarouselSnapHelper()
//        snapHelper.attachToRecyclerView(storyRecyclerView)
//
//        storyAdapter = StoryAdapter()
//        storyRecyclerView?.adapter = storyAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { KeyBoardUtils.setupHideKeyboard(binding.searchView, it) }
//        storyAdapter?.submitList(fakeStories)

        // Khi nhấn nút back trên thiết bị nếu đang show Image Detail thì đóng Image Detail View
        // chứ không back về fragment trước
        activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner) {
            if (searchView?.isShowing == true)
                searchView?.hide()
            else {
                if (findNavController().previousBackStackEntry == null) {
                    activity?.let {
                        (it as MainActivity).handleDoubleBackPress()
                    }
                }
                else findNavController().navigateUp()
            }
        }

        viewModel.authenticated.observe(this.viewLifecycleOwner) { authenticated ->
            if (!authenticated) {
                findNavController().popBackStack()
                findNavController().navigate(Screen.LoginScreen.screenId)
            }
        }

        viewModel.searchHistories.observe(this.viewLifecycleOwner) {
            searchHistoryAdapter?.submitList(it)
            if (it.isNullOrEmpty()) recentSearchHistory?.visibility = View.GONE
        }

        viewModel.searchResultList.observe(this.viewLifecycleOwner) {
            userAdapter?.submitList(it)
        }

        viewModel.searchViewStatus.observe(this.viewLifecycleOwner) {
            when (it) {
                is SearchViewStatus.ShowSearchResult -> {
                    // ẩn searchHistories và hiện search result
                    recentSearchHistory?.visibility = View.GONE
                    binding.isSearchedUserEmpty = viewModel.searchResultList.value.isNullOrEmpty()
                }

                is SearchViewStatus.ShowSearchHistory -> {
                    // Ẩn search result và hiện search hístories nếu có
                    usersRecyclerView?.visibility = View.GONE
                    binding.tvSearchedUserEmpty.visibility = View.GONE
                    recentSearchHistory?.visibility =
                        if (viewModel.searchHistories.value.isNullOrEmpty())
                            View.GONE
                        else
                            View.VISIBLE
                }

                is SearchViewStatus.Other -> {
                    // Hiện Search Histories nếu text trong ô tìm kiếm rỗng
                    if (searchViewEditText?.text.isNullOrEmpty()) {
                        viewModel.setSearchViewStatus(SearchViewStatus.ShowSearchHistory)
                    }
                }
            }
        }

        viewModel.chatRoomsLiveData.observe(this.viewLifecycleOwner) { chatRoomsList ->
            binding.isConversationEmpty = chatRoomsList.isNullOrEmpty()
            chatroomAdapter?.submitList(chatRoomsList)
        }

        viewModel.currentUserLiveData.observe(this.viewLifecycleOwner) {
            it?.let { user ->
                if (!viewModel.isFragmentCreatedFirstTime) {
                    searchBar?.hint = getString(R.string.hi_user, user.username)
                    TimeUtils.startCountdown(countDownTime = 3) {
                        searchBar?.hint = getString(R.string.search)
                    }
                }
                context?.let { context ->
                    _binding?.let {
                        loadingProgressBarMenu?.isVisible = !viewModel.isFragmentCreatedFirstTime
                    }
                    MediaUtils.loadImageWithListener(
                        context = context,
                        photo = user.userAvatar ?: R.drawable.ic_broken_image,
                        actionOnResourceReady = { draw ->
                            viewModel.isFragmentCreatedFirstTime = true
                            lifecycleScope.launch(Dispatchers.Main) {
                                _binding?.let {
                                    loadingProgressBarMenu?.isVisible = false
                                    avatarMenu?.apply {
                                        isVisible = true
                                        icon = draw
                                    }
                                }
                            }
                        },
                        actionOnLoadFailed = {
                            viewModel.isFragmentCreatedFirstTime = true
                            lifecycleScope.launch(Dispatchers.Main) {
                                _binding?.let {
                                    loadingProgressBarMenu?.isVisible = false
                                    avatarMenu?.apply {
                                        isVisible = true
                                        icon = AppCompatResources.getDrawable(
                                            context,
                                            R.drawable.ic_broken_image
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun onActionSearch() {
        activity?.let {
            NetworkChecker.checkNetwork(it) {
                searchViewEditText?.text?.length?.let { length ->
                    searchViewEditText?.setSelection(length)
                }
                viewModel.searchKeyword(searchViewEditText?.text.toString().trim())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchBar = null
        searchView = null
        searchViewEditText = null
        searchBarMenu = null
        avatarMenu = null
        loadingProgressBarMenu = null
        chatRoomsRecyclerView = null
        chatroomAdapter = null
        usersRecyclerView = null
        userAdapter = null
        searchHistoriesRecyclerView = null
        searchHistoryAdapter = null
        storyRecyclerView = null
        storyAdapter = null
        recentSearchHistory = null
        viewModel.setSearchViewStatus(SearchViewStatus.ShowSearchResult)
    }

    private fun removeSearchHistory(searchHistory: SearchHistory?, clearAll: Boolean) {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(
                    if (clearAll) R.string.clear_all_search_history_title
                    else R.string.remove_search_history_title
                )
                .setMessage(
                    if (clearAll) getString(R.string.clear_all_search_history_message)
                    else getString(
                        R.string.remove_search_history_message,
                        searchHistory?.text
                    )
                )
                .setCancelable(true)
                .setPositiveButton(R.string.ok) { _, _ ->
                    if (clearAll) viewModel.clearAllSearchHistory()
                    else viewModel.deleteSearchHistory(searchHistory!!)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
}