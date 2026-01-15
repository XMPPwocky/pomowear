package com.pomowear.complication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.pomowear.domain.model.TimerPhase
import com.pomowear.domain.model.TimerState
import com.pomowear.service.TimerService
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Provides access to the current TimerService state for complications.
 * Handles service binding and gracefully falls back to default state if service is unavailable.
 */
object ComplicationTimerStateProvider {

    private const val TAG = "ComplicationTimerState"
    private const val BINDING_TIMEOUT_MS = 2000L

    /**
     * Gets the current timer state by binding to TimerService.
     * Returns a default Idle state if the service is not available or binding fails.
     */
    suspend fun getCurrentState(context: Context): TimerState {
        return suspendCancellableCoroutine { continuation ->
            var resumed = false
            var connection: ServiceConnection? = null

            // Timeout handler to prevent hanging
            val timeoutRunnable = Runnable {
                if (!resumed) {
                    resumed = true
                    Log.w(TAG, "Service binding timed out, returning default state")
                    connection?.let {
                        try {
                            context.unbindService(it)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error unbinding service on timeout", e)
                        }
                    }
                    continuation.resume(createDefaultState())
                }
            }

            connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    if (!resumed) {
                        resumed = true
                        try {
                            val binder = service as? TimerService.TimerBinder
                            val timerService = binder?.getService()
                            val state = timerService?.timerState?.value ?: createDefaultState()

                            context.unbindService(this)
                            continuation.resume(state)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting timer state from service", e)
                            try {
                                context.unbindService(this)
                            } catch (unbindError: Exception) {
                                Log.e(TAG, "Error unbinding service", unbindError)
                            }
                            continuation.resume(createDefaultState())
                        }
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    if (!resumed) {
                        resumed = true
                        Log.w(TAG, "Service disconnected unexpectedly")
                        continuation.resume(createDefaultState())
                    }
                }
            }

            try {
                val intent = Intent(context, TimerService::class.java)
                val bound = context.bindService(
                    intent,
                    connection,
                    Context.BIND_AUTO_CREATE
                )

                if (!bound) {
                    resumed = true
                    Log.w(TAG, "Failed to bind to TimerService")
                    continuation.resume(createDefaultState())
                } else {
                    // Set timeout
                    android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(timeoutRunnable, BINDING_TIMEOUT_MS)
                }
            } catch (e: Exception) {
                if (!resumed) {
                    resumed = true
                    Log.e(TAG, "Exception while binding to service", e)
                    continuation.resume(createDefaultState())
                }
            }

            continuation.invokeOnCancellation {
                connection?.let {
                    try {
                        context.unbindService(it)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error unbinding service on cancellation", e)
                    }
                }
            }
        }
    }

    /**
     * Creates a default idle state for when the service is unavailable.
     */
    private fun createDefaultState(): TimerState {
        val defaultDurationMillis = 25 * 60 * 1000L
        return TimerState.Idle(
            phase = TimerPhase.WORK,
            remainingMillis = defaultDurationMillis,
            totalMillis = defaultDurationMillis
        )
    }
}
