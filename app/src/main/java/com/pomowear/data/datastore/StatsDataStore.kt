package com.pomowear.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pomowear.domain.model.DailyStats
import com.pomowear.domain.model.TimerPhase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.statsDataStore: DataStore<Preferences> by preferencesDataStore(name = "pomowear_stats")

/**
 * DataStore for daily pomodoro statistics.
 * Automatically resets stats at midnight using date comparison.
 */
class StatsDataStore(private val context: Context) {

    companion object {
        private val LAST_UPDATED_DATE = stringPreferencesKey("last_updated_date")
        private val COMPLETED_SESSIONS = intPreferencesKey("completed_sessions")
        private val TOTAL_WORK_TIME_MILLIS = longPreferencesKey("total_work_time_millis")
        private val TOTAL_BREAK_TIME_MILLIS = longPreferencesKey("total_break_time_millis")
    }

    /**
     * Returns current date in yyyy-MM-dd format.
     */
    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }

    /**
     * Flow of daily stats that automatically resets when date changes.
     */
    val statsFlow: Flow<DailyStats> = context.statsDataStore.data
        .map { preferences ->
            val currentDate = getCurrentDate()
            val storedDate = preferences[LAST_UPDATED_DATE] ?: currentDate

            if (storedDate != currentDate) {
                // Different day - return empty stats
                DailyStats(date = currentDate)
            } else {
                // Same day - return stored stats
                DailyStats(
                    date = currentDate,
                    completedSessions = preferences[COMPLETED_SESSIONS] ?: 0,
                    totalWorkTimeMillis = preferences[TOTAL_WORK_TIME_MILLIS] ?: 0L,
                    totalBreakTimeMillis = preferences[TOTAL_BREAK_TIME_MILLIS] ?: 0L
                )
            }
        }

    /**
     * Records a completed pomodoro session.
     * Increments session count and adds duration to appropriate time counter.
     * Automatically resets stats if date has changed.
     */
    suspend fun recordCompletedSession(phase: TimerPhase, durationMillis: Long) {
        context.statsDataStore.edit { preferences ->
            val currentDate = getCurrentDate()
            val storedDate = preferences[LAST_UPDATED_DATE] ?: currentDate

            if (storedDate != currentDate) {
                // New day - reset stats
                preferences[LAST_UPDATED_DATE] = currentDate
                preferences[COMPLETED_SESSIONS] = 1
                preferences[TOTAL_WORK_TIME_MILLIS] = if (phase == TimerPhase.WORK) durationMillis else 0L
                preferences[TOTAL_BREAK_TIME_MILLIS] = if (phase != TimerPhase.WORK) durationMillis else 0L
            } else {
                // Same day - increment stats
                val currentSessions = preferences[COMPLETED_SESSIONS] ?: 0
                val currentWorkTime = preferences[TOTAL_WORK_TIME_MILLIS] ?: 0L
                val currentBreakTime = preferences[TOTAL_BREAK_TIME_MILLIS] ?: 0L

                preferences[COMPLETED_SESSIONS] = currentSessions + 1

                when (phase) {
                    TimerPhase.WORK -> {
                        preferences[TOTAL_WORK_TIME_MILLIS] = currentWorkTime + durationMillis
                    }
                    TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> {
                        preferences[TOTAL_BREAK_TIME_MILLIS] = currentBreakTime + durationMillis
                    }
                }
            }
        }
    }
}
