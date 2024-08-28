package com.android.kotlin.familymessagingapp.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.android.kotlin.familymessagingapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogUtils {

    fun loadingDialogInitialize(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.progress_indicator)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    private fun createCommonDialog(
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
        onCancelListener: () -> Unit?,
        cancelable: Boolean? = true
    ): MaterialAlertDialogBuilder {
        return createCommonDialog(
            context = context,
            title = R.string.network_not_available_title,
            message = R.string.network_not_available_message,
            cancelable = cancelable ?: true,
            positiveButtonLabel = R.string.try_again,
            negativeButtonLabel = R.string.cancel,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick,
            onCancelListener = onCancelListener
        )
    }

    fun logoutDialog(
        context: Context,
        onPositiveClick: () -> Unit?,
        onNegativeClick: () -> Unit?,
        onCancelListener: () -> Unit?
    ): MaterialAlertDialogBuilder {
        return createCommonDialog(
            context = context,
            title = R.string.logout,
            message = R.string.logout_message,
            cancelable = true,
            positiveButtonLabel = R.string.ok,
            negativeButtonLabel = R.string.cancel,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick,
            onCancelListener = onCancelListener
        )
    }

    fun leaveChatRoomDialog(context: Context, onPositiveClick: () -> Unit?): AlertDialog {
        return createCommonDialog(
            context = context,
            title = R.string.leave_group,
            message = R.string.leave_chatroom_warning,
            cancelable = true,
            positiveButtonLabel = R.string.ok,
            negativeButtonLabel = R.string.cancel,
            onPositiveClick = onPositiveClick,
            onNegativeClick = {},
            onCancelListener = {}
        )
            .create()
    }

    fun showNotificationDialog(
        context: Context,
        @StringRes title: Int? = null,
        @StringRes message: Int,
        cancelable: Boolean = true,
        onOkButtonClick: ((DialogInterface, Int) -> Unit)? = null
    ): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)
            .setMessage(context.getString(message))
            .setPositiveButton(R.string.ok, onOkButtonClick)
            .setCancelable(cancelable)
            .create()
        if (title != null) dialog.setTitle(title)
        return dialog
    }

    fun cameraPermissionRequiredDialog(
        context: Context,
        onPositiveClick: () -> Unit?,
        onNegativeClick: () -> Unit?,
        onCancelListener: () -> Unit? = {}
    ): AlertDialog {
        return createCommonDialog(
            context = context,
            title = R.string.camera_access_denied_title,
            message = R.string.camera_access_denied_message,
            cancelable = false,
            positiveButtonLabel = R.string.go_to_setting,
            negativeButtonLabel = R.string.cancel,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick,
            onCancelListener = onCancelListener
        )
            .create()
    }
}