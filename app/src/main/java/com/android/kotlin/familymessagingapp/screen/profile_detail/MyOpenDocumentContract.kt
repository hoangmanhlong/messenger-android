package com.android.kotlin.familymessagingapp.screen.profile_detail

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

class MyOpenDocumentContract : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        val intent: Intent = super.createIntent(context, input)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }
}