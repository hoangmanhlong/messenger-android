package com.android.kotlin.familymessagingapp.utils


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkChecker {
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val network = connectivityManager?.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun checkNetwork(context: Context, callback: () -> Unit) {
        if (isNetworkAvailable(context = context)) callback()
        else AppDialog.showNetworkNotAvailableDialog(
            context = context,
            onPositiveClick = { checkNetwork(context, callback) },
            onNegativeClick = {},
            onCancelListener = {}
        ).show()
    }
}
