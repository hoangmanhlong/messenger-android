package com.android.kotlin.familymessagingapp.view

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
import com.android.kotlin.familymessagingapp.model.FirebaseCallStatus
import com.android.kotlin.familymessagingapp.utils.AppDialog
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.Screen
import com.android.kotlin.familymessagingapp.viewmodel.LoginWithEmailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginWithEmailFragment : Fragment() {

    private val _viewModel: LoginWithEmailViewModel by viewModels()

    private var _binding: FragmentLoginWithEmailBinding? = null

    private val binding get() = _binding!!


    private var dialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginWithEmailBinding.inflate(inflater, container, false)
        dialog = activity?.let { AppDialog.createLoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { HideKeyboard.setupHideKeyboard(view, it) }

        _viewModel.authenticationStatus.observe(this.viewLifecycleOwner) { authenticated ->
            if (authenticated) {
                findNavController().popBackStack(Screen.LoginScreen.screenId, true)
                findNavController().navigate(R.id.homeFragment)
            }
        }

        _viewModel.loginAuthenticationCallStatus.observe(this.viewLifecycleOwner) { callStatus ->
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

        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog = null
    }
}