package com.android.kotlin.familymessagingapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object HideKeyboard {

    private fun hideSoftKeyboard(activity: Activity) {
        val currentFocus = activity.currentFocus ?: return
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupHideKeyboard(view: View, activity: Activity) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideSoftKeyboard(activity)
                false
            }
        }

        if (view is ViewGroup) {
            (0 until view.childCount).forEach { i ->
                val innerView = view.getChildAt(i)
                setupHideKeyboard(innerView, activity)
            }
        }
    }

    fun hideKeyboard(view: View, context: Context) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
