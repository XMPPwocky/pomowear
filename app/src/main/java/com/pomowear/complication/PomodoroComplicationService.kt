package com.pomowear.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.pomowear.MainActivity
import com.pomowear.R
import com.pomowear.domain.model.TimerPhase
import com.pomowear.domain.model.TimerState

/**
 * Complication data source for the Pomodoro Timer.
 * Provides RANGED_VALUE complication showing current phase, countdown, and progress.
 */
class PomodoroComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return when (type) {
            ComplicationType.RANGED_VALUE -> createPreviewData()
            else -> NoDataComplicationData()
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        return when (request.complicationType) {
            ComplicationType.RANGED_VALUE -> {
                val timerState = ComplicationTimerStateProvider.getCurrentState(this)
                createRangedValueData(timerState)
            }
            else -> NoDataComplicationData()
        }
    }

    /**
     * Creates preview data shown in the watch face complication picker.
     */
    private fun createPreviewData(): RangedValueComplicationData {
        val previewState = TimerState.Running(
            phase = TimerPhase.WORK,
            remainingMillis = 15 * 60 * 1000L, // 15 minutes
            totalMillis = 25 * 60 * 1000L      // 25 minutes
        )
        return createRangedValueData(previewState)
    }

    /**
     * Creates RANGED_VALUE complication data from timer state.
     */
    private fun createRangedValueData(state: TimerState): RangedValueComplicationData {
        val text = createComplicationText(state)
        val icon = createComplicationIcon(state)
        val tapIntent = createTapIntent()
        val contentDescription = createContentDescription(state)

        return RangedValueComplicationData.Builder(
            value = state.progress,
            min = 0f,
            max = 1f,
            contentDescription = contentDescription
        )
            .setText(text)
            .setMonochromaticImage(icon)
            .setTapAction(tapIntent)
            .build()
    }

    /**
     * Creates the text to display in the complication based on timer state.
     */
    private fun createComplicationText(state: TimerState): PlainComplicationText {
        return when (state) {
            is TimerState.Idle -> {
                val shortLabel = getShortPhaseLabel(state.phase)
                PlainComplicationText.Builder(text = shortLabel).build()
            }
            is TimerState.Running -> {
                // Format: "W 24:59" (7 chars max)
                val minutes = (state.remainingMillis / 1000 / 60).toInt()
                val seconds = (state.remainingMillis / 1000 % 60).toInt()
                val timeText = String.format("%d:%02d", minutes, seconds)
                val shortLabel = getShortPhaseLabel(state.phase)
                PlainComplicationText.Builder(text = "$shortLabel $timeText").build()
            }
            is TimerState.Paused -> {
                PlainComplicationText.Builder(text = "PAUSED").build()
            }
            is TimerState.Completed -> {
                PlainComplicationText.Builder(text = "DONE!").build()
            }
        }
    }

    /**
     * Creates the icon to display in the complication based on timer state.
     */
    private fun createComplicationIcon(state: TimerState): MonochromaticImage {
        val iconRes = when (state) {
            is TimerState.Paused -> android.R.drawable.ic_media_pause
            is TimerState.Completed -> android.R.drawable.checkbox_on_background
            else -> when (state.phase) {
                TimerPhase.WORK -> android.R.drawable.ic_media_play
                TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK ->
                    android.R.drawable.ic_menu_recent_history
            }
        }

        return MonochromaticImage.Builder(
            Icon.createWithResource(this, iconRes)
        ).build()
    }

    /**
     * Creates the tap action that opens the app when the complication is tapped.
     */
    private fun createTapIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Creates content description for accessibility.
     */
    private fun createContentDescription(state: TimerState): PlainComplicationText {
        val phaseLabel = getPhaseLabel(state.phase)
        val stateDescription = when (state) {
            is TimerState.Idle -> "Idle"
            is TimerState.Running -> {
                val minutes = (state.remainingMillis / 1000 / 60).toInt()
                "$minutes minutes remaining"
            }
            is TimerState.Paused -> "Paused"
            is TimerState.Completed -> "Completed"
        }

        return PlainComplicationText.Builder(
            text = "Pomodoro Timer: $phaseLabel - $stateDescription"
        ).build()
    }

    /**
     * Returns a short label for the timer phase.
     */
    private fun getPhaseLabel(phase: TimerPhase): String {
        return when (phase) {
            TimerPhase.WORK -> "WORK"
            TimerPhase.SHORT_BREAK -> "BREAK"
            TimerPhase.LONG_BREAK -> "REST"
        }
    }

    /**
     * Returns a short single-letter label for the timer phase.
     * Used in complication text to fit within space constraints.
     */
    private fun getShortPhaseLabel(phase: TimerPhase): String {
        return when (phase) {
            TimerPhase.WORK -> "W"
            TimerPhase.SHORT_BREAK -> "B"
            TimerPhase.LONG_BREAK -> "R"
        }
    }
}
