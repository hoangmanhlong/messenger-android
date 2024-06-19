package com.android.kotlin.familymessagingapp.screen.profile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentPersonalBinding
import com.android.kotlin.familymessagingapp.screen.confirm_delete_account.ConfirmDeleteAccountFragment
import com.android.kotlin.familymessagingapp.screen.profile_detail.MyOpenDocumentContract
import com.android.kotlin.familymessagingapp.screen.select_language.SelectLanguageBottomSheetDialogFragment
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val _viewModel: ProfileViewModel by viewModels()

    private var _binding: FragmentPersonalBinding? = null

    private val binding get() = _binding!!

    private var isDialogShowing: Boolean? = false


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

        binding.ivAvatar.setOnClickListener {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        isDialogShowing = null
    }

    private fun onLogoutViewClick() {
        context?.let {
            NetworkChecker.checkNetwork(it) {
                isDialogShowing = true
                DialogUtils.createCommonDialog(
                    context = it,
                    title = R.string.logout,
                    message = R.string.logout_message,
                    cancelable = true,
                    positiveButtonLabel = R.string.ok,
                    negativeButtonLabel = R.string.cancel,
                    onPositiveClick = { _viewModel.logout() },
                    onNegativeClick = {
                        isDialogShowing = false
                        Unit
                    },
                    onCancelListener = {
                        isDialogShowing = false
                        Unit
                    }
                ).show()
            }
        }
    }

    fun onSelectLanguageViewClick() {
        SelectLanguageBottomSheetDialogFragment().show(
            this.parentFragmentManager,
            SelectLanguageBottomSheetDialogFragment.TAG
        )
    }

    private fun onDeleteAccountViewClick() {
        ConfirmDeleteAccountFragment().show(
            this.parentFragmentManager,
            ConfirmDeleteAccountFragment.TAG
        )
    }
}