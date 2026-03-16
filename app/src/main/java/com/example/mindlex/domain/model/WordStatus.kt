package com.example.mindlex.domain.model

/**
 * Статус изучения слова.
 *
 * @property level Уровень знания слова (0-5)
 */
enum class WordStatus(val level: Int) {
    NEW(0),
    LEARNING(1),
    KNOWN(2),
    REVIEW(3)
}
