package com.pomowear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.pomowear.data.datastore.SettingsDataStore
import com.pomowear.presentation.navigation.PomowearNavigation
import com.pomowear.presentation.settings.SettingsViewModel
import com.pomowear.presentation.timer.TimerViewModel
import com.pomowear.theme.PomowearTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var timerViewModel: TimerViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result - app continues either way
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        requestNotificationPermission()

        // Keep screen on while app is visible
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize dependencies
        settingsDataStore = SettingsDataStore(applicationContext)
        timerViewModel = TimerViewModel(applicationContext, settingsDataStore)
        settingsViewModel = SettingsViewModel(settingsDataStore)

        setContent {
            PomowearTheme {
                PomowearNavigation(
                    timerViewModel = timerViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
