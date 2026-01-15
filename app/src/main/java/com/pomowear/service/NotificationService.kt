package com.pomowear.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pomowear.MainActivity
import com.pomowear.domain.model.TimerPhase
import java.util.concurrent.TimeUnit

class NotificationService(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_PROGRESS = "pomowear_progress"
        private const val CHANNEL_ID_ALERTS = "pomowear_alerts"
        private const val NOTIFICATION_ID_PROGRESS = 1
        private const val NOTIFICATION_ID_COMPLETE = 2
    }

    private val notificationManager = NotificationManagerCompat.from(context)
    private var currentPhase: TimerPhase = TimerPhase.WORK

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            // Progress channel - low importance, no sound/vibration
            val progressChannel = NotificationChannel(
                CHANNEL_ID_PROGRESS,
                "Timer Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current timer progress"
                setShowBadge(false)
            }
            manager.createNotificationChannel(progressChannel)

            // Alerts channel - high importance with vibration
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Timer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timer completion alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            }
            manager.createNotificationChannel(alertsChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun getPhaseLabel(phase: TimerPhase): String = when (phase) {
        TimerPhase.WORK -> "Work"
        TimerPhase.SHORT_BREAK -> "Short Break"
        TimerPhase.LONG_BREAK -> "Long Break"
    }

    fun showTimerRunning(phase: TimerPhase, remainingMillis: Long, totalMillis: Long) {
        currentPhase = phase
        updateTimerProgress(phase, remainingMillis, totalMillis)
    }

    fun updateTimerProgress(phase: TimerPhase, remainingMillis: Long, totalMillis: Long) {
        val progress = ((totalMillis - remainingMillis) * 100 / totalMillis).toInt()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROGRESS)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("${getPhaseLabel(phase)} - ${formatTime(remainingMillis)}")
            .setContentText("Pomodoro timer running")
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(getPendingIntent())
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID_PROGRESS, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun hideTimerProgress() {
        notificationManager.cancel(NOTIFICATION_ID_PROGRESS)
    }

    fun notifyTimerComplete(phase: TimerPhase) {
        // Cancel the progress notification
        hideTimerProgress()

        val (title, text) = when (phase) {
            TimerPhase.WORK -> "Work Complete!" to "Time for a break"
            TimerPhase.SHORT_BREAK -> "Break Over!" to "Ready to focus?"
            TimerPhase.LONG_BREAK -> "Break Over!" to "Ready to focus?"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(getPendingIntent())
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID_COMPLETE, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
