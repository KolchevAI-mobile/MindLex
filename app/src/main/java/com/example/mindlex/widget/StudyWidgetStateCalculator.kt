package com.example.mindlex.widget

import android.content.Context
import com.example.mindlex.R
import com.example.mindlex.domain.usecase.CalculateRecommendedTimes
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Состояние виджета: слоты как в [CalculateRecommendedTimes], прогресс — как на дашборде
 * (число повторений сегодня / дневная цель).
 */
data class StudyWidgetUi(
    val title: String,
    val subtitle: String,
    val progressPercent: Int,
    val showProgress: Boolean
)

object StudyWidgetStateCalculator {

    private val recommendedTimes = CalculateRecommendedTimes()

    fun compute(
        context: Context,
        now: Instant,
        zone: TimeZone,
        dailyGoal: Int,
        preferredStudyTime: LocalTime,
        reviewedToday: Int
    ): StudyWidgetUi {
        val goal = dailyGoal.coerceAtLeast(1)
        if (reviewedToday >= goal) {
            return StudyWidgetUi(
                title = context.getString(R.string.widget_well_done_title),
                subtitle = context.getString(R.string.widget_well_done_subtitle),
                progressPercent = 100,
                showProgress = true
            )
        }

        val slots = recommendedTimes(preferredStudyTime, goal)
            .map { LocalDateTime(now.toLocalDateTime(zone).date, it).toInstant(zone) }
            .distinct()
            .sorted()

        val nextFuture = slots.firstOrNull { it > now }

        val progress = ((reviewedToday.toDouble() / goal) * 100).toInt().coerceIn(0, 100)

        return if (nextFuture != null) {
            val slotLocal = nextFuture.toLocalDateTime(zone)
            val timeLabel = formatClock(slotLocal.time)
            val untilMs = nextFuture.toEpochMilliseconds() - now.toEpochMilliseconds()
            val durationText = formatDurationRussian(untilMs)
            StudyWidgetUi(
                title = context.getString(R.string.widget_countdown_title),
                subtitle = context.getString(R.string.widget_countdown_subtitle, timeLabel, durationText),
                progressPercent = progress,
                showProgress = true
            )
        } else {
            StudyWidgetUi(
                title = context.getString(R.string.widget_study_now_title),
                subtitle = context.getString(
                    R.string.widget_study_now_subtitle,
                    reviewedToday,
                    goal
                ),
                progressPercent = progress,
                showProgress = true
            )
        }
    }

    fun todayBounds(zone: TimeZone, now: Instant): Pair<Instant, Instant> {
        val today = now.toLocalDateTime(zone).date
        val start = today.atStartOfDayIn(zone)
        val end = today.plus(DatePeriod(days = 1)).atStartOfDayIn(zone)
        return start to end
    }

    private fun formatClock(time: LocalTime): String =
        "%02d:%02d".format(time.hour, time.minute)

    private fun formatDurationRussian(ms: Long): String {
        if (ms <= 0L) return "скоро"
        val totalMinutes = (ms / 60_000).toInt().coerceAtLeast(1)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "$hours ч $minutes мин"
            hours > 0 -> "$hours ч"
            else -> "$minutes мин"
        }
    }
}
