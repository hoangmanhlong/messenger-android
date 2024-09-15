package com.android.kotlin.familymessagingapp.screen.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.databinding.FragmentLoginBinding
import com.android.kotlin.familymessagingapp.screen.Screen
import com.android.kotlin.familymessagingapp.screen.select_language.SelectLanguageBottomSheetDialogFragment
import com.android.kotlin.familymessagingapp.services.firebase_services.google_authentication.FindIntentSenderResult
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val _viewModel: LoginViewModel by viewModels()

    private val loginWithGoogleAccountLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && context != null) {
                NetworkChecker.checkNetwork(requireContext()) {
                    _viewModel.signInWithActivityResult(result)
                }
            }
        }

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private var noGoogleAccountDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.fragment = this@LoginFragment
        binding.loginWithGoogleView.setOnClickListener { _viewModel.launchGoogleSignIn() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner) {
            if (findNavController().previousBackStackEntry == null && activity != null)
                    (activity as MainActivity).handleDoubleBackPress()
            else findNavController().navigateUp()
        }

        _viewModel.authenticationStatus.observe(this.viewLifecycleOwner) { authenticated ->
            if (authenticated == null) return@observe

            if (authenticated) navigateToHomeScreen()
            else showErrorDialog(false)
        }

        _viewModel.loadingStatus.observe(this.viewLifecycleOwner) {
            (activity as MainActivity).isShowLoadingDialog(it)
        }

        _viewModel.isTheEnglishLanguageDisplayedLiveData.observe(this.viewLifecycleOwner) {
            binding.isTheEnglishLanguageDisplayed = it
        }

        _viewModel.findIntentSenderStatus.observe(this.viewLifecycleOwner) { status ->
            status?.let {
                when (status) {
                    is FindIntentSenderResult.NoAccountFound -> {
                        showErrorDialog(true)
                    }

                    is FindIntentSenderResult.Success -> {
                        loginWithGoogleAccountLauncher.launch(
                            IntentSenderRequest.Builder(status.intentSender).build()
                        )
                    }

                    is FindIntentSenderResult.Error -> {
                        showErrorDialog(false)
                    }
                }
                _viewModel.setFindIntentSenderStatus(null)
            }
        }
    }

    private fun navigateToHomeScreen() {
        findNavController().apply {
            popBackStack(Screen.LoginScreen.screenId, true)
            navigate(Screen.HomeScreen.screenId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        noGoogleAccountDialog = null
        _viewModel.setFindIntentSenderStatus(null)
        _viewModel.setAuthenticationStatus(null)
        (activity as MainActivity).isShowLoadingDialog(false)
    }

    fun onSelectLanguageViewClick() {
        SelectLanguageBottomSheetDialogFragment().show(
            this.parentFragmentManager,
            SelectLanguageBottomSheetDialogFragment.TAG
        )
    }

    private fun showErrorDialog(isNoGoogleAccountError: Boolean) {
        if (noGoogleAccountDialog == null && activity != null) {
            noGoogleAccountDialog = DialogUtils.showNotificationDialog(
                context = requireActivity(),
                message = if (isNoGoogleAccountError) R.string.no_google_account_warning else R.string.error_occurred
            )
        }
        noGoogleAccountDialog?.show()
    }
}