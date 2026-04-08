package com.example.mindlex.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import com.example.mindlex.domain.usecase.CalculateRecommendedTimes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
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
    @ApplicationContext private val context: Context,
    private val calculateRecommendedTimes: CalculateRecommendedTimes
) {
    private val workManager: WorkManager = WorkManager.getInstance(context)

    suspend fun rescheduleDailyNotifications(
        notificationsEnabled: Boolean,
        preferredStudyTime: LocalTime,
        dailyGoal: Int
    ) {
        cancelAllScheduledNotificationWorks()

        if (!notificationsEnabled) {
            return
        }

        val sessionTimes = calculateRecommendedTimes(preferredStudyTime, dailyGoal)
        sessionTimes.forEachIndexed { index, time ->
            val isMainSlot = time.hour == preferredStudyTime.hour && time.minute == preferredStudyTime.minute
            val request = PeriodicWorkRequestBuilder<MainStudyReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayTo(time), TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        MainStudyReminderWorker.KEY_DAILY_GOAL to dailyGoal,
                        MainStudyReminderWorker.KEY_SLOT_INDEX to index + 1,
                        MainStudyReminderWorker.KEY_IS_MAIN_SLOT to isMainSlot
                    )
                )
                .addTag(TAG_STUDY_DAILY)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "study_main_$index",
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }

        val missedGoalRequest = PeriodicWorkRequestBuilder<MissedGoalReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayTo(LocalTime(20, 0, 0)), TimeUnit.MILLISECONDS)
            .addTag(TAG_STUDY_DAILY)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "study_missed_goal",
            ExistingPeriodicWorkPolicy.REPLACE,
            missedGoalRequest
        )

        val dueReviewRequest = PeriodicWorkRequestBuilder<DueReviewReminderWorker>(3, TimeUnit.HOURS)
            .addTag(TAG_DUE_REVIEW)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "study_due_review",
            ExistingPeriodicWorkPolicy.REPLACE,
            dueReviewRequest
        )
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

    private fun cancelAllScheduledNotificationWorks() {
        (0..2).forEach { workManager.cancelUniqueWork("study_main_$it") }
        workManager.cancelUniqueWork("study_missed_goal")
        workManager.cancelUniqueWork("study_due_review")
    }

    companion object {
        private const val TAG_STUDY_DAILY = "study_daily"
        private const val TAG_DUE_REVIEW = "study_due_review"
    }
}

@HiltWorker
class MainStudyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (settingsRepository.isNotificationsEnabled().firstOrNull() != true) {
            return Result.success()
        }

        MindLexNotifications.ensureChannel(applicationContext)
        val goal = inputData.getInt(KEY_DAILY_GOAL, 10)
        val isMainSlot = inputData.getBoolean(KEY_IS_MAIN_SLOT, true)

        if (isMainSlot) {
            MindLexNotifications.show(
                context = applicationContext,
                notificationId = 1000 + inputData.getInt(KEY_SLOT_INDEX, 1),
                title = "Пора учиться! 📚",
                message = "Ваша цель сегодня: $goal слов"
            )
        } else {
            MindLexNotifications.show(
                context = applicationContext,
                notificationId = 1000 + inputData.getInt(KEY_SLOT_INDEX, 1),
                title = "Время для занятия! 🔁",
                message = "Рекомендованная сессия повторения. Цель на сегодня: $goal слов"
            )
        }
        return Result.success()
    }

    companion object {
        const val KEY_DAILY_GOAL = "daily_goal"
        const val KEY_SLOT_INDEX = "slot_index"
        const val KEY_IS_MAIN_SLOT = "is_main_slot"
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
            title = "Пора повторить! 🔄",
            message = "У вас $dueCount слов на повторение"
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
        val reviewedToday = wordProgressRepository.observeReviewedWordsCountBetween(start, end).first()

        if (reviewedToday >= dailyGoal) return Result.success()
        MindLexNotifications.ensureChannel(applicationContext)
        MindLexNotifications.show(
            context = applicationContext,
            notificationId = 3001,
            title = "Не забудьте! ⏰",
            message = "Вы ещё не выполнили цель сегодня"
        )
        return Result.success()
    }
}
