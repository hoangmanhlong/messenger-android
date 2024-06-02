package com.android.kotlin.familymessagingapp.components.login

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.components.select_language.SelectLanguageBottomSheetDialogFragment
import com.android.kotlin.familymessagingapp.databinding.FragmentLoginBinding
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val _viewModel: LoginViewModel by viewModels()

    private val loginWithGoogleAccountLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                context?.let { context ->
                    NetworkChecker.checkNetwork(context) {
                        _viewModel.signInWithActivityResult(result)
                    }
                }
            }
        }

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private var _loadingDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.fragment = this@LoginFragment
        activity?.let { _loadingDialog = DialogUtils.createLoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel.authenticationStatus.observe(this.viewLifecycleOwner) { authenticated ->
            if (authenticated) navigateToHomeScreen()
        }

        _viewModel.loadingStatus.observe(this.viewLifecycleOwner) { isLoading ->
            showLoadingDialog(isLoading)
        }

        _viewModel.isTheEnglishLanguageDisplayedLiveData.observe(this.viewLifecycleOwner) {
            binding.isTheEnglishLanguageDisplayed = it
        }

        binding.btLoginWithGoogleAccount.root.setOnClickListener { onLoginWithGoogleAccountButtonClick() }

//        binding.btLoginWithEmail.setOnClickListener { navigateToSignInWithEmailScreen() }
    }

    private fun onLoginWithGoogleAccountButtonClick() {
        context?.let { context ->
            NetworkChecker.checkNetwork(context) {
                _viewModel.launchGoogleSignIn(loginWithGoogleAccountLauncher)
            }
        }
    }

    private fun navigateToHomeScreen() {
        findNavController().popBackStack(Screen.LoginScreen.screenId, true)
        findNavController().navigate(Screen.HomeScreen.screenId)
    }

    private fun navigateToSignInWithEmailScreen() {
        findNavController().navigate(Screen.LoginScreen.toSignInWithEmail())
    }

    private fun showLoadingDialog(isShow: Boolean) {
        _loadingDialog?.let {
            if (isShow && !it.isShowing) it.show()
            else it.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showLoadingDialog(false)
        _loadingDialog = null
        _binding = null
    }

    fun onSelectLanguageViewClick() {
        SelectLanguageBottomSheetDialogFragment().show(
            this.parentFragmentManager,
            SelectLanguageBottomSheetDialogFragment.TAG
        )
    }
}