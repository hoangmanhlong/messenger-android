package com.android.kotlin.familymessagingapp

import android.app.Application
import com.facebook.FacebookSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(this)
    }
}