package com.android.kotlin.familymessagingapp.view

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
import com.android.kotlin.familymessagingapp.databinding.FragmentLoginBinding
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.Screen
import com.android.kotlin.familymessagingapp.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    private val loginWithGoogleAccountLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.signInWithActivityResult(result)
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
        activity?.let { _loadingDialog = DialogUtils.createLoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.authenticationStatus.observe(this.viewLifecycleOwner) { authenticated ->
            if (authenticated) navigateToHomeScreen()
        }

        viewModel.loadingStatus.observe(this.viewLifecycleOwner) { isLoading ->
            showLoadingDialog(isLoading)
        }

        binding.btLoginWithGoogleAccount.setOnClickListener { onLoginWithGoogleAccountButtonClick() }

        binding.btLoginWithEmail.setOnClickListener { navigateToSignInWithEmailScreen() }
    }

    private fun onLoginWithGoogleAccountButtonClick() {
        context?.let { context ->
            NetworkChecker.checkNetwork(context) {
                viewModel.launchGoogleSignIn(loginWithGoogleAccountLauncher)
            }
        }
    }

    private fun navigateToHomeScreen() {
        requireContext()
        findNavController().popBackStack(Screen.LoginScreen.screenId, true)
        findNavController().navigate(R.id.homeFragment)
    }

    private fun navigateToSignInWithEmailScreen() {
        findNavController().navigate(Screen.LoginScreen.navigateToSignInWithEmailScreenRouteName())
    }

    private fun showLoadingDialog(isShow: Boolean) {
        _loadingDialog?.let {
            if (isShow) it.show()
            else it.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showLoadingDialog(false)
        _loadingDialog = null
        _binding = null
    }
}