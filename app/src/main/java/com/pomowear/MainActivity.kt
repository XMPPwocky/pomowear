package com.pomowear

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pomowear.data.datastore.SettingsDataStore
import com.pomowear.presentation.navigation.PomowearNavigation
import com.pomowear.presentation.settings.SettingsViewModel
import com.pomowear.presentation.timer.TimerViewModel
import com.pomowear.service.NotificationService
import com.pomowear.service.VibrationService
import com.pomowear.theme.PomowearTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var vibrationService: VibrationService
    private lateinit var notificationService: NotificationService
    private lateinit var timerViewModel: TimerViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on while app is visible
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize dependencies
        settingsDataStore = SettingsDataStore(applicationContext)
        vibrationService = VibrationService(applicationContext)
        notificationService = NotificationService(applicationContext)
        timerViewModel = TimerViewModel(settingsDataStore, vibrationService, notificationService)
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
}
