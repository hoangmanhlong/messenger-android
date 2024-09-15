package com.android.kotlin.familymessagingapp.screen.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainViewModel
import com.android.kotlin.familymessagingapp.databinding.FragmentProfileBinding
import com.android.kotlin.familymessagingapp.screen.Screen
import com.android.kotlin.familymessagingapp.screen.confirm_delete_account.ConfirmDeleteAccountFragment
import com.android.kotlin.familymessagingapp.screen.confirm_delete_account.DeleteAccountEventBus
import com.android.kotlin.familymessagingapp.screen.select_language.SelectLanguageBottomSheetDialogFragment
import com.android.kotlin.familymessagingapp.utils.DeviceUtils
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val _viewModel: ProfileViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    private var logOutDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.fragment = this@ProfileFragment

        binding.enabledAI.setOnClickListener { _viewModel.enableAI() }

        binding.deleteAccountView.setOnClickListener {
            ConfirmDeleteAccountFragment().show(
                this.parentFragmentManager,
                ConfirmDeleteAccountFragment.TAG
            )
        }

        binding.logOutView.setOnClickListener { onLogoutViewClick() }
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

        binding.feedbackView.setOnClickListener { activity?.let { _viewModel.composeFeedback(it) } }

        binding.notificationView.setOnClickListener {
            activity?.let { DeviceUtils.openNotificationPermissionSetting(it) }
        }

        binding.myQRCodeView.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myQRFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.isTheEnglishLanguageDisplayed.observe(this.viewLifecycleOwner) {
            binding.isTheEnglishLanguageDisplayed = it
        }

        binding.ivAvatar.setOnClickListener {
            val userData = mainViewModel.currentUserLiveData.value
            userData?.let {
                val action =
                    ProfileFragmentDirections.actionProfileFragmentToProfileDetailFragment(it)
                findNavController().navigate(action)
            }
        }

        mainViewModel.currentUserLiveData.observe(this.viewLifecycleOwner) {
            it?.let { binding.userData = it }
        }

        _viewModel.privateUserData.observe(this.viewLifecycleOwner) {
            binding.turnOnSuggestedAnswers = it?.mobileConfig?.turnOnSuggestedAnswers ?: false
        }

        _viewModel.authenticationStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (!it) {
                    findNavController().apply {
                        popBackStack(Screen.HomeScreen.screenId, true)
                        navigate(Screen.LoginScreen.screenId)
                    }
                }
            }
        }

        _viewModel.areNotificationsEnabledLiveData.observe(this.viewLifecycleOwner) { areNotificationsEnabled ->
            binding.areNotificationsEnabled = areNotificationsEnabled
        }

        _viewModel.isLoading.observe(this.viewLifecycleOwner) { mainViewModel.setIsLoading(it) }
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.saveCurrentNotificationStatus()
        EventBus.getDefault().register(this)
    }

    fun deleteAccount() = _viewModel.deleteAccount()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (logOutDialog?.isShowing == true) logOutDialog?.dismiss()
        logOutDialog = null
        mainViewModel.setIsLoading(false)
    }

    private fun onLogoutViewClick() {
        context?.let {
            if (logOutDialog == null) {
                logOutDialog = DialogUtils.logoutDialog(
                    context = it,
                    onPositiveClick = { _viewModel.logout() }
                )
            }

            if (logOutDialog?.isShowing == false) logOutDialog?.show()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    fun onDeleteAccountEventBus(deleteAccountEventBus: DeleteAccountEventBus) {
        _viewModel.deleteAccount()
        EventBus.getDefault().removeStickyEvent(deleteAccountEventBus)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    fun onSelectLanguageViewClick() {
        SelectLanguageBottomSheetDialogFragment().show(
            this.parentFragmentManager,
            SelectLanguageBottomSheetDialogFragment.TAG
        )
    }
}