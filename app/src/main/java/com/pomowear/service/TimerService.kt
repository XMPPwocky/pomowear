package com.pomowear.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.pomowear.MainActivity
import com.pomowear.domain.model.TimerPhase
import com.pomowear.domain.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TimerService : Service() {

    companion object {
        const val ACTION_START = "com.pomowear.START"
        const val ACTION_PAUSE = "com.pomowear.PAUSE"
        const val ACTION_RESET = "com.pomowear.RESET"
        const val ACTION_SET_PHASE = "com.pomowear.SET_PHASE"
        const val ACTION_UPDATE_SETTINGS = "com.pomowear.UPDATE_SETTINGS"

        const val EXTRA_PHASE = "phase"
        const val EXTRA_WORK_DURATION = "work_duration"
        const val EXTRA_SHORT_BREAK_DURATION = "short_break_duration"
        const val EXTRA_LONG_BREAK_DURATION = "long_break_duration"
        const val EXTRA_TEST_MODE = "test_mode"

        private const val CHANNEL_ID = "pomowear_timer_service"
        private const val NOTIFICATION_ID = 1
        private const val ALERT_CHANNEL_ID = "pomowear_alerts"
        private const val ALERT_NOTIFICATION_ID = 2
    }

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    private var workDurationMinutes = 25
    private var shortBreakDurationMinutes = 5
    private var longBreakDurationMinutes = 15
    private var testMode = false

    private val _timerState = MutableStateFlow<TimerState>(
        TimerState.Idle(
            phase = TimerPhase.WORK,
            remainingMillis = 25 * 60 * 1000L,
            totalMillis = 25 * 60 * 1000L
        )
    )
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private lateinit var vibrationService: VibrationService

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        vibrationService = VibrationService(applicationContext)
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESET -> resetTimer()
            ACTION_SET_PHASE -> {
                val phaseOrdinal = intent.getIntExtra(EXTRA_PHASE, 0)
                setPhase(TimerPhase.entries[phaseOrdinal])
            }
            ACTION_UPDATE_SETTINGS -> {
                workDurationMinutes = intent.getIntExtra(EXTRA_WORK_DURATION, 25)
                shortBreakDurationMinutes = intent.getIntExtra(EXTRA_SHORT_BREAK_DURATION, 5)
                longBreakDurationMinutes = intent.getIntExtra(EXTRA_LONG_BREAK_DURATION, 15)
                testMode = intent.getBooleanExtra(EXTRA_TEST_MODE, false)
                // Update idle state if not running
                val currentState = _timerState.value
                if (currentState is TimerState.Idle) {
                    val duration = getDurationForPhase(currentState.phase)
                    _timerState.value = TimerState.Idle(
                        phase = currentState.phase,
                        remainingMillis = duration,
                        totalMillis = duration
                    )
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows timer progress"
                setShowBadge(true)
            }
            manager.createNotificationChannel(serviceChannel)

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Timer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timer completion alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            }
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun buildProgressNotification(phase: TimerPhase, remainingMillis: Long, totalMillis: Long): Notification {
        val progress = ((totalMillis - remainingMillis) * 100 / totalMillis).toInt()
        val phaseLabel = when (phase) {
            TimerPhase.WORK -> "Work"
            TimerPhase.SHORT_BREAK -> "Short Break"
            TimerPhase.LONG_BREAK -> "Long Break"
        }
        val timeStr = formatTime(remainingMillis)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("$phaseLabel - $timeStr")
            .setContentText("Pomodoro timer running")
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(getPendingIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setLocalOnly(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun startTimer() {
        val currentState = _timerState.value
        if (currentState is TimerState.Running) return

        // If completed, reset first
        if (currentState is TimerState.Completed) {
            resetTimer()
            startTimer()
            return
        }

        val remainingMillis = currentState.remainingMillis
        val totalMillis = currentState.totalMillis
        val phase = currentState.phase

        _timerState.value = TimerState.Running(
            phase = phase,
            remainingMillis = remainingMillis,
            totalMillis = totalMillis
        )

        // Start as foreground service
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildProgressNotification(phase, remainingMillis, totalMillis),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var remaining = remainingMillis
            var lastNotificationUpdate = remaining

            while (remaining > 0) {
                delay(100L)
                remaining -= 100L
                _timerState.value = TimerState.Running(
                    phase = phase,
                    remainingMillis = remaining.coerceAtLeast(0L),
                    totalMillis = totalMillis
                )

                // Update notification every second
                if (lastNotificationUpdate - remaining >= 1000L) {
                    val manager = getSystemService(NotificationManager::class.java)
                    manager.notify(NOTIFICATION_ID, buildProgressNotification(phase, remaining, totalMillis))
                    lastNotificationUpdate = remaining
                }
            }

            // Timer completed
            _timerState.value = TimerState.Completed(
                phase = phase,
                totalMillis = totalMillis
            )

            // Stop foreground but keep service running
            stopForeground(STOP_FOREGROUND_REMOVE)

            // Vibrate and show completion notification
            vibrationService.vibrateTimerComplete()
            showCompletionNotification(phase)
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        val currentState = _timerState.value
        if (currentState is TimerState.Running) {
            _timerState.value = TimerState.Paused(
                phase = currentState.phase,
                remainingMillis = currentState.remainingMillis,
                totalMillis = currentState.totalMillis
            )
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun resetTimer() {
        timerJob?.cancel()
        val currentPhase = _timerState.value.phase
        val duration = getDurationForPhase(currentPhase)
        _timerState.value = TimerState.Idle(
            phase = currentPhase,
            remainingMillis = duration,
            totalMillis = duration
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun setPhase(phase: TimerPhase) {
        timerJob?.cancel()
        val duration = getDurationForPhase(phase)
        _timerState.value = TimerState.Idle(
            phase = phase,
            remainingMillis = duration,
            totalMillis = duration
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun getDurationForPhase(phase: TimerPhase): Long {
        if (testMode) {
            return 10 * 1000L // 10 seconds for testing
        }
        return when (phase) {
            TimerPhase.WORK -> workDurationMinutes * 60 * 1000L
            TimerPhase.SHORT_BREAK -> shortBreakDurationMinutes * 60 * 1000L
            TimerPhase.LONG_BREAK -> longBreakDurationMinutes * 60 * 1000L
        }
    }

    private fun showCompletionNotification(phase: TimerPhase) {
        val (title, text) = when (phase) {
            TimerPhase.WORK -> "Work Complete!" to "Time for a break"
            TimerPhase.SHORT_BREAK -> "Break Over!" to "Ready to focus?"
            TimerPhase.LONG_BREAK -> "Break Over!" to "Ready to focus?"
        }

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(getPendingIntent())
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLocalOnly(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(ALERT_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }
}
