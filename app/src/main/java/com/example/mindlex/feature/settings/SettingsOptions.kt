package com.example.mindlex.feature.settings

import com.example.mindlex.core.constants.LearningDefaults

data class LanguageOption(val code: String, val displayName: String)

data class CategoryOption(val code: String, val displayName: String)

object Languages {
    val ALL = listOf(
        LanguageOption("en", "English"),
        LanguageOption("de", "Deutsch"),
        LanguageOption("fr", "Français"),
        LanguageOption("es", "Español")
    )

    fun getDisplayName(code: String): String =
        ALL.find { it.code == code }?.displayName ?: code
}

object Categories {
    val ALL = listOf(
        CategoryOption("general", "Общие"),
        CategoryOption("food", "Еда"),
        CategoryOption("travel", "Путешествия"),
        CategoryOption("business", "Бизнес"),
        CategoryOption("it", "IT"),
        CategoryOption("sport", "Спорт"),
        CategoryOption("family", "Семья")
    )

    fun getDisplayName(code: String): String = when (code) {
        LearningDefaults.CUSTOM_DATASET_CATEGORY -> "Свой словарь"
        else -> ALL.find { it.code == code }?.displayName ?: code
    }
}
