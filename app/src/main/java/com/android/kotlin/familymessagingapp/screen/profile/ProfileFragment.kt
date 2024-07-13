package com.android.kotlin.familymessagingapp.screen.profile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.BuildConfig
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.databinding.FragmentPersonalBinding
import com.android.kotlin.familymessagingapp.screen.Screen
import com.android.kotlin.familymessagingapp.screen.confirm_delete_account.ConfirmDeleteAccountFragment
import com.android.kotlin.familymessagingapp.screen.select_language.SelectLanguageBottomSheetDialogFragment
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.StringUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val _viewModel: ProfileViewModel by viewModels()

    private var _binding: FragmentPersonalBinding? = null

    private val binding get() = _binding!!

    private var isDialogShowing: Boolean? = false

    private var confirmDeleteAccountFragment: ConfirmDeleteAccountFragment? = null

    private lateinit var selectLanguageBottomSheetDialogFragment: SelectLanguageBottomSheetDialogFragment

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalBinding.inflate(inflater, container, false)
        binding.fragment = this@ProfileFragment

        binding.enabledAI.setOnCheckedChangeListener { _, isChecked ->
            context?.let {
                NetworkChecker.checkNetwork(it) {
                    _viewModel.enableAI(isChecked)
                }
            }
        }

        binding.deleteAccountView.setOnClickListener {
            if (confirmDeleteAccountFragment == null)
                confirmDeleteAccountFragment = ConfirmDeleteAccountFragment(this)
            confirmDeleteAccountFragment?.show(
                this.parentFragmentManager,
                ConfirmDeleteAccountFragment.TAG
            )
        }

        binding.logOutView.setOnClickListener {
            if (!isDialogShowing!!) onLogoutViewClick()
        }
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

        binding.feedbackView.setOnClickListener {
            activity?.let {
                StringUtils.composeEmail(it, arrayOf(getString(R.string.feedback_mail)), null)
            }
        }

        binding.notificationView.setOnClickListener { requestNotificationPermission() }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel.isTheEnglishLanguageDisplayed.observe(this.viewLifecycleOwner) {
            binding.isTheEnglishLanguageDisplayed = it
        }

        binding.userAvatarCard.setOnClickListener {
            val userData = _viewModel.currentUserLiveData.value
            userData?.let {
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToProfileDetailFragment(it)
                findNavController().navigate(action)
            }
        }

        _viewModel.currentUserLiveData.observe(this.viewLifecycleOwner) {
            it?.let { binding.userData = it }
        }

        _viewModel.authenticationStatus.observe(this.viewLifecycleOwner) {
            if (!it) {
                findNavController().apply {
                    popBackStack(Screen.HomeScreen.screenId, true)
                    navigate(Screen.LoginScreen.screenId)
                }
            }
        }

        _viewModel.areNotificationsEnabledLiveData.observe(this.viewLifecycleOwner) { areNotificationsEnabled ->
            binding.areNotificationsEnabled = areNotificationsEnabled
        }

        _viewModel.isLoading.observe(this.viewLifecycleOwner) {
            (activity as MainActivity).isShowLoadingDialog(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestNotificationPermission() {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
        }
        startActivity(intent)
    }

    fun deleteAccount() {
        _viewModel.deleteAccount()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity).isShowLoadingDialog(false)
    }

    private fun onLogoutViewClick() {
        context?.let {
            NetworkChecker.checkNetwork(it) {
                isDialogShowing = true
                DialogUtils.logoutDialog(
                    context = it,
                    onPositiveClick = {
                        _viewModel.logout()
                    },
                    onNegativeClick = {
                        isDialogShowing = false
                        Unit
                    },
                    onCancelListener = {
                        isDialogShowing = false
                        Unit
                    })
                    .show()
            }
        }
    }

    fun onSelectLanguageViewClick() {
        selectLanguageBottomSheetDialogFragment = SelectLanguageBottomSheetDialogFragment()
        selectLanguageBottomSheetDialogFragment.show(
            this.parentFragmentManager,
            SelectLanguageBottomSheetDialogFragment.TAG
        )
    }
}