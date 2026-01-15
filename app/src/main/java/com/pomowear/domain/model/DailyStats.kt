package com.pomowear.domain.model

/**
 * Daily statistics for pomodoro sessions.
 * Automatically resets at midnight.
 */
data class DailyStats(
    val date: String,                    // Format: "2026-01-15"
    val completedSessions: Int = 0,      // Total sessions completed today
    val totalWorkTimeMillis: Long = 0,   // Total work time today
    val totalBreakTimeMillis: Long = 0   // Total break time today
) {
    /**
     * Total work time in minutes.
     */
    val totalWorkMinutes: Int
        get() = (totalWorkTimeMillis / 1000 / 60).toInt()

    /**
     * Total break time in minutes.
     */
    val totalBreakMinutes: Int
        get() = (totalBreakTimeMillis / 1000 / 60).toInt()

    /**
     * Combined work and break time in minutes.
     */
    val totalMinutes: Int
        get() = totalWorkMinutes + totalBreakMinutes
}
