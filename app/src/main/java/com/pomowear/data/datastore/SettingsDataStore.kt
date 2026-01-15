package com.pomowear.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pomowear.domain.model.PomodoroSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pomowear_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val WORK_DURATION = intPreferencesKey("work_duration_minutes")
        private val SHORT_BREAK_DURATION = intPreferencesKey("short_break_duration_minutes")
        private val LONG_BREAK_DURATION = intPreferencesKey("long_break_duration_minutes")
        private val TEST_MODE = booleanPreferencesKey("test_mode")
    }

    val settingsFlow: Flow<PomodoroSettings> = context.dataStore.data.map { prefs ->
        PomodoroSettings(
            workDurationMinutes = prefs[WORK_DURATION] ?: 25,
            shortBreakDurationMinutes = prefs[SHORT_BREAK_DURATION] ?: 5,
            longBreakDurationMinutes = prefs[LONG_BREAK_DURATION] ?: 15,
            testMode = prefs[TEST_MODE] ?: false
        )
    }

    suspend fun updateWorkDuration(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[WORK_DURATION] = minutes
        }
    }

    suspend fun updateShortBreakDuration(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[SHORT_BREAK_DURATION] = minutes
        }
    }

    suspend fun updateLongBreakDuration(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[LONG_BREAK_DURATION] = minutes
        }
    }

    suspend fun updateTestMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TEST_MODE] = enabled
        }
    }
}
