package com.pomowear.complication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.pomowear.service.TimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Singleton service that observes TimerService state changes and triggers complication updates.
 * This ensures complications stay in sync with the timer state.
 */
class ComplicationUpdaterService private constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "ComplicationUpdater"

        @Volatile
        private var instance: ComplicationUpdaterService? = null

        /**
         * Gets or creates the singleton instance of ComplicationUpdaterService.
         */
        fun getInstance(context: Context): ComplicationUpdaterService {
            return instance ?: synchronized(this) {
                instance ?: ComplicationUpdaterService(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerService: TimerService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as? TimerService.TimerBinder
                timerService = binder?.getService()
                bound = true
                Log.d(TAG, "Connected to TimerService")
                observeTimerState()
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to TimerService", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
            Log.d(TAG, "Disconnected from TimerService")
        }
    }

    /**
     * Starts the updater service by binding to TimerService and observing state changes.
     */
    fun start() {
        if (bound) {
            Log.d(TAG, "Already bound to TimerService")
            return
        }

        try {
            val intent = Intent(context, TimerService::class.java)
            // Start the service first to ensure it's running
            context.startService(intent)
            // Then bind to it
            val bindSuccess = context.bindService(
                intent,
                connection,
                Context.BIND_AUTO_CREATE
            )

            if (!bindSuccess) {
                Log.w(TAG, "Failed to bind to TimerService")
            } else {
                Log.d(TAG, "Binding to TimerService...")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ComplicationUpdaterService", e)
        }
    }

    /**
     * Stops the updater service by unbinding from TimerService and cancelling coroutines.
     */
    fun stop() {
        if (bound) {
            try {
                context.unbindService(connection)
                bound = false
                Log.d(TAG, "Unbound from TimerService")
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding from TimerService", e)
            }
        }
        serviceScope.cancel()
    }

    /**
     * Observes TimerService state changes and triggers complication updates.
     * Uses distinctUntilChanged and throttling to avoid battery-draining update floods.
     */
    private fun observeTimerState() {
        val service = timerService
        if (service == null) {
            Log.w(TAG, "Cannot observe state - TimerService is null")
            return
        }

        serviceScope.launch {
            try {
                var lastUpdateTime = 0L
                val minUpdateIntervalMs = 60_000L  // 1 minute minimum between running updates

                service.timerState
                    .map { state ->
                        StateSnapshot(
                            stateType = state::class.simpleName ?: "Unknown",
                            phase = state.phase,
                            // Quantize progress to 5% increments (0-20) to reduce noise
                            progressPercent = (state.progress * 20).toInt()
                        )
                    }
                    .distinctUntilChanged()
                    .collect { snapshot ->
                        val now = System.currentTimeMillis()
                        val isRunning = snapshot.stateType == "Running"

                        // Always update immediately on state type or phase changes
                        // Throttle during Running state to once per minute
                        if (!isRunning || (now - lastUpdateTime >= minUpdateIntervalMs)) {
                            Log.d(TAG, "Timer state changed: $snapshot")
                            requestComplicationUpdate()
                            lastUpdateTime = now
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing timer state", e)
            }
        }
    }

    /**
     * Requests all active complications to update.
     */
    private fun requestComplicationUpdate() {
        serviceScope.launch {
            try {
                val component = ComponentName(context, PomodoroComplicationService::class.java)
                val requester = ComplicationDataSourceUpdateRequester.create(
                    context = context,
                    complicationDataSourceComponent = component
                )

                requester.requestUpdateAll()
                Log.d(TAG, "Requested complication update")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request complication update", e)
            }
        }
    }

    /**
     * Simplified state snapshot for detecting changes that require complication updates.
     * Excludes continuously-changing values like remainingMillis to enable effective throttling.
     */
    private data class StateSnapshot(
        val stateType: String,
        val phase: com.pomowear.domain.model.TimerPhase,
        val progressPercent: Int  // 0-20 representing 5% increments
    )
}
