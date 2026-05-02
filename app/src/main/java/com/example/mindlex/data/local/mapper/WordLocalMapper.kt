package com.example.mindlex.data.local.mapper

import com.example.mindlex.data.local.entity.WordEntity
import com.example.mindlex.data.remote.api.models.Definition
import com.example.mindlex.domain.model.Word
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WordLocalMapper {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun toDomain(entity: WordEntity): Word {
        val definitions: List<Definition> =
            json.decodeFromString(entity.definitions)

        val exampleText = definitions.firstOrNull()?.definition 
            ?: definitions.firstOrNull()?.example 
            ?: ""

        return Word(
            id = entity.id,
            wordForeign = entity.word,
            wordNative = entity.translation ?: "",
            targetLanguage = "en", 
            example = exampleText.takeIf { it.isNotEmpty() },
            phonetic = entity.phonetic,
            partOfSpeech = entity.partOfSpeech,
            category = "general"
        )
    }

    fun fromDomain(word: Word): WordEntity {
        val definition = Definition(
            definition = word.example ?: "",
            example = word.example,
            synonyms = emptyList(),
            antonyms = emptyList()
        )
        val definitionsJson: String = json.encodeToString(listOf(definition))

        return WordEntity(
            id = word.id,
            word = word.wordForeign,
            translation = word.wordNative,
            phonetic = word.phonetic,
            partOfSpeech = word.partOfSpeech,
            definitions = definitionsJson,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }
}
