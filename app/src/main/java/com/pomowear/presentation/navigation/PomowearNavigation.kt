package com.pomowear.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.pomowear.presentation.settings.SettingsScreen
import com.pomowear.presentation.settings.SettingsViewModel
import com.pomowear.presentation.stats.StatsScreen
import com.pomowear.presentation.stats.StatsViewModel
import com.pomowear.presentation.timer.TimerScreen
import com.pomowear.presentation.timer.TimerViewModel

sealed class Screen(val route: String) {
    data object Timer : Screen("timer")
    data object Settings : Screen("settings")
    data object Stats : Screen("stats")
}

@Composable
fun PomowearNavigation(
    timerViewModel: TimerViewModel,
    settingsViewModel: SettingsViewModel,
    statsViewModel: StatsViewModel
) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Screen.Timer.route
    ) {
        composable(Screen.Timer.route) {
            TimerScreen(
                viewModel = timerViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Stats.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                viewModel = statsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
