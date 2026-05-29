package com.example.mindlex.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Серия на дашборде: обнуляем, если последнее занятие было раньше вчера. */
internal fun resolveVisibleDashboardStreak(
    storedStreak: Int,
    lastStudyDateRaw: String?,
    today: LocalDate
): Int {
    val lastStudyDate = lastStudyDateRaw?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: return 0
    val yesterday = today.minus(DatePeriod(days = 1))
    return when {
        lastStudyDate == today || lastStudyDate == yesterday -> storedStreak
        else -> 0
    }
}
