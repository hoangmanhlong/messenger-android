package com.android.kotlin.familymessagingapp.screen.register

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.databinding.FragmentRegisterBinding
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val _viewModel: RegisterViewModel by viewModels()

    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding!!

    private var _dialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        _dialog = activity?.let { DialogUtils.createLoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { HideKeyboard.setupHideKeyboard(view, it) }
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }

        binding.btSignUp.setOnClickListener {
            context?.let {
                NetworkChecker.checkNetwork(it) {
                    _viewModel.signup()
                }
            }
        }

        _viewModel.buttonRegisterStatus.observe(this.viewLifecycleOwner) {
            it?.let { binding.btSignUp.isEnabled = it }
        }

        binding.etUsername.addTextChangedListener {
            it?.let { username -> _viewModel.setUsername(username.toString()) }
        }

        binding.etPassword.addTextChangedListener {
            it?.let { password -> _viewModel.setPassword(password.toString()) }
        }

        _viewModel.isLoading.observe(this.viewLifecycleOwner) { isLoading ->
            _dialog?.let { dialog ->
                if (isLoading) dialog.show()
                else dialog.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}