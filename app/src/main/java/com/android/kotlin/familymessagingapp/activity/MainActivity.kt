package com.android.kotlin.familymessagingapp.activity

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.kotlin.familymessagingapp.databinding.ActivityMainBinding
import com.android.kotlin.familymessagingapp.screen.video_call.CallFragment
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val _viewModel: MainViewModel by viewModels()

    private lateinit var connectivityManager: ConnectivityManager

    private var _loadingDialog: Dialog? = null

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    private lateinit var navController: NavController

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            _viewModel.saveNotificationStatus(permissionGranted)
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        splashScreen.setKeepOnScreenCondition { false }
//        theme.applyStyle(R.style.AppTheme, false)
        setContentView(binding.root)
        _loadingDialog = DialogUtils.loadingDialogInitialize(this)
        networkListener()
        if (allPermissionsGranted()) {
            _viewModel.saveNotificationStatus(true)
        } else {
            requestPermissions()
        }
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.appContainer.id) as NavHostFragment
        navController = navHostFragment.navController


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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissions() {
        activityResultLauncher.launch(CallFragment.REQUIRED_PERMISSIONS)
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
//                showNetworkErrorDialogDialog(false)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
//                showNetworkErrorDialogDialog(true)
            }
        }
    }

//    fun showNetworkErrorDialogDialog(show: Boolean) {
//        lifecycleScope.launch(Dispatchers.Main) {
//            _networkErrorDialog?.let {networkErrorDialog ->
//                if (show) networkErrorDialog.show()
//            }
//        }
//    }

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this@MainActivity,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val REQUIRED_PERMISSIONS = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }
}