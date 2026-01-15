package com.pomowear.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.pomowear.domain.model.TimerPhase
import com.pomowear.util.formatTime

@Composable
fun CircularTimerDisplay(
    remainingMillis: Long,
    progress: Float,
    phase: TimerPhase,
    modifier: Modifier = Modifier
) {
    val primaryColor = when (phase) {
        TimerPhase.WORK -> MaterialTheme.colorScheme.primary
        TimerPhase.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
        TimerPhase.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
    }
    val trackColor = primaryColor.copy(alpha = 0.3f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc (remaining time)
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * (1f - progress),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = formatTime(remainingMillis),
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
