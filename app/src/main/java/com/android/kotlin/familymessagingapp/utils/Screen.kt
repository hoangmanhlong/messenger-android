package com.android.kotlin.familymessagingapp.utils

import com.android.kotlin.familymessagingapp.R

sealed class Screen(val screenId: Int) {
    data object LoginScreen : Screen(R.id.loginFragment) {
        fun navigateToSignInWithEmailScreenRouteName(): Int = R.id.action_loginFragment_to_loginWithEmailFragment
    }

    data object HomeScreen : Screen(R.id.homeFragment) {
        fun navigateToSettingScreen(): Int = R.id.action_homeFragment_to_personalFragment
    }

    data object LoginWithEmailScreen : Screen(R.id.loginWithEmailFragment) {
        fun navigateToSignUpAccountWithEmailScreen(): Int = R.id.action_loginWithEmailFragment_to_registerFragment
    }
}