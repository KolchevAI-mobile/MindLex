package com.example.mindlex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mindlex.domain.model.Vocabulary
import kotlinx.datetime.Instant

@Entity(
    tableName = "vocabulary_cache",
    indices = [
        Index(value = ["targetLanguage", "category"])
    ]
)
data class VocabularyEntity(
    @PrimaryKey
    val id: String,
    val targetLanguage: String,
    val word: String,
    val translation: String,
    val example: String?,
    val phonetic: String?,
    val partOfSpeech: String?,
    val category: String,
    val source: String = "remote",
    
    val synonymsForeign: String? = null,
    val lastAccessed: Instant
)

fun VocabularyEntity.toDomain(): Vocabulary =
    Vocabulary(
        id = id,
        targetLanguage = targetLanguage,
        word = word,
        translation = translation,
        example = example,
        phonetic = phonetic,
        partOfSpeech = partOfSpeech,
        category = category,
        synonymsForeign = synonymsForeign.parseSynonymTokens()
    )

private fun String?.parseSynonymTokens(): List<String> {
    if (this.isNullOrBlank()) return emptyList()
    return split(',', ';')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
