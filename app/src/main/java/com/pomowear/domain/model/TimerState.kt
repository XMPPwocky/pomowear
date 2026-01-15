package com.pomowear.domain.model

sealed class TimerState {
    abstract val phase: TimerPhase
    abstract val remainingMillis: Long
    abstract val totalMillis: Long

    data class Idle(
        override val phase: TimerPhase = TimerPhase.WORK,
        override val remainingMillis: Long,
        override val totalMillis: Long
    ) : TimerState()

    data class Running(
        override val phase: TimerPhase,
        override val remainingMillis: Long,
        override val totalMillis: Long
    ) : TimerState()

    data class Paused(
        override val phase: TimerPhase,
        override val remainingMillis: Long,
        override val totalMillis: Long
    ) : TimerState()

    data class Completed(
        override val phase: TimerPhase,
        override val remainingMillis: Long = 0L,
        override val totalMillis: Long
    ) : TimerState()

    val progress: Float
        get() = if (totalMillis > 0) {
            (totalMillis - remainingMillis).toFloat() / totalMillis.toFloat()
        } else 0f
}
