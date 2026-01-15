package com.pomowear.presentation.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomowear.data.datastore.SettingsDataStore
import com.pomowear.domain.model.PomodoroSettings
import com.pomowear.domain.model.TimerPhase
import com.pomowear.domain.model.TimerState
import com.pomowear.service.TimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel(
    private val context: Context,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private var timerService: TimerService? = null
    private var bound = false

    private val _timerState = MutableStateFlow<TimerState>(
        TimerState.Idle(
            phase = TimerPhase.WORK,
            remainingMillis = 25 * 60 * 1000L,
            totalMillis = 25 * 60 * 1000L
        )
    )
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            bound = true

            // Observe service state
            viewModelScope.launch {
                timerService?.timerState?.collect { state ->
                    _timerState.value = state
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
        }
    }

    init {
        // Start and bind to service
        val intent = Intent(context, TimerService::class.java)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // Forward settings to service
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { settings ->
                sendSettingsToService(settings)
            }
        }
    }

    private fun sendSettingsToService(settings: PomodoroSettings) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_UPDATE_SETTINGS
            putExtra(TimerService.EXTRA_WORK_DURATION, settings.workDurationMinutes)
            putExtra(TimerService.EXTRA_SHORT_BREAK_DURATION, settings.shortBreakDurationMinutes)
            putExtra(TimerService.EXTRA_LONG_BREAK_DURATION, settings.longBreakDurationMinutes)
            putExtra(TimerService.EXTRA_TEST_MODE, settings.testMode)
        }
        context.startService(intent)
    }

    fun start() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        context.startService(intent)
    }

    fun pause() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun reset() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_RESET
        }
        context.startService(intent)
    }

    fun setPhase(phase: TimerPhase) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_SET_PHASE
            putExtra(TimerService.EXTRA_PHASE, phase.ordinal)
        }
        context.startService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        if (bound) {
            context.unbindService(connection)
            bound = false
        }
    }
}
