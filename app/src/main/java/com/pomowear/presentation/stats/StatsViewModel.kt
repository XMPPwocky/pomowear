package com.pomowear.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomowear.data.datastore.StatsDataStore
import com.pomowear.domain.model.DailyStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for the stats screen.
 * Observes daily stats from StatsDataStore.
 */
class StatsViewModel(
    private val statsDataStore: StatsDataStore
) : ViewModel() {

    /**
     * Current daily stats as a StateFlow for Compose observation.
     */
    val stats: StateFlow<DailyStats> = statsDataStore.statsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyStats(date = getCurrentDate())
        )

    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }
}
