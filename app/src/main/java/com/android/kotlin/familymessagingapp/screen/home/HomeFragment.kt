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
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistory
import com.android.kotlin.familymessagingapp.databinding.FragmentHomeBinding
import com.android.kotlin.familymessagingapp.screen.Screen
import com.android.kotlin.familymessagingapp.utils.AppImageUtils
import com.android.kotlin.familymessagingapp.utils.KeyBoardUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
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
                searchView?.editText?.text = Editable.Factory.getInstance().newEditable(it.text)
                onActionSearch()
            },
            onPushItem = {
                searchView?.editText?.text = Editable.Factory.getInstance().newEditable(it.text)
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

        searchView?.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) onActionSearch()
            false
        }

        searchView?.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setIsShowSearchResult(false)
                binding.tvSearchedUserEmpty.visibility = View.GONE
                usersRecyclerView?.visibility = View.GONE
                recentSearchHistory?.visibility =
                    if (
                        s.isNullOrEmpty()
                        && !viewModel.searchHistories.value.isNullOrEmpty()
                        && !viewModel.isShowSearchResult
                    )
                        View.VISIBLE
                    else
                        View.GONE
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

        viewModel.authenticated.observe(this.viewLifecycleOwner) { authenticated ->
            if (!authenticated) {
                findNavController().popBackStack()
                findNavController().navigate(Screen.LoginScreen.screenId)
            }
        }

        viewModel.searchHistories.observe(this.viewLifecycleOwner) {
            recentSearchHistory?.visibility =
                if (!it.isNullOrEmpty() && !viewModel.isShowSearchResult)
                    View.VISIBLE
                else
                    View.GONE
            searchHistoryAdapter?.submitList(it)
        }

        viewModel.searchResultList.observe(this.viewLifecycleOwner) {
            searchView?.let { searchView ->
                if (searchView.isShowing) {
                    recentSearchHistory?.visibility = View.GONE
                    binding.isSearchedUserEmpty = it.isNullOrEmpty()
                    userAdapter?.submitList(it)
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
                    startCountdown {
                        searchBar?.hint = getString(R.string.search)
                    }
                }
                context?.let { context ->
                    _binding?.let {
                        loadingProgressBarMenu?.isVisible = !viewModel.isFragmentCreatedFirstTime
                    }
                    AppImageUtils.loadImageWithListener(
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
                viewModel.searchKeyword(searchView?.editText?.text.toString().trim())
            }
        }
    }

    private fun startCountdown(onFinish: () -> Unit) {
        var timeRemaining = 3
        val scope = CoroutineScope(Job() + Dispatchers.Main)

        scope.launch {
            while (timeRemaining > 0) {
                delay(1000L) // Wait for 1 second
                timeRemaining--
            }
            onFinish() // Callback when countdown finishes
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchBar = null
        searchView = null
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