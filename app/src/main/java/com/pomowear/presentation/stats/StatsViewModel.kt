package com.pomowear.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pomowear.data.datastore.StatsDataStore
import com.pomowear.domain.model.DailyStats
import com.pomowear.util.getCurrentDateString
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

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
            initialValue = DailyStats(date = getCurrentDateString())
        )
}
