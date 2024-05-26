package com.android.kotlin.familymessagingapp.utils

import com.android.kotlin.familymessagingapp.R

sealed class Screen(val screenId: Int) {
    data object LoginScreen : Screen(R.id.loginFragment) {
        fun navigateToSignInWithEmailScreenRouteName(): Int = R.id.action_loginFragment_to_loginWithEmailFragment
        fun navigateToSignUpAccountScreenRouteName(): Int = R.id.action_loginFragment_to_registerFragment
    }

    data object HomeScreen : Screen(R.id.homeFragment) {
        fun navigateToSettingScreen(): Int = R.id.action_homeFragment_to_personalFragment
    }
}