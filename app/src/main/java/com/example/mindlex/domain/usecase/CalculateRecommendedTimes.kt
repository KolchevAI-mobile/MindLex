package com.example.mindlex.domain.usecase

import kotlinx.datetime.LocalTime
import javax.inject.Inject

class CalculateRecommendedTimes @Inject constructor() {

    operator fun invoke(preferredTime: LocalTime, dailyGoal: Int): List<LocalTime> {
        val sessionsNeeded = when {
            dailyGoal <= 10 -> 1
            dailyGoal <= 20 -> 2
            else -> 3
        }
        return when (sessionsNeeded) {
            1 -> listOf(preferredTime)
            2 -> listOf(
                shiftHours(preferredTime, -4),
                preferredTime
            )
            else -> listOf(
                shiftHours(preferredTime, -4),
                preferredTime,
                shiftHours(preferredTime, 4)
            )
        }
    }

    private fun shiftHours(base: LocalTime, deltaHours: Int): LocalTime {
        val totalMinutes = ((base.hour + deltaHours) * 60 + base.minute + 24 * 60) % (24 * 60)
        return LocalTime(totalMinutes / 60, totalMinutes % 60, 0)
    }
}
