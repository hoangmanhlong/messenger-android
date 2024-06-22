package com.android.kotlin.familymessagingapp.screen.login

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.kotlin.familymessagingapp.activity.MainActivity
import com.android.kotlin.familymessagingapp.screen.select_language.SelectLanguageBottomSheetDialogFragment
import com.android.kotlin.familymessagingapp.databinding.FragmentLoginBinding
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.NetworkChecker
import com.android.kotlin.familymessagingapp.utils.Screen
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import dagger.hilt.android.AndroidEntryPoint
import java.util.Arrays

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val _viewModel: LoginViewModel by viewModels()

    private lateinit var callbackManager: CallbackManager

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.fragment = this@LoginFragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel.authenticationStatus.observe(this.viewLifecycleOwner) { authenticated ->
            if (authenticated) navigateToHomeScreen()
        }

        _viewModel.loadingStatus.observe(this.viewLifecycleOwner) {
            (activity as MainActivity).isShowLoadingDialog(it)
        }

        _viewModel.isTheEnglishLanguageDisplayedLiveData.observe(this.viewLifecycleOwner) {
            binding.isTheEnglishLanguageDisplayed = it
        }

        binding.btLoginWithGoogleAccount.root.setOnClickListener { onLoginWithGoogleAccountButtonClick() }

//        binding.btLoginWithYourAccount.setOnClickListener {
//            findNavController().navigate(Screen.LoginScreen.toSignInYourAccount())
//        }
//
//        binding.btSignUp.setOnClickListener {
//            findNavController().navigate(Screen.LoginScreen.toRegister())
//        }

        callbackManager = CallbackManager.Factory.create()
        binding.facebook.setPermissions("email", "public_profile")
        binding.facebook.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    _viewModel.signInWithFacebook(result.accessToken)
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException) {
                }
            },
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity).isShowLoadingDialog(false)
    }

    fun onSelectLanguageViewClick() {
        SelectLanguageBottomSheetDialogFragment().show(
            this.parentFragmentManager,
            SelectLanguageBottomSheetDialogFragment.TAG
        )
    }
}