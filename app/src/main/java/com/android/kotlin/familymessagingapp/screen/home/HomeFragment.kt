package com.android.kotlin.familymessagingapp.screen.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.adapter.ChatRoomAdapter
import com.android.kotlin.familymessagingapp.adapter.UserAdapter
import com.android.kotlin.familymessagingapp.databinding.FragmentHomeBinding
import com.android.kotlin.familymessagingapp.utils.AppImageUtils
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.Screen
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
        userAdapter = UserAdapter {
            findNavController().navigate(R.id.chatRoomFragment)
        }
        chatRoomsRecyclerView?.adapter = chatroomAdapter
        usersRecyclerView?.adapter = userAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { HideKeyboard.setupHideKeyboard(binding.searchViewContent, it) }
        binding.isSearchedUserEmpty = false
        viewModel.authenticated.observe(this.viewLifecycleOwner) { authenticated ->
            if (!authenticated) {
                findNavController().popBackStack()
                findNavController().navigate(Screen.LoginScreen.screenId)
            }
        }

        viewModel.chatRoomsLiveData.observe(this.viewLifecycleOwner) { chatRoomsList ->
            binding.isConversationEmpty = chatRoomsList.isNullOrEmpty()
            if (chatRoomsList.isNotEmpty()) {
                val sortedList  = chatRoomsList.sortedByDescending { chatroom -> chatroom.time }
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
            binding.isSearchedUserEmpty = it.isNullOrEmpty()
            searchView?.let {searchView ->
                if (searchView.isShowing) {
                    userAdapter?.submitList(it)
                }
            }
        }

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

        searchView?.editText?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { // Handle only editor action
                activity?.let {
                    NetworkChecker.checkNetwork(it) {
                        viewModel.searchKeyword(searchView?.editText?.text.toString().trim())
                    }
                }
            }
            false
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