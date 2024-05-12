package com.android.kotlin.familymessagingapp.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentLoginBinding
import com.android.kotlin.familymessagingapp.model.FirebaseCallStatus
import com.android.kotlin.familymessagingapp.utils.AppDialog
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private var dialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        dialog = activity?.let { AppDialog.createLoadingDialog(it) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { HideKeyboard.setupHideKeyboard(view, it) }

        viewModel.loginAuthenticationCallStatus.observe(this.viewLifecycleOwner) { callStatus ->
            callStatus?.let {
                when (callStatus) {
                    is FirebaseCallStatus.Error -> {
                        binding.tvLoginError.visibility = View.VISIBLE
                        dialog?.dismiss()
                    }

                    FirebaseCallStatus.Calling -> dialog?.show()
                    FirebaseCallStatus.Success -> dialog?.dismiss()
                }
            }
        }

        viewModel.authenticationStatus.observe(this.viewLifecycleOwner) { authenticated ->
            val currentDestinationId = findNavController().currentDestination?.id
            if (authenticated && currentDestinationId != null) {
                findNavController().popBackStack(currentDestinationId, true)
                findNavController().navigate(R.id.homeFragment)
            }
        }

        viewModel.loginButtonState.observe(this.viewLifecycleOwner) {
            binding.btLogin.isEnabled = it
        }

        binding.etEmail.addTextChangedListener {
            if (binding.tvLoginError.visibility == View.VISIBLE)
                binding.tvLoginError.visibility = View.GONE
            viewModel.setEmail(it.toString().trim())
        }
        binding.etPassword.addTextChangedListener {
            if (binding.tvLoginError.visibility == View.VISIBLE)
                binding.tvLoginError.visibility = View.GONE
            viewModel.setPassword(it.toString().trim())
        }

        binding.btLogin.setOnClickListener {
            context?.let { NetworkChecker.checkNetwork(it) { viewModel.login() } }
        }
        binding.tvSignUp.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_registerFragment) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog = null
    }
}