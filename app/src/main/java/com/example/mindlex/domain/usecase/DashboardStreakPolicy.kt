package com.example.mindlex.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

internal fun resolveVisibleDashboardStreak(
    storedStreak: Int,
    lastStudyDateRaw: String?,
    today: LocalDate
): Int {
    val lastStudyDate = lastStudyDateRaw?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return 0
    val yesterday = today.minus(DatePeriod(days = 1))
    return when {
        lastStudyDate == today -> storedStreak
        lastStudyDate == yesterday -> storedStreak
        lastStudyDate < yesterday -> 0
        else -> 0
    }
}
