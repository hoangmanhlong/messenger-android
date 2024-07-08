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
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistory
import com.android.kotlin.familymessagingapp.databinding.FragmentHomeBinding
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.screen.Screen
import com.android.kotlin.familymessagingapp.utils.AppImageUtils
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
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
        chatroomAdapter = ChatRoomAdapter(
            onChatRoomClick = {
                // Because information about the chat room other than messages
                // usually does not change, all this information is transmitted to Chat Room Details
                val action = HomeFragmentDirections.actionHomeFragmentToChatRoomFragment(it)
                findNavController().navigate(action)
            },
            onChatRoomLongClick = {

            }
        )
        chatRoomsRecyclerView?.adapter = chatroomAdapter

        searchHistoriesRecyclerView = binding.searchHistoriesRecyclerView
        recentSearchHistory = binding.recentSearchHistoryView
        searchHistoryAdapter = SearchHistoryAdapter(
            onDeleteItem = { viewModel.deleteSearchHistory(it) },
            onItemClicked = {
                searchView?.editText?.text = Editable.Factory.getInstance().newEditable(it.text)
            }
        )
        searchHistoriesRecyclerView?.adapter = searchHistoryAdapter

        usersRecyclerView = binding.searchResultRecyclerView
        userAdapter = UserAdapter {
            val chatroom = ChatRoom(
                chatroomName = it.username,
                chatRoomImage = it.userAvatar,
                members = listOf(it.uid!!)
            )
            val action = HomeFragmentDirections.actionHomeFragmentToChatRoomFragment(chatroom)
            findNavController().navigate(action)
        }
        usersRecyclerView?.adapter = userAdapter

        searchBar?.setOnMenuItemClickListener {
            when (it.itemId) {
                avatarMenu?.itemId -> {
                    findNavController().navigate(Screen.HomeScreen.toProfile())
                    true
                }

                R.id.QrCodeMenu -> {
                    findNavController().navigate(R.id.scanQRCodeFragment)
                    true
                }

                else -> false
            }
        }

        searchView?.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) onActionSearch()
            false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { HideKeyboard.setupHideKeyboard(binding.searchViewContent, it) }

        viewModel.authenticated.observe(this.viewLifecycleOwner) { authenticated ->
            if (!authenticated) {
                findNavController().popBackStack()
                findNavController().navigate(Screen.LoginScreen.screenId)
            }
        }

        viewModel.searchHistories.observe(this.viewLifecycleOwner) {
            if (searchView?.isShowing == true) bindSearchHistories(it)
        }

//        viewModel.isShowingSearchResult.observe(this.viewLifecycleOwner) {
//            if (it) {
//                binding.tvSearchedUserEmpty.visibility = View.VISIBLE
//                usersRecyclerView?.visibility = View.VISIBLE
//                recentSearchHistory?.visibility = View.GONE
//            } else {
//                binding.tvSearchedUserEmpty.visibility = View.GONE
//                usersRecyclerView?.visibility = View.GONE
//                recentSearchHistory?.visibility = View.VISIBLE
//            }
//        }

        viewModel.chatRoomsLiveData.observe(this.viewLifecycleOwner) { chatRoomsList ->
            binding.isConversationEmpty = chatRoomsList.isNullOrEmpty()
            chatroomAdapter?.submitList(chatRoomsList)
        }

        viewModel.currentUserLiveData.observe(this.viewLifecycleOwner) {
            it?.let { user ->
                if (!viewModel.isFirstLoadImage) {
                    searchBar?.hint = getString(R.string.hi_user, user.username)
                    startCountdown {
                        searchBar?.hint = getString(R.string.search)
                    }
                }
                context?.let { context ->
                    _binding?.let {
                        loadingProgressBarMenu?.isVisible = !viewModel.isFirstLoadImage
                    }
                    AppImageUtils.loadImageWithListener(
                        context = context,
                        photo = user.userAvatar ?: R.drawable.ic_broken_image,
                        actionOnResourceReady = { draw ->
                            viewModel.isFirstLoadImage = true
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
                            viewModel.isFirstLoadImage = true
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

        viewModel.searchedUser.observe(this.viewLifecycleOwner) {
            searchView?.let { searchView ->
                if (searchView.isShowing) {
                    binding.isSearchedUserEmpty = it.isNullOrEmpty()
                    userAdapter?.submitList(it)
                }
            }
        }

        searchView?.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                binding.tvSearchedUserEmpty.visibility = View.GONE
                binding.searchResultRecyclerView.visibility = View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })

        viewModel.searchViewState.observe(this.viewLifecycleOwner) {
            when (it) {
                SearchView.TransitionState.HIDDEN -> {

                }

                SearchView.TransitionState.SHOWN -> {

                }

                SearchView.TransitionState.HIDING -> {}
                SearchView.TransitionState.SHOWING -> {}
            }
        }

        searchView?.addTransitionListener { searchView, transitionState, transitionState2 ->
            viewModel.setSearchViewState(transitionState2)
            if (transitionState2 == SearchView.TransitionState.HIDDEN) {
                binding.tvSearchedUserEmpty.visibility = View.GONE
                binding.searchResultRecyclerView.visibility = View.GONE
            }

            if (transitionState2 == SearchView.TransitionState.SHOWN)
                bindSearchHistories(viewModel.searchHistories.value)
        }
    }

    private fun bindSearchHistories(list: List<SearchHistory>?) {
        recentSearchHistory?.visibility = if (list.isNullOrEmpty()) View.GONE else View.VISIBLE
        searchHistoryAdapter?.submitList(list)
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
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}