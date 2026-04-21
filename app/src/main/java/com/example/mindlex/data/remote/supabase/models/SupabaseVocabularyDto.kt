package com.example.mindlex.data.remote.supabase.models

import com.example.mindlex.data.local.entity.VocabularyEntity
import com.example.mindlex.data.local.entity.toDomain
import com.example.mindlex.domain.model.Vocabulary
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO таблицы vocabulary в Supabase с поддержкой мультиязычности. */
@Serializable
data class SupabaseVocabularyDto(
    val id: String,
    val word_en: String? = null,
    val word_de: String? = null,
    val word_fr: String? = null,
    val word_es: String? = null,
    val word_ru: String,
    val example_en: String? = null,
    val example_de: String? = null,
    val example_fr: String? = null,
    val example_es: String? = null,
    val example_ru: String? = null,
    val phonetic_en: String? = null,
    val phonetic_de: String? = null,
    val phonetic_fr: String? = null,
    val phonetic_es: String? = null,
    @SerialName("part_of_speech")
    val partOfSpeech: String? = null,
    val category: String? = null,
    /** Синонимы/альтернативы на английском (таблица words), через запятую или ; */
    @SerialName("synonyms_en")
    val synonymsEn: String? = null
) {

    /** Конвертирует в Room entity для заданного языка обучения. */
    fun toVocabularyEntity(targetLang: String): VocabularyEntity {
        val now = Clock.System.now()

        val word = when (targetLang) {
            "en" -> word_en
            "de" -> word_de
            "fr" -> word_fr
            "es" -> word_es
            else -> word_en
        } ?: word_ru

        val example = when (targetLang) {
            "en" -> example_en
            "de" -> example_de
            "fr" -> example_fr
            "es" -> example_es
            else -> example_en
        } ?: example_ru

        val phonetic = when (targetLang) {
            "en" -> phonetic_en
            "de" -> phonetic_de
            "fr" -> phonetic_fr
            "es" -> phonetic_es
            else -> phonetic_en
        }

        val synonymsStored = when (targetLang) {
            "en" -> synonymsEn
            else -> null
        }

        return VocabularyEntity(
            id = id,
            targetLanguage = targetLang,
            word = word,
            translation = word_ru,
            example = example,
            phonetic = phonetic,
            partOfSpeech = partOfSpeech,
            category = category ?: "general",
            source = "remote",
            synonymsForeign = synonymsStored,
            lastAccessed = now
        )
    }

    /** Конвертирует в доменную модель для заданного языка обучения. */
    fun toVocabulary(targetLang: String): Vocabulary {
        val entity = toVocabularyEntity(targetLang)
        return entity.toDomain()
    }
}

