package com.example.mindlex.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class StudyNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager: WorkManager = WorkManager.getInstance(context)

    suspend fun rescheduleDailyNotifications(
        notificationsEnabled: Boolean,
        preferredStudyTime: LocalTime,
        dailyGoal: Int
    ) {
        if (!notificationsEnabled) {
            cancelAll()
            return
        }

        val sessionTimes = recommendedSessionTimes(preferredStudyTime, dailyGoal)
        sessionTimes.forEachIndexed { index, time ->
            val request = PeriodicWorkRequestBuilder<MainStudyReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayTo(time), TimeUnit.MILLISECONDS)
                .setInputData(
                    androidx.work.workDataOf(
                        MainStudyReminderWorker.KEY_DAILY_GOAL to dailyGoal,
                        MainStudyReminderWorker.KEY_SLOT_INDEX to index + 1
                    )
                )
                .addTag(TAG_STUDY_DAILY)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "study_main_$index",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        val missedGoalRequest = PeriodicWorkRequestBuilder<MissedGoalReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayTo(LocalTime(20, 0, 0)), TimeUnit.MILLISECONDS)
            .addTag(TAG_STUDY_DAILY)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "study_missed_goal",
            ExistingPeriodicWorkPolicy.UPDATE,
            missedGoalRequest
        )

        val dueReviewRequest = PeriodicWorkRequestBuilder<DueReviewReminderWorker>(3, TimeUnit.HOURS)
            .addTag(TAG_DUE_REVIEW)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "study_due_review",
            ExistingPeriodicWorkPolicy.UPDATE,
            dueReviewRequest
        )
    }

    fun recommendedSessionTimes(preferred: LocalTime, dailyGoal: Int): List<LocalTime> {
        val sessionsPerDay = when {
            dailyGoal >= 45 -> 3
            dailyGoal >= 20 -> 2
            else -> 1
        }
        return when (sessionsPerDay) {
            1 -> listOf(preferred)
            2 -> listOf(
                shiftHours(preferred, -4),
                preferred
            )
            else -> listOf(
                shiftHours(preferred, -4),
                preferred,
                shiftHours(preferred, 4)
            )
        }
    }

    private fun shiftHours(base: LocalTime, deltaHours: Int): LocalTime {
        val totalMinutes = ((base.hour + deltaHours) * 60 + base.minute + 24 * 60) % (24 * 60)
        return LocalTime(totalMinutes / 60, totalMinutes % 60, 0)
    }

    private fun calculateDelayTo(time: LocalTime): Long {
        val now = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        val nowDateTime = now.toLocalDateTime(zone)
        var trigger = LocalDateTime(nowDateTime.date, time).toInstant(zone)
        if (trigger <= now) {
            trigger = LocalDateTime(nowDateTime.date.plus(DatePeriod(days = 1)), time).toInstant(zone)
        }
        return (trigger - now).inWholeMilliseconds
    }

    private fun cancelAll() {
        workManager.cancelAllWorkByTag(TAG_STUDY_DAILY)
        workManager.cancelAllWorkByTag(TAG_DUE_REVIEW)
    }

    companion object {
        private const val TAG_STUDY_DAILY = "study_daily"
        private const val TAG_DUE_REVIEW = "study_due_review"
    }
}

@HiltWorker
class MainStudyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        MindLexNotifications.ensureChannel(applicationContext)
        val goal = inputData.getInt(KEY_DAILY_GOAL, 10)
        MindLexNotifications.show(
            context = applicationContext,
            notificationId = 1000 + inputData.getInt(KEY_SLOT_INDEX, 1),
            title = "Пора учиться!",
            message = "Ваша цель: $goal слов сегодня"
        )
        return Result.success()
    }

    companion object {
        const val KEY_DAILY_GOAL = "daily_goal"
        const val KEY_SLOT_INDEX = "slot_index"
    }
}

@HiltWorker
class DueReviewReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val wordProgressRepository: WordProgressRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (settingsRepository.isNotificationsEnabled().firstOrNull() != true) return Result.success()

        val dueCount = wordProgressRepository.countDueReviews(Clock.System.now())
        if (dueCount <= 0) return Result.success()

        MindLexNotifications.ensureChannel(applicationContext)
        MindLexNotifications.show(
            context = applicationContext,
            notificationId = 2001,
            title = "Пора повторить слова",
            message = "Пора повторить $dueCount слов"
        )
        return Result.success()
    }
}

@HiltWorker
class MissedGoalReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val wordProgressRepository: WordProgressRepository
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        if (settingsRepository.isNotificationsEnabled().firstOrNull() != true) return Result.success()

        val dailyGoal = settingsRepository.getDailyGoal().firstOrNull() ?: 10
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(zone).date
        val start = today.atStartOfDayIn(zone)
        val end = today.plus(DatePeriod(days = 1)).atStartOfDayIn(zone)
        val reviewedToday = wordProgressRepository.observeReviewedWordsCountBetween(start, end).firstOrNull() ?: 0

        if (reviewedToday >= dailyGoal) return Result.success()
        MindLexNotifications.ensureChannel(applicationContext)
        MindLexNotifications.show(
            context = applicationContext,
            notificationId = 3001,
            title = "Напоминание о цели",
            message = "Вы ещё не выполнили цель сегодня!"
        )
        return Result.success()
    }
}
