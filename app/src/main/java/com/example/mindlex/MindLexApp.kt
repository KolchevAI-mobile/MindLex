package com.example.mindlex

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MindLexApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Инициализация Timber для логирования
        Timber.plant(Timber.DebugTree())
        Timber.d("[MindLexApp] Application создан, Timber инициализирован")
    }
}
