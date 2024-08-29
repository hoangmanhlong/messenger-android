package com.android.kotlin.familymessagingapp.screen

import com.android.kotlin.familymessagingapp.R

sealed class Screen(val screenId: Int) {
    data object LoginScreen : Screen(R.id.loginFragment) {
        fun toSignInYourAccount(): Int = R.id.action_loginFragment_to_loginWithEmailFragment

        fun toRegister(): Int = R.id.action_loginFragment_to_registerFragment
    }

    data object HomeScreen : Screen(R.id.homeFragment) {
        fun toProfile(): Int = R.id.action_homeFragment_to_personalFragment
    }

    data object LoginWithEmailScreen : Screen(R.id.loginWithEmailFragment) {
        fun toSignUpWithEmail(): Int = R.id.action_loginWithEmailFragment_to_registerFragment
    }

    data object Register : Screen(R.id.registerFragment)

    data object ProfileScreen : Screen(R.id.profileFragment) {
        fun toProfileDetail(): Int = R.id.action_profileFragment_to_profileDetailFragment
    }

    data object ScanQrCode : Screen(R.id.scanQRCodeFragment) {
        fun toChatRoom(): Int = R.id.action_scanQRCodeFragment_to_chatRoomFragment
    }

    data object ChatRoom: Screen(R.id.chatRoomFragment)

    data object  CreateGroupChat: Screen(R.id.createGroupChatFragment)

    data object  ChatRoomDetail: Screen(R.id.chatRoomDetailFragment)
}