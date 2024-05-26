package com.android.kotlin.familymessagingapp.view

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.FragmentLoginBinding
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.Screen
import com.android.kotlin.familymessagingapp.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.authenticationStatus.observe(this.viewLifecycleOwner) { authenticated ->
            if (authenticated) navigateToHomeScreen()
        }

        binding.btLoginWithGoogleAccount.setOnClickListener { onLoginWithGoogleAccountButtonClick() }
        binding.tvSignUp.setOnClickListener { navigateToSignUpScreen() }
        binding.btLoginWithEmail.setOnClickListener { navigateToUserProfile() }
    }

    private fun onLoginWithGoogleAccountButtonClick() {
        context?.let { context ->
            NetworkChecker.checkNetwork(context) {
                viewModel.launchGoogleSignIn(loginWithGoogleAccountLauncher)
            }
        }
    }

    private fun navigateToHomeScreen() {
        findNavController().popBackStack(Screen.LoginScreen.screenId, true)
        findNavController().navigate(R.id.homeFragment)
    }

    private fun navigateToSignUpScreen() {
        findNavController().navigate(Screen.LoginScreen.navigateToSignUpAccountScreenRouteName())
    }

    private fun navigateToUserProfile() {
        findNavController().navigate(Screen.LoginScreen.navigateToSignInWithEmailScreenRouteName())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}