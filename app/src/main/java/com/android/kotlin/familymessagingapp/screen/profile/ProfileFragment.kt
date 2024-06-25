package com.android.kotlin.familymessagingapp.screen.profile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.databinding.FragmentPersonalBinding
import com.android.kotlin.familymessagingapp.screen.confirm_delete_account.ConfirmDeleteAccountFragment
import com.android.kotlin.familymessagingapp.screen.select_language.SelectLanguageBottomSheetDialogFragment
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.screen.Screen
import com.android.kotlin.familymessagingapp.utils.StringUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val _viewModel: ProfileViewModel by viewModels()

    private var _binding: FragmentPersonalBinding? = null

    private val binding get() = _binding!!

    private var isDialogShowing: Boolean? = false

    private lateinit var confirmDeleteAccountFragment: ConfirmDeleteAccountFragment

    private lateinit var selectLanguageBottomSheetDialogFragment: SelectLanguageBottomSheetDialogFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalBinding.inflate(inflater, container, false)
        binding.fragment = this@ProfileFragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logOutView.setOnClickListener {
            if (!isDialogShowing!!) onLogoutViewClick()
        }
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

        _viewModel.isTheEnglishLanguageDisplayed.observe(this.viewLifecycleOwner) {
            binding.isTheEnglishLanguageDisplayed = it
        }

        binding.userAvatarCard.setOnClickListener {
            val userData = _viewModel.currentUserLiveData.value
            userData?.let {
                val bundle = Bundle()
                bundle.putParcelable(Constant.USER_DATA_KEY, it)
                findNavController().navigate(
                    Screen.ProfileScreen.toProfileDetail(),
                    bundle
                )
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

        binding.deleteAccountView.setOnClickListener {
            onDeleteAccountViewClick()
        }

        _viewModel.areNotificationsEnabledLiveData.observe(this.viewLifecycleOwner) { areNotificationsEnabled ->
            binding.areNotificationsEnabled = areNotificationsEnabled
        }

        binding.notificationView.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }

        _viewModel.isLoading.observe(this.viewLifecycleOwner) {
            (activity as MainActivity).isShowLoadingDialog(it)
        }

        binding.feedbackView.setOnClickListener {
            activity?.let {
                StringUtils.composeEmail(it, arrayOf(getString(R.string.feedback_mail)), "Error")
            }
        }
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

    private fun onDeleteAccountViewClick() {
        confirmDeleteAccountFragment = ConfirmDeleteAccountFragment(this)
        confirmDeleteAccountFragment.show(
            this.parentFragmentManager,
            ConfirmDeleteAccountFragment.TAG
        )
    }
}