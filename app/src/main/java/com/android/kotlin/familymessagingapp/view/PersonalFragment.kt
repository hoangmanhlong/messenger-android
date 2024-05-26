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
import com.android.kotlin.familymessagingapp.utils.AppDialog
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.viewmodel.SettingViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonalFragment : Fragment() {

    private val viewModel: SettingViewModel by viewModels()

    private var _binding: FragmentPersonalBinding? = null

    private val binding get() = _binding!!

    private var isDialogShowing: Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.logOutView.setOnClickListener {
            if (!isDialogShowing!!) onLogoutViewClick()
        }
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }
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
                AppDialog.createCommonDialog(
                    context = requireContext(),
                    title = R.string.logout,
                    message = R.string.logout_message,
                    cancelable = true,
                    positiveButtonLabel = R.string.ok,
                    negativeButtonLabel = R.string.cancel,
                    onPositiveClick = {
                        FirebaseAuth.getInstance().signOut()
                        findNavController().popBackStack(R.id.homeFragment, true)
                        findNavController().navigate(R.id.loginFragment)
                    },
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