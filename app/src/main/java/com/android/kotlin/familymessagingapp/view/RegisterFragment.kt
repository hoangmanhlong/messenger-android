package com.android.kotlin.familymessagingapp.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.databinding.FragmentRegisterBinding
import com.android.kotlin.familymessagingapp.utils.AppDialog
import com.android.kotlin.familymessagingapp.utils.HideKeyboard
import com.android.kotlin.familymessagingapp.viewmodel.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val _viewModel: SignUpViewModel by viewModels()

    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding!!

    private var dialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        dialog = activity?.let { AppDialog.createLoadingDialog(it) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { HideKeyboard.setupHideKeyboard(view, it) }
        binding.btNavigateUp.setOnClickListener { findNavController().navigateUp() }
//        _viewModel.loginAuthenticationCallStatus.observe(this.viewLifecycleOwner) { callStatus ->
//            callStatus?.let {
//                when (callStatus) {
//                    FirebaseCallStatus.LOADING -> dialog?.show()
//                    FirebaseCallStatus.ERROR -> {
//                        dialog?.dismiss()
//                        binding.tvLoginError.visibility = View.VISIBLE
//                    }
//
//                    FirebaseCallStatus.SUCCESS -> {
//                        dialog?.dismiss()
//                        findNavController().popBackStack(R.id.loginFragment, true)
//                        findNavController().navigate(R.id.homeFragment)
//                    }
//                }
//            }
//        }

        binding.btSignUp.setOnClickListener { _viewModel.signup() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}