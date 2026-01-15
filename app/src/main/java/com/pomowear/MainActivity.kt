package com.pomowear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.pomowear.data.datastore.SettingsDataStore
import com.pomowear.data.datastore.StatsDataStore
import com.pomowear.presentation.navigation.PomowearNavigation
import com.pomowear.presentation.settings.SettingsViewModel
import com.pomowear.presentation.stats.StatsViewModel
import com.pomowear.presentation.timer.TimerViewModel
import com.pomowear.theme.PomowearTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var statsDataStore: StatsDataStore
    private lateinit var timerViewModel: TimerViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var statsViewModel: StatsViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result - app continues either way
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        requestNotificationPermission()

        // Initialize dependencies
        settingsDataStore = SettingsDataStore(applicationContext)
        statsDataStore = StatsDataStore(applicationContext)
        timerViewModel = TimerViewModel(applicationContext, settingsDataStore)
        settingsViewModel = SettingsViewModel(settingsDataStore)
        statsViewModel = StatsViewModel(statsDataStore)

        setContent {
            PomowearTheme {
                PomowearNavigation(
                    timerViewModel = timerViewModel,
                    settingsViewModel = settingsViewModel,
                    statsViewModel = statsViewModel
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
