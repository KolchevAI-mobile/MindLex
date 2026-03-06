package com.example.mindlex.data.local

import androidx.room.TypeConverter
import com.example.mindlex.domain.model.Definition
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalTypeConverters {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromDefinitionsList(value: List<Definition>?): String? {
        if (value == null) return null
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toDefinitionsList(value: String?): List<Definition>? {
        if (value.isNullOrEmpty()) return null
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }
}
