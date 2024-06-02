package com.android.kotlin.familymessagingapp

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.android.kotlin.familymessagingapp.databinding.ActivityMainBinding
import com.android.kotlin.familymessagingapp.utils.PermissionUtils
import com.android.kotlin.familymessagingapp.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val _viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        _viewModel.saveNotificationStatus(isGranted)
    }

    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        _viewModel.executeTheJobOnFirstRun()
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.appContainer.id) as NavHostFragment
        navController = navHostFragment.navController
        PermissionUtils.askNotificationPermission(this, requestPermissionLauncher)
        _viewModel.saveNotificationStatus(PermissionUtils.areNotificationsEnabled(this))
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}