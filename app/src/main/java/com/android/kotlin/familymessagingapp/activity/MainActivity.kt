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
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.databinding.ActivityMainBinding
import com.android.kotlin.familymessagingapp.screen.Screen
import com.android.kotlin.familymessagingapp.screen.video_call.CallFragment
import com.android.kotlin.familymessagingapp.utils.DialogUtils
import com.android.kotlin.familymessagingapp.utils.PermissionUtils
import com.android.kotlin.familymessagingapp.utils.TimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val _viewModel: MainViewModel by viewModels()

    private lateinit var connectivityManager: ConnectivityManager

    private var _loadingDialog: Dialog? = null

    private var backPressedOnce = false

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private var _binding: ActivityMainBinding? = null

    private var networkNotificationDialog: AlertDialog? = null

    private lateinit var navigationView: NavigationView

    private lateinit var navigationViewHeader: View

    private lateinit var userImageView: ImageView

    private lateinit var usernameTextView: TextView

    private lateinit var userEmailTextView: TextView

    private lateinit var verifiedMaterialCardView: MaterialCardView

    private val binding get() = _binding!!

    private lateinit var navController: NavController

    private lateinit var drawerLayout: DrawerLayout

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
        navigationViewMapper()
        navigationView.itemIconTintList = null
        networkNotificationDialog = DialogUtils.showNotificationDialog(
            context = this,
            title = R.string.network_not_available_title,
            message = R.string.network_not_available_message
        )
//        if (!NetworkChecker.isNetworkAvailable(this)) {
//            this@MainActivity.window?.statusBarColor = getErrorColor()
////            networkNotificationDialog?.show()
//        }
        _loadingDialog = DialogUtils.loadingDialogInitialize(this)
        networkListener()
        checkNotificationPermission()
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.appContainer.id) as NavHostFragment
        navController = navHostFragment.navController
        _viewModel.isLoading.observe(this) {
            showLoadingDialog(it)
        }

        _viewModel.currentUserLiveData.observe(this) { userdata ->
            if (userdata != null && !userdata.uid.isNullOrEmpty()) {
                val userAvatar = userdata.userAvatar
                if (userAvatar.isNullOrEmpty()) {
                    userImageView.setImageResource(R.drawable.ic_user_default)
                } else {
                    Glide.with(this)
                        .load(userAvatar)
                        .error(R.drawable.ic_broken_image)
                        .placeholder(R.drawable.loading_animation)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .into(userImageView)
                }
                usernameTextView.text = userdata.username
                userEmailTextView.text = userdata.email
                verifiedMaterialCardView.visibility =
                    if (userdata.verified()) View.VISIBLE else View.GONE
            }
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (navController.currentDestination?.id != Screen.HomeScreen.screenId) {
                closeDrawer()
                return@setNavigationItemSelectedListener false
            }

            when (menuItem.itemId) {
                R.id.createGroupChatMenu -> {
                    closeDrawer()
                    navController.navigate(Screen.CreateGroupChat.screenId)
                    false
                }

                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Enable swipe to open the drawer only in HomeFragment. Disable swipe to open
            // the drawer in other fragments
            val updatedLockMode = if (destination.id == Screen.HomeScreen.screenId) {
                DrawerLayout.LOCK_MODE_UNLOCKED
            } else {
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            }
            drawerLayout.setDrawerLockMode(updatedLockMode)
        }
    }

    private fun navigationViewMapper() {
        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView
        navigationViewHeader = navigationView.getHeaderView(0)
        userImageView = navigationViewHeader.findViewById(R.id.userAvatar)
        usernameTextView = navigationViewHeader.findViewById(R.id.tvUsername)
        userEmailTextView = navigationViewHeader.findViewById(R.id.tvEmail)
        verifiedMaterialCardView = navigationViewHeader.findViewById(R.id.verifiedMaterialCardView)
        navigationViewHeader.setOnClickListener {
            closeDrawer()
            navController.navigate(Screen.ProfileScreen.screenId)
        }
    }

    fun isShowLoadingDialog(isLoading: Boolean) {
        _viewModel.setIsLoading(isLoading)
    }

    private fun closeDrawer() = binding.drawerLayout.close()

    private fun showLoadingDialog(isShow: Boolean) {
        _loadingDialog?.let {
            if (isShow && !it.isShowing) it.show()
            else it.dismiss()
        }
    }

    fun handleDoubleBackPress() {
        if (backPressedOnce) {
            finish()
        } else {
            backPressedOnce = true
            Toast.makeText(this, getString(R.string.press_to_exit), Toast.LENGTH_SHORT)
                .show()

            TimeUtils.startCountdown(countDownTime = 2) {
                backPressedOnce = false
            }
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
                lifecycleScope.launch {
//                    this@MainActivity.window?.statusBarColor = getBackgroundColor()
//                    if (networkNotificationDialog?.isShowing == true) {
//                        networkNotificationDialog?.dismiss()
//                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                lifecycleScope.launch {
//                    this@MainActivity.window?.statusBarColor = getErrorColor()
//                    networkNotificationDialog?.show()
                }
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

    private fun getBackgroundColor(): Int {
        val typedValue = TypedValue()
        val theme = this.theme
        theme.resolveAttribute(android.R.attr.background, typedValue, true)
        return typedValue.data
    }

    private fun getErrorColor(): Int {
        return ContextCompat.getColor(this@MainActivity, R.color.md_theme_error)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this@MainActivity,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!allPermissionsGranted()) requestPermissions()
        } else {
            saveNotificationStatus(PermissionUtils.areNotificationsEnabled(this))
        }
    }

    fun saveNotificationStatus(enabled: Boolean) {
        _viewModel.saveNotificationStatus(enabled)
    }

    fun openDrawer() = binding.drawerLayout.open()

    companion object {
        val TAG: String = MainActivity::class.java.simpleName

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val REQUIRED_PERMISSIONS = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }
}