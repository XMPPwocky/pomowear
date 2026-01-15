package com.pomowear.presentation.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * Stats screen showing today's pomodoro statistics.
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateBack: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
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
        // Header
        item {
            ListHeader {
                Text(
                    text = "Today's Stats",
                    textAlign = TextAlign.Center
                )
            }
        }

        // Completed Sessions
        item {
            Spacer(modifier = Modifier.height(8.dp))
            StatCard(
                label = "Completed Sessions",
                value = "${stats.completedSessions}"
            )
        }

        // Work Time
        item {
            Spacer(modifier = Modifier.height(8.dp))
            StatCard(
                label = "Work Time",
                value = "${stats.totalWorkMinutes} min"
            )
        }

        // Break Time
        item {
            Spacer(modifier = Modifier.height(8.dp))
            StatCard(
                label = "Break Time",
                value = "${stats.totalBreakMinutes} min"
            )
        }

        // Total Time
        item {
            Spacer(modifier = Modifier.height(8.dp))
            StatCard(
                label = "Total Time",
                value = "${stats.totalMinutes} min"
            )
        }

        // Back button
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

/**
 * Card displaying a single stat with label and value.
 */
@Composable
private fun StatCard(
    label: String,
    value: String
) {
    Card(
        onClick = { /* Not clickable */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
