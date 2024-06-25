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
import com.android.kotlin.familymessagingapp.databinding.FragmentHomeBinding
import com.android.kotlin.familymessagingapp.utils.AppImageUtils
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.screen.Screen
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
        usersRecyclerView = binding.usersRecyclerView
        chatroomAdapter = ChatRoomAdapter()
        searchHistoriesRecyclerView = binding.searchHistoriesRecyclerView
        recentSearchHistory = binding.recentSearchHistoryView
        userAdapter = UserAdapter { findNavController().navigate(Screen.ChatRoom.screenId) }
        searchHistoryAdapter = SearchHistoryAdapter(
            onDeleteItem = { viewModel.deleteSearchHistory(it) },
            onItemClicked = {
                searchView?.editText?.text = Editable.Factory.getInstance().newEditable(it.text)
            }
        )
        searchHistoriesRecyclerView?.adapter = searchHistoryAdapter
        chatRoomsRecyclerView?.adapter = chatroomAdapter
        usersRecyclerView?.adapter = userAdapter
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
            if (it.isNullOrEmpty()) {
                recentSearchHistory?.visibility = View.GONE
            } else {
                recentSearchHistory?.visibility = View.VISIBLE
                searchHistoryAdapter?.submitList(it)
            }
        }

//        viewModel.isShowSearchedUserResult.observe(this.viewLifecycleOwner) {
//            if (it) {
//                binding.tvSearchedUserEmpty.visibility = View.VISIBLE
//                usersRecyclerView?.visibility = View.VISIBLE
//                searchHistoriesRecyclerView?.visibility = View.GONE
//            } else {
//                binding.tvSearchedUserEmpty.visibility = View.GONE
//                usersRecyclerView?.visibility = View.GONE
//                searchHistoriesRecyclerView?.visibility = View.VISIBLE
//            }
//        }

        viewModel.chatRoomsLiveData.observe(this.viewLifecycleOwner) { chatRoomsList ->
            binding.isConversationEmpty = chatRoomsList.isNullOrEmpty()
            if (chatRoomsList.isNotEmpty()) {
                val sortedList = chatRoomsList.sortedByDescending { chatroom -> chatroom.time }
                chatroomAdapter?.submitList(sortedList)
            }
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
                if (recentSearchHistory?.visibility == View.GONE && viewModel.searchHistories.value?.size != 0) {
                    recentSearchHistory?.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })

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