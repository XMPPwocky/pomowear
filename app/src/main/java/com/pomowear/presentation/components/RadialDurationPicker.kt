package com.pomowear.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun RadialDurationPicker(
    selectedMinutes: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val durations = listOf(5, 10, 15, 20, 25, 30, 35, 40, 45)
    var currentIndex by remember {
        mutableIntStateOf(durations.indexOf(selectedMinutes).coerceIn(0, durations.size - 1))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Label
        Text(
            text = "Work Duration",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stepper controls
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Decrease button
            FilledTonalIconButton(
                onClick = {
                    if (currentIndex > 0) currentIndex--
                },
                enabled = currentIndex > 0
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease")
            }

            // Duration display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${durations[currentIndex]}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Increase button
            FilledTonalIconButton(
                onClick = {
                    if (currentIndex < durations.size - 1) currentIndex++
                },
                enabled = currentIndex < durations.size - 1
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Increase")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Start button
        Button(
            onClick = { onDurationSelected(durations[currentIndex]) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start")
        }
    }
}
