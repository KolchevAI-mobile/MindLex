package com.example.mindlex.data.remote.supabase.models

import com.example.mindlex.data.local.entity.VocabularyEntity
import com.example.mindlex.data.local.entity.toDomain
import com.example.mindlex.domain.model.Vocabulary
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

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
    
    @SerialName("synonyms_en")
    @Serializable(with = SynonymsFieldSerializer::class)
    val synonymsEn: List<String> = emptyList(),
    @SerialName("synonyms_de")
    @Serializable(with = SynonymsFieldSerializer::class)
    val synonymsDe: List<String> = emptyList(),
    @SerialName("synonyms_fr")
    @Serializable(with = SynonymsFieldSerializer::class)
    val synonymsFr: List<String> = emptyList(),
    @SerialName("synonyms_es")
    @Serializable(with = SynonymsFieldSerializer::class)
    val synonymsEs: List<String> = emptyList()
) {

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
            "de" -> synonymsDe
            "fr" -> synonymsFr
            "es" -> synonymsEs
            else -> synonymsEn
        }.joinToString(separator = ", ")

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

    fun toVocabulary(targetLang: String): Vocabulary {
        val entity = toVocabularyEntity(targetLang)
        return entity.toDomain()
    }
}

private object SynonymsFieldSerializer : kotlinx.serialization.KSerializer<List<String>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SynonymsField", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonDecoder = decoder as? JsonDecoder ?: return emptyList()
        val element = jsonDecoder.decodeJsonElement()
        return parseSynonyms(element)
    }

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encodeString(value.joinToString(", "))
    }

    private fun parseSynonyms(element: JsonElement): List<String> {
        return when (element) {
            is JsonArray -> element
                .filterIsInstance<JsonPrimitive>()
                .mapNotNull { it.contentOrNull }
                .flatMap(::splitSynonymTokens)
            is JsonPrimitive -> splitSynonymTokens(element.contentOrNull.orEmpty())
            else -> emptyList()
        }.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun splitSynonymTokens(raw: String): List<String> {
        val normalized = raw.trim()
        if (normalized.isEmpty()) return emptyList()
        return normalized.split(',', ';', '|', '/')
    }
}
