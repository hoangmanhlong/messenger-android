package com.android.kotlin.familymessagingapp.activity

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.kotlin.familymessagingapp.databinding.ActivityMainBinding
import com.android.kotlin.familymessagingapp.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val _viewModel: MainViewModel by viewModels()

//    private lateinit var connectivityManager: ConnectivityManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        _viewModel.saveNotificationStatus(isGranted)
    }

//    private val networkRequest = NetworkRequest.Builder()
//        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//        .build();
//
//    private lateinit var networkCallback: NetworkCallback

    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        splashScreen.setKeepOnScreenCondition { false }
        setContentView(binding.root)
        _viewModel.executeTheJobOnFirstRun()
//        networkListener()
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.appContainer.id) as NavHostFragment
        navController = navHostFragment.navController
        PermissionUtils.askNotificationPermission(this, requestPermissionLauncher)
        _viewModel.saveNotificationStatus(PermissionUtils.areNotificationsEnabled(this))
    }

//    override fun onStart() {
//        super.onStart()
//        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
//    }
//
//    override fun onStop() {
//        super.onStop()
//        connectivityManager.unregisterNetworkCallback(networkCallback)
//    }
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

//    fun getPrimaryColor(context: Context): Int {
//        val typedValue = TypedValue()
//        val theme = context.theme
//        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
//        return typedValue.data
//    }
}