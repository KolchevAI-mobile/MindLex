package com.example.mindlex.data.local.mapper

import com.example.mindlex.data.local.entity.WordEntity
import com.example.mindlex.domain.model.Definition
import com.example.mindlex.domain.model.Word
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WordLocalMapper {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun toDomain(entity: WordEntity): Word {
        val definitions: List<Definition> =
            json.decodeFromString(entity.definitions)

        return Word(
            id = entity.id,
            word = entity.word,
            translation = entity.translation,
            phonetic = entity.phonetic,
            partOfSpeech = entity.partOfSpeech,
            definitions = definitions,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun fromDomain(word: Word): WordEntity {
        val definitionsJson: String = json.encodeToString(word.definitions)

        return WordEntity(
            id = word.id,
            word = word.word,
            translation = word.translation,
            phonetic = word.phonetic,
            partOfSpeech = word.partOfSpeech,
            definitions = definitionsJson,
            createdAt = word.createdAt,
            updatedAt = word.updatedAt
        )
    }
}
