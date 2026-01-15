package com.pomowear.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomowear.data.datastore.SettingsDataStore
import com.pomowear.domain.model.PomodoroSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val settings: StateFlow<PomodoroSettings> = settingsDataStore.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PomodoroSettings()
        )

    fun updateWorkDuration(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.updateWorkDuration(minutes)
        }
    }

    fun updateShortBreakDuration(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.updateShortBreakDuration(minutes)
        }
    }

    fun updateLongBreakDuration(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.updateLongBreakDuration(minutes)
        }
    }
}
