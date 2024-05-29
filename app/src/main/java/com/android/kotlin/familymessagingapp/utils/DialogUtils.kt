package com.android.kotlin.familymessagingapp.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.StringRes
import com.android.kotlin.familymessagingapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogUtils {
    fun createLoadingDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.progress_indicator)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    fun createCommonDialog(
        context: Context,
        @StringRes title: Int,
        @StringRes message: Int,
        cancelable: Boolean,
        @StringRes positiveButtonLabel: Int,
        @StringRes negativeButtonLabel: Int,
        onPositiveClick: () -> Unit?,
        onNegativeClick: () -> Unit?,
        onCancelListener: () -> Unit?
    ): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(cancelable)
            .setPositiveButton(positiveButtonLabel) { _, _ -> onPositiveClick() }
            .setNegativeButton(negativeButtonLabel) { _, _ -> onNegativeClick() }
            .setOnCancelListener { onCancelListener() }
    }

    fun showNetworkNotAvailableDialog(
        context: Context,
        onPositiveClick: () -> Unit?,
        onNegativeClick: () -> Unit?,
        onCancelListener: () -> Unit?
    ): MaterialAlertDialogBuilder {
        return createCommonDialog(
            context = context,
            title = R.string.network_not_available_title,
            message = R.string.network_not_available_message,
            cancelable = true,
            positiveButtonLabel = R.string.try_again,
            negativeButtonLabel = R.string.cancel,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick,
            onCancelListener = onCancelListener
        )
    }
}