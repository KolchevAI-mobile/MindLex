package com.example.mindlex.widget

import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface StudyWidgetEntryPoint {
    fun settingsRepository(): SettingsRepository
    fun wordProgressRepository(): WordProgressRepository
}
