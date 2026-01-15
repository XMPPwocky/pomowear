package com.pomowear.domain.model

data class PomodoroSettings(
    val workDurationMinutes: Int = 25,
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val testMode: Boolean = false,
    val askForWorkDuration: Boolean = true
)
