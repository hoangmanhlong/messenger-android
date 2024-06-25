package com.android.kotlin.familymessagingapp.activity

import android.app.Dialog
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.kotlin.familymessagingapp.databinding.ActivityMainBinding
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.PermissionUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val _viewModel: MainViewModel by viewModels()

    private lateinit var connectivityManager: ConnectivityManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        _viewModel.saveNotificationStatus(isGranted)
    }

    private var _loadingDialog: Dialog? = null

    private var _networkErrorDialog: MaterialAlertDialogBuilder? = null

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build();

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        splashScreen.setKeepOnScreenCondition { false }
//        theme.applyStyle(R.style.AppTheme, false)
        setContentView(binding.root)
        _networkErrorDialog = DialogUtils.showNetworkNotAvailableDialog(this@MainActivity, {}, {}, {})
        _loadingDialog = DialogUtils.createLoadingDialog(this)
        _viewModel.executeTheJobOnFirstRun()
        networkListener()
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.appContainer.id) as NavHostFragment
        navController = navHostFragment.navController
        PermissionUtils.askNotificationPermission(this, requestPermissionLauncher)
        _viewModel.saveNotificationStatus(PermissionUtils.areNotificationsEnabled(this))

        _viewModel.isLoading.observe(this) {
            showLoadingDialog(it)
        }
    }

    fun isShowLoadingDialog(isLoading: Boolean) {
        _viewModel.setIsLoading(isLoading)
    }

    private fun showLoadingDialog(isShow: Boolean) {
        _loadingDialog?.let {
            if (isShow && !it.isShowing) it.show()
            else it.dismiss()
        }
    }

    fun isTheEnglishLanguageSelected(isTheEnglishLanguageSelected: Boolean) {
        _viewModel.isTheEnglishLanguageSelected(isTheEnglishLanguageSelected)
    }

    fun changeLanguage() {
        _viewModel.changeLanguage()
    }

    override fun onStart() {
        super.onStart()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
//
//    private fun networkListener() {
//        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
//        networkCallback = object : NetworkCallback() {
//            override fun onAvailable(network: Network) {
//                super.onAvailable(network)
//                this@MainActivity.window?.statusBarColor = getPrimaryColor(this@MainActivity)
//            }
//
//            override fun onLost(network: Network) {
//                super.onLost(network)
//                this@MainActivity.window?.statusBarColor =
//                    ContextCompat.getColor(this@MainActivity, R.color.md_theme_light_error)
//            }
//        }
//    }

    private fun networkListener() {
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                showNetworkErrorDialogDialog(false)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                showNetworkErrorDialogDialog(true)
            }
        }
    }

    fun showNetworkErrorDialogDialog(show: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            _networkErrorDialog?.let {networkErrorDialog ->
                if (show) networkErrorDialog.show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _loadingDialog = null
        _binding = null
    }

//    fun getPrimaryColor(context: Context): Int {
//        val typedValue = TypedValue()
//        val theme = context.theme
//        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
//        return typedValue.data
//    }
}