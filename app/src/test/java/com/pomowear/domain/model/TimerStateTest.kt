package com.pomowear.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TimerStateTest {

    @Test
    fun `progress is 0 when timer at start`() {
        val state = TimerState.Idle(
            phase = TimerPhase.WORK,
            remainingMillis = 1500000L,
            totalMillis = 1500000L
        )
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `progress is 0_5 when timer halfway`() {
        val state = TimerState.Running(
            phase = TimerPhase.WORK,
            remainingMillis = 750000L,
            totalMillis = 1500000L
        )
        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun `progress is 1 when timer complete`() {
        val state = TimerState.Completed(
            phase = TimerPhase.WORK,
            remainingMillis = 0L,
            totalMillis = 1500000L
        )
        assertEquals(1f, state.progress, 0.001f)
    }

    @Test
    fun `progress is 0 when totalMillis is zero`() {
        val state = TimerState.Idle(
            phase = TimerPhase.WORK,
            remainingMillis = 0L,
            totalMillis = 0L
        )
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `progress works for Idle state`() {
        val state = TimerState.Idle(
            phase = TimerPhase.WORK,
            remainingMillis = 900000L,
            totalMillis = 1500000L
        )
        assertEquals(0.4f, state.progress, 0.001f)
    }

    @Test
    fun `progress works for Running state`() {
        val state = TimerState.Running(
            phase = TimerPhase.SHORT_BREAK,
            remainingMillis = 150000L,
            totalMillis = 300000L
        )
        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun `progress works for Paused state`() {
        val state = TimerState.Paused(
            phase = TimerPhase.LONG_BREAK,
            remainingMillis = 450000L,
            totalMillis = 900000L
        )
        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun `progress works for Completed state`() {
        val state = TimerState.Completed(
            phase = TimerPhase.WORK,
            totalMillis = 1500000L
        )
        assertEquals(1f, state.progress, 0.001f)
    }

    @Test
    fun `progress handles near-complete values`() {
        val state = TimerState.Running(
            phase = TimerPhase.WORK,
            remainingMillis = 1000L,
            totalMillis = 1500000L
        )
        val expected = (1500000f - 1000f) / 1500000f
        assertEquals(expected, state.progress, 0.001f)
    }

    @Test
    fun `Completed state has zero remaining by default`() {
        val state = TimerState.Completed(
            phase = TimerPhase.WORK,
            totalMillis = 1500000L
        )
        assertEquals(0L, state.remainingMillis)
    }

    @Test
    fun `Idle state has default phase WORK`() {
        val state = TimerState.Idle(
            remainingMillis = 1500000L,
            totalMillis = 1500000L
        )
        assertEquals(TimerPhase.WORK, state.phase)
    }
}
