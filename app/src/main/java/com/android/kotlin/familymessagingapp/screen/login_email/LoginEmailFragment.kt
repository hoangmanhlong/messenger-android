package com.android.kotlin.familymessagingapp.screen.login_email

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentLoginWithEmailBinding
import com.android.kotlin.familymessagingapp.model.AuthenticationStatus
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginEmailFragment : Fragment() {

    private val _viewModel: LoginEmailViewModel by viewModels()

    private var _binding: FragmentLoginWithEmailBinding? = null

    private val binding get() = _binding!!

    private var _loadingDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginWithEmailBinding.inflate(inflater, container, false)
        _loadingDialog = activity?.let { DialogUtils.createLoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { HideKeyboard.setupHideKeyboard(view, it) }

        _viewModel.authenticationStatus.observe(this.viewLifecycleOwner) {
            when(it) {
                AuthenticationStatus.SUCCESS -> {
                    findNavController().popBackStack(Screen.LoginScreen.screenId, true)
                    findNavController().navigate(R.id.homeFragment)
                }
                AuthenticationStatus.FAILURE -> binding.tvLoginError.visibility = View.VISIBLE
                AuthenticationStatus.NONE -> binding.tvLoginError.visibility = View.GONE
            }
        }

        _viewModel.isLoading.observe(this.viewLifecycleOwner) { isLoading ->
            showLoadingDialog(isLoading)
        }

        _viewModel.loginButtonState.observe(this.viewLifecycleOwner) {
            binding.btLogin.isEnabled = it
        }

        binding.btLogin.setOnClickListener {
            context?.let { NetworkChecker.checkNetwork(it) { _viewModel.loginWithEmail() } }
        }

        binding.etEmail.addTextChangedListener {
            if (binding.tvLoginError.visibility == View.VISIBLE)
                binding.tvLoginError.visibility = View.GONE
            _viewModel.setEmail(it.toString().trim())
        }

        binding.etPassword.addTextChangedListener {
            if (binding.tvLoginError.visibility == View.VISIBLE)
                binding.tvLoginError.visibility = View.GONE
            _viewModel.setPassword(it.toString().trim())
        }

        binding.tvSignUp.setOnClickListener { navigateToSignUpScreen() }
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }
    }

    private fun navigateToSignUpScreen() {
        findNavController().navigate(Screen.LoginWithEmailScreen.toSignUpWithEmail())
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
        _binding = null
        _loadingDialog = null
        _viewModel.setAuthenticationStatusNone()
    }
}