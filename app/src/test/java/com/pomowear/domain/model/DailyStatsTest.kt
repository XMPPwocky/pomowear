package com.pomowear.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DailyStatsTest {

    @Test
    fun `default values are correct`() {
        val stats = DailyStats(date = "2026-01-15")
        assertEquals(0, stats.completedSessions)
        assertEquals(0L, stats.totalWorkTimeMillis)
        assertEquals(0L, stats.totalBreakTimeMillis)
    }

    @Test
    fun `totalWorkMinutes returns 0 for zero milliseconds`() {
        val stats = DailyStats(date = "2026-01-15", totalWorkTimeMillis = 0L)
        assertEquals(0, stats.totalWorkMinutes)
    }

    @Test
    fun `totalWorkMinutes returns 1 for one minute`() {
        val stats = DailyStats(date = "2026-01-15", totalWorkTimeMillis = 60000L)
        assertEquals(1, stats.totalWorkMinutes)
    }

    @Test
    fun `totalWorkMinutes truncates partial minutes`() {
        val stats = DailyStats(date = "2026-01-15", totalWorkTimeMillis = 90000L) // 1.5 min
        assertEquals(1, stats.totalWorkMinutes)
    }

    @Test
    fun `totalWorkMinutes handles 25 minute session`() {
        val stats = DailyStats(date = "2026-01-15", totalWorkTimeMillis = 1500000L)
        assertEquals(25, stats.totalWorkMinutes)
    }

    @Test
    fun `totalBreakMinutes returns 0 for zero milliseconds`() {
        val stats = DailyStats(date = "2026-01-15", totalBreakTimeMillis = 0L)
        assertEquals(0, stats.totalBreakMinutes)
    }

    @Test
    fun `totalBreakMinutes returns 5 for five minutes`() {
        val stats = DailyStats(date = "2026-01-15", totalBreakTimeMillis = 300000L)
        assertEquals(5, stats.totalBreakMinutes)
    }

    @Test
    fun `totalBreakMinutes truncates partial minutes`() {
        val stats = DailyStats(date = "2026-01-15", totalBreakTimeMillis = 59999L)
        assertEquals(0, stats.totalBreakMinutes)
    }

    @Test
    fun `totalMinutes sums work and break time`() {
        val stats = DailyStats(
            date = "2026-01-15",
            totalWorkTimeMillis = 1500000L,  // 25 min
            totalBreakTimeMillis = 300000L   // 5 min
        )
        assertEquals(30, stats.totalMinutes)
    }

    @Test
    fun `totalMinutes returns 0 when both are zero`() {
        val stats = DailyStats(date = "2026-01-15")
        assertEquals(0, stats.totalMinutes)
    }

    @Test
    fun `custom values are preserved`() {
        val stats = DailyStats(
            date = "2026-01-15",
            completedSessions = 4,
            totalWorkTimeMillis = 6000000L,  // 100 min
            totalBreakTimeMillis = 1200000L  // 20 min
        )
        assertEquals(4, stats.completedSessions)
        assertEquals(100, stats.totalWorkMinutes)
        assertEquals(20, stats.totalBreakMinutes)
        assertEquals(120, stats.totalMinutes)
    }
}
