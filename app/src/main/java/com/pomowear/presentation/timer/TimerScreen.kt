package com.pomowear.presentation.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.pomowear.domain.model.TimerPhase
import com.pomowear.domain.model.TimerState
import com.pomowear.presentation.components.CircularTimerDisplay

@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val timerState by viewModel.timerState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Phase indicator
        Text(
            text = when (timerState.phase) {
                TimerPhase.WORK -> "WORK"
                TimerPhase.SHORT_BREAK -> "SHORT BREAK"
                TimerPhase.LONG_BREAK -> "LONG BREAK"
            },
            style = MaterialTheme.typography.labelMedium,
            color = when (timerState.phase) {
                TimerPhase.WORK -> MaterialTheme.colorScheme.primary
                TimerPhase.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
                TimerPhase.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Circular timer display
        CircularTimerDisplay(
            remainingMillis = timerState.remainingMillis,
            progress = timerState.progress,
            phase = timerState.phase,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start/Pause button
            when (timerState) {
                is TimerState.Running -> {
                    FilledIconButton(
                        onClick = { viewModel.pause() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Pause"
                        )
                    }
                }
                else -> {
                    FilledIconButton(
                        onClick = { viewModel.start() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Start"
                        )
                    }
                }
            }

            // Reset button
            FilledTonalIconButton(
                onClick = { viewModel.reset() }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Reset"
                )
            }

            // Stats button
            IconButton(
                onClick = onNavigateToStats
            ) {
                Icon(
                    imageVector = Icons.Rounded.ShowChart,
                    contentDescription = "Stats"
                )
            }

            // Settings button
            IconButton(
                onClick = onNavigateToSettings
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings"
                )
            }
        }

        // Phase selection chips (only show when idle or completed)
        if (timerState is TimerState.Idle || timerState is TimerState.Completed) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TimerPhase.entries.forEach { phase ->
                    PhaseChip(
                        label = when (phase) {
                            TimerPhase.WORK -> "W"
                            TimerPhase.SHORT_BREAK -> "S"
                            TimerPhase.LONG_BREAK -> "L"
                        },
                        selected = timerState.phase == phase,
                        onClick = { viewModel.setPhase(phase) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhaseChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
