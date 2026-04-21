package com.example.mindlex.data.repository

import com.example.mindlex.data.local.entity.VocabularyEntity
import com.example.mindlex.data.local.repository.VocabularyLocalDataSource
import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.DatasetImportPayload
import com.example.mindlex.domain.model.VocabularySource
import com.example.mindlex.domain.repository.CustomDatasetRepository
import com.example.mindlex.domain.repository.SettingsRepository
import java.security.MessageDigest
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

class CustomDatasetRepositoryImpl @Inject constructor(
    private val localDataSource: VocabularyLocalDataSource,
    private val settingsRepository: SettingsRepository
) : CustomDatasetRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeDatasetMeta(): Flow<CustomDatasetMeta?> =
        settingsRepository.getCustomDatasetMeta()

    override suspend fun importDataset(payload: DatasetImportPayload): Result<CustomDatasetMeta> = runCatching {
        val language = settingsRepository.getSelectedLanguage().first()
        val extension = payload.fileName.substringAfterLast('.', "").lowercase()
        val records = when (extension) {
            "csv" -> parseCsv(payload.rawContent)
            "json" -> parseJson(payload.rawContent)
            else -> throw IllegalArgumentException("Поддерживаются только CSV и JSON файлы.")
        }
        if (records.isEmpty()) {
            throw IllegalArgumentException("Файл не содержит валидных слов.")
        }

        val now = Clock.System.now()
        val entities = records.map { record ->
            VocabularyEntity(
                id = stableId(language, record.word, record.translation),
                targetLanguage = language,
                word = record.word,
                translation = record.translation,
                example = record.example,
                phonetic = record.phonetic,
                partOfSpeech = record.partOfSpeech,
                category = record.category.ifBlank { "general" },
                source = "custom",
                synonymsForeign = record.synonyms,
                lastAccessed = now
            )
        }

        localDataSource.replaceAll(entities)

        val meta = CustomDatasetMeta(
            displayName = payload.fileName,
            format = extension.uppercase(),
            recordsCount = entities.size,
            importedAtEpochMillis = now.toEpochMilliseconds()
        )
        settingsRepository.setCustomDatasetMeta(meta)
        settingsRepository.setVocabularySource(VocabularySource.CUSTOM)
        meta
    }.onFailure { error ->
        Timber.e(error, "[CustomDataset] Ошибка импорта ${payload.fileName}")
    }

    override suspend fun deleteDataset(): Result<Unit> = runCatching {
        localDataSource.clearAll()
        settingsRepository.setCustomDatasetMeta(null)
        settingsRepository.setVocabularySource(VocabularySource.REMOTE)
    }.onFailure { error ->
        Timber.e(error, "[CustomDataset] Ошибка удаления датасета")
    }

    private fun stableId(language: String, word: String, translation: String): String {
        val key = "$language|${word.lowercase()}|${translation.lowercase()}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun parseCsv(raw: String): List<ParsedRecord> {
        val lines = raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
        if (lines.isEmpty()) return emptyList()

        val header = lines.first().split(',').map { it.trim() }
        val rows = lines.drop(1)
        val indexOf = { name: String -> header.indexOfFirst { it.equals(name, ignoreCase = true) } }
        val wordIndex = indexOf("word")
        val translationIndex = indexOf("translation")
        if (wordIndex < 0 || translationIndex < 0) {
            throw IllegalArgumentException("CSV должен содержать колонки 'word' и 'translation'.")
        }

        return rows.mapNotNull { row ->
            val cols = row.split(',').map { it.trim() }
            val word = cols.getOrNull(wordIndex).orEmpty()
            val translation = cols.getOrNull(translationIndex).orEmpty()
            if (word.isBlank() || translation.isBlank()) return@mapNotNull null
            ParsedRecord(
                word = word,
                translation = translation,
                example = cols.valueByIndex(indexOf("example")),
                phonetic = cols.valueByIndex(indexOf("phonetic")),
                partOfSpeech = cols.valueByIndex(indexOf("partOfSpeech")),
                category = cols.valueByIndex(indexOf("category")) ?: "general",
                synonyms = cols.valueByIndex(indexOf("synonyms"))
            )
        }
    }

    private fun parseJson(raw: String): List<ParsedRecord> {
        val parsed = json.decodeFromString<List<JsonWordRecord>>(raw)
        return parsed.mapNotNull { item ->
            val word = item.word.trim()
            val translation = item.translation.trim()
            if (word.isBlank() || translation.isBlank()) return@mapNotNull null
            ParsedRecord(
                word = word,
                translation = translation,
                example = item.example?.trim().takeUnless { it.isNullOrBlank() },
                phonetic = item.phonetic?.trim().takeUnless { it.isNullOrBlank() },
                partOfSpeech = item.partOfSpeech?.trim().takeUnless { it.isNullOrBlank() },
                category = item.category?.trim().takeUnless { it.isNullOrBlank() } ?: "general",
                synonyms = item.synonyms?.trim().takeUnless { it.isNullOrBlank() }
            )
        }
    }

    private fun List<String>.valueByIndex(index: Int): String? {
        if (index < 0) return null
        return getOrNull(index)?.takeIf { it.isNotBlank() }
    }
}

private data class ParsedRecord(
    val word: String,
    val translation: String,
    val example: String?,
    val phonetic: String?,
    val partOfSpeech: String?,
    val category: String,
    val synonyms: String?
)

@Serializable
private data class JsonWordRecord(
    val word: String,
    val translation: String,
    val example: String? = null,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val category: String? = null,
    val synonyms: String? = null
)

