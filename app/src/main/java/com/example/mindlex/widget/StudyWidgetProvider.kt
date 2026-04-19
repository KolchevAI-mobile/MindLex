package com.example.mindlex.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.mindlex.R
import com.example.mindlex.ui.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

class StudyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            StudyWidgetEntryPoint::class.java
        )
        val settings = entryPoint.settingsRepository()
        val progressRepo = entryPoint.wordProgressRepository()
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()

        val ui = runBlocking {
            val goal = settings.getDailyGoal().first()
            val preferred = settings.getPreferredStudyTime().first()
            val (start, end) = StudyWidgetStateCalculator.todayBounds(zone, now)
            val reviewed = progressRepo.observeReviewedWordsCountBetween(start, end).first()
            StudyWidgetStateCalculator.compute(
                context = appContext,
                now = now,
                zone = zone,
                dailyGoal = goal,
                preferredStudyTime = preferred,
                reviewedToday = reviewed
            )
        }

        val openApp = PendingIntent.getActivity(
            appContext,
            0,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        appWidgetIds.forEach { id ->
            val views = RemoteViews(appContext.packageName, R.layout.widget_study_schedule).apply {
                setTextViewText(R.id.widget_title, ui.title)
                setTextViewText(R.id.widget_subtitle, ui.subtitle)
                setOnClickPendingIntent(R.id.widget_root, openApp)
                if (ui.showProgress) {
                    setViewVisibility(R.id.widget_progress, View.VISIBLE)
                    setProgressBar(R.id.widget_progress, 100, ui.progressPercent, false)
                } else {
                    setViewVisibility(R.id.widget_progress, View.GONE)
                }
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
