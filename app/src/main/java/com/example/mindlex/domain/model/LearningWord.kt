package com.example.mindlex.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Доменная модель слова для обучения.
 *
 * @property id Уникальный идентификатор
 * @property wordForeign Слово на иностранном языке (основной вариант)
 * @property wordNative Перевод на русский (например "яблоко")
 * @property alternativeTranslations Альтернативные варианты перевода (синонимы)
 * @property targetLanguage Целевой язык ("en", "de", "fr", "es")
 * @property example Пример использования
 * @property phonetic Фонетическая транскрипция
 * @property partOfSpeech Часть речи
 * @property category Категория слова
 */
@Serializable
data class Word(
    val id: String,
    val wordForeign: String,
    val wordNative: String,
    val alternativeTranslations: List<String> = emptyList(),
    val targetLanguage: String,
    val example: String? = null,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val category: String = "general"
)
