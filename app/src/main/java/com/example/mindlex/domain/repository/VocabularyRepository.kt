package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.Vocabulary
import com.example.mindlex.domain.model.Word
import kotlinx.coroutines.flow.Flow

/** Репозиторий для получения слов обучения с офлайн-кэшированием. */
interface VocabularyRepository {

    fun getRandomWords(
        limit: Int
    ): Flow<Result<List<Vocabulary>>>

    fun getWordsByCategory(
        category: String,
        limit: Int
    ): Flow<Result<List<Vocabulary>>>

    /** Точное совпадение слова на языке обучения (из кэша Room). */
    suspend fun findVocabularyByForeignWord(foreignWord: String): Vocabulary?

    /**
     * Случайное слово из категории; маппинг [Vocabulary] → [Word] выполняется в data-слое.
     * @param reuseIfAllExcluded при true: если все id в [excludedIds], берётся слово из полного пула
     */
    suspend fun getRandomWordByCategoryExcluding(
        category: String,
        limit: Int,
        excludedIds: Set<String>,
        reuseIfAllExcluded: Boolean
    ): Result<Word>
}
