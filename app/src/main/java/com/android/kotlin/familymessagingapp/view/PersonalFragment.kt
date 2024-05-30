package com.android.kotlin.familymessagingapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentPersonalBinding
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.Screen
import com.android.kotlin.familymessagingapp.viewmodel.SettingViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonalFragment : Fragment() {

    private val _viewModel: SettingViewModel by viewModels()

    private var _binding: FragmentPersonalBinding? = null

    private val binding get() = _binding!!

    private var isDialogShowing: Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalBinding.inflate(inflater, container, false)
        binding.fragment = this@PersonalFragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.logOutView.setOnClickListener {
            if (!isDialogShowing!!) onLogoutViewClick()
        }
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

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
                    context = requireContext(),
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
}