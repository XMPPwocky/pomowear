package com.pomowear.presentation.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomowear.data.datastore.SettingsDataStore
import com.pomowear.domain.model.PomodoroSettings
import com.pomowear.domain.model.TimerPhase
import com.pomowear.domain.model.TimerState
import com.pomowear.service.NotificationService
import com.pomowear.service.VibrationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val vibrationService: VibrationService,
    private val notificationService: NotificationService
) : ViewModel() {

    private var timerJob: Job? = null
    private var settings: PomodoroSettings = PomodoroSettings()

    private val _timerState = MutableStateFlow<TimerState>(
        TimerState.Idle(
            phase = TimerPhase.WORK,
            remainingMillis = 25 * 60 * 1000L,
            totalMillis = 25 * 60 * 1000L
        )
    )
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { loadedSettings ->
                settings = loadedSettings
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
    }

    fun start() {
        val currentState = _timerState.value
        if (currentState is TimerState.Running) return

        // If completed, reset first
        if (currentState is TimerState.Completed) {
            reset()
            return start()
        }

        val remainingMillis = currentState.remainingMillis
        val totalMillis = currentState.totalMillis
        val phase = currentState.phase

        _timerState.value = TimerState.Running(
            phase = phase,
            remainingMillis = remainingMillis,
            totalMillis = totalMillis
        )

        // Show persistent notification
        notificationService.showTimerRunning(phase, remainingMillis, totalMillis)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
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
                    notificationService.updateTimerProgress(phase, remaining, totalMillis)
                    lastNotificationUpdate = remaining
                }
            }
            _timerState.value = TimerState.Completed(
                phase = phase,
                totalMillis = totalMillis
            )
            // Vibrate and show completion notification
            vibrationService.vibrateTimerComplete()
            notificationService.notifyTimerComplete(phase)
        }
    }

    fun pause() {
        timerJob?.cancel()
        notificationService.hideTimerProgress()
        val currentState = _timerState.value
        if (currentState is TimerState.Running) {
            _timerState.value = TimerState.Paused(
                phase = currentState.phase,
                remainingMillis = currentState.remainingMillis,
                totalMillis = currentState.totalMillis
            )
        }
    }

    fun reset() {
        timerJob?.cancel()
        notificationService.hideTimerProgress()
        val currentPhase = _timerState.value.phase
        val duration = getDurationForPhase(currentPhase)
        _timerState.value = TimerState.Idle(
            phase = currentPhase,
            remainingMillis = duration,
            totalMillis = duration
        )
    }

    fun setPhase(phase: TimerPhase) {
        timerJob?.cancel()
        notificationService.hideTimerProgress()
        val duration = getDurationForPhase(phase)
        _timerState.value = TimerState.Idle(
            phase = phase,
            remainingMillis = duration,
            totalMillis = duration
        )
    }

    private fun getDurationForPhase(phase: TimerPhase): Long {
        return when (phase) {
            TimerPhase.WORK -> settings.workDurationMinutes * 60 * 1000L
            TimerPhase.SHORT_BREAK -> settings.shortBreakDurationMinutes * 60 * 1000L
            TimerPhase.LONG_BREAK -> settings.longBreakDurationMinutes * 60 * 1000L
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        notificationService.hideTimerProgress()
    }
}
