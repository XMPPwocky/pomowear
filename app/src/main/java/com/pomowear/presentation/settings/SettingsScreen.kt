package com.pomowear.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 32.dp,
            start = 8.dp,
            end = 8.dp,
            bottom = 32.dp
        )
    ) {
        item {
            ListHeader {
                Text(
                    text = "Settings",
                    textAlign = TextAlign.Center
                )
            }
        }

        // Work duration
        item {
            DurationSettingRow(
                label = "Work",
                value = settings.workDurationMinutes,
                onDecrease = {
                    if (settings.workDurationMinutes > 1) {
                        viewModel.updateWorkDuration(settings.workDurationMinutes - 1)
                    }
                },
                onIncrease = {
                    if (settings.workDurationMinutes < 60) {
                        viewModel.updateWorkDuration(settings.workDurationMinutes + 1)
                    }
                }
            )
        }

        // Short break duration
        item {
            DurationSettingRow(
                label = "Short Break",
                value = settings.shortBreakDurationMinutes,
                onDecrease = {
                    if (settings.shortBreakDurationMinutes > 1) {
                        viewModel.updateShortBreakDuration(settings.shortBreakDurationMinutes - 1)
                    }
                },
                onIncrease = {
                    if (settings.shortBreakDurationMinutes < 30) {
                        viewModel.updateShortBreakDuration(settings.shortBreakDurationMinutes + 1)
                    }
                }
            )
        }

        // Long break duration
        item {
            DurationSettingRow(
                label = "Long Break",
                value = settings.longBreakDurationMinutes,
                onDecrease = {
                    if (settings.longBreakDurationMinutes > 1) {
                        viewModel.updateLongBreakDuration(settings.longBreakDurationMinutes - 1)
                    }
                },
                onIncrease = {
                    if (settings.longBreakDurationMinutes < 60) {
                        viewModel.updateLongBreakDuration(settings.longBreakDurationMinutes + 1)
                    }
                }
            )
        }

        // Done button
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun DurationSettingRow(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = onDecrease
        ) {
            Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "$value min",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        FilledTonalIconButton(
            onClick = onIncrease
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Increase")
        }
    }
}
