package com.example.mindlex.data.repository

import android.content.Context
import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.data.local.entity.VocabularyEntity
import com.example.mindlex.data.local.repository.VocabularyLocalDataSource
import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.DatasetImportPayload
import com.example.mindlex.domain.model.ManualWordEntry
import com.example.mindlex.domain.model.VocabularySource
import com.example.mindlex.domain.repository.CustomDatasetRepository
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.WordProgressRepository
import java.security.MessageDigest
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CustomDatasetRepositoryImpl @Inject constructor(
    private val localDataSource: VocabularyLocalDataSource,
    private val settingsRepository: SettingsRepository,
    private val wordProgressRepository: WordProgressRepository,
    @ApplicationContext private val appContext: Context
) : CustomDatasetRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeCurrentDatasetMeta(): Flow<CustomDatasetMeta?> =
        settingsRepository.getCustomDatasetMeta()

    override fun observeDatasetHistory(): Flow<List<CustomDatasetMeta>> =
        settingsRepository.getCustomDatasetHistory()

    override suspend fun importDataset(payload: DatasetImportPayload): Result<CustomDatasetMeta> =
        runCatching { importPayload(payload) }

    override suspend fun importManualDataset(
        entries: List<ManualWordEntry>,
        displayName: String
    ): Result<CustomDatasetMeta> = runCatching {
        val records = entries.mapNotNull { entry ->
            val word = entry.word.trim()
            val translation = entry.translation.trim()
            if (word.isBlank() || translation.isBlank()) return@mapNotNull null
            ParsedRecord(
                word = word,
                translation = translation,
                example = entry.example?.trim().takeUnless { it.isNullOrBlank() },
                phonetic = entry.phonetic?.trim().takeUnless { it.isNullOrBlank() },
                partOfSpeech = null,
                category = "general",
                synonyms = null
            )
        }
        if (records.isEmpty()) {
            throw IllegalArgumentException("Добавьте хотя бы одну пару слово–перевод.")
        }
        val now = Clock.System.now()
        val metaId = stableId("manual", displayName, now.toEpochMilliseconds().toString())
        applyRecords(
            records = records,
            meta = CustomDatasetMeta(
                id = metaId,
                displayName = displayName.ifBlank { DEFAULT_MANUAL_NAME },
                format = "MANUAL",
                recordsCount = records.size,
                importedAtEpochMillis = now.toEpochMilliseconds(),
                sourceUri = "manual://$metaId"
            )
        )
    }

    override suspend fun refreshDataset(datasetId: String): Result<CustomDatasetMeta> = runCatching {
        val history = settingsRepository.getCustomDatasetHistory().first()
        val item = history.firstOrNull { it.id == datasetId }
            ?: throw IllegalArgumentException("Датасет не найден в истории.")
        if (item.format == "MANUAL") {
            throw IllegalArgumentException("Ручной датасет нельзя обновить из файла.")
        }
        importPayload(readPayloadFromStoredMeta(item), datasetId = item.id)
    }

    override suspend fun deleteDataset(datasetId: String): Result<Unit> = runCatching {
        val history = settingsRepository.getCustomDatasetHistory().first()
        val updated = history.filterNot { it.id == datasetId }
        settingsRepository.setCustomDatasetHistory(updated)

        val current = settingsRepository.getCustomDatasetMeta().first()
        if (current?.id == datasetId) {
            resetToRemoteMode()
        }
    }

    private suspend fun importPayload(
        payload: DatasetImportPayload,
        datasetId: String? = null
    ): CustomDatasetMeta {
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
        val metaId = datasetId ?: stableId(payload.sourceUri, payload.fileName, now.toEpochMilliseconds().toString())
        return applyRecords(
            records = records,
            meta = CustomDatasetMeta(
                id = metaId,
                displayName = payload.fileName,
                format = extension.uppercase(),
                recordsCount = records.size,
                importedAtEpochMillis = now.toEpochMilliseconds(),
                sourceUri = payload.sourceUri
            )
        )
    }

    /** Перед новым датасетом чистим словарь и весь прогресс, потом включаем офлайн-режим. */
    private suspend fun applyRecords(
        records: List<ParsedRecord>,
        meta: CustomDatasetMeta
    ): CustomDatasetMeta {
        val categoryBefore = settingsRepository.getSelectedCategory().first()
        if (categoryBefore != LearningDefaults.CUSTOM_DATASET_CATEGORY) {
            settingsRepository.setLastRemoteCategory(categoryBefore)
        }
        val language = settingsRepository.getSelectedLanguage().first()
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
                source = SOURCE_CUSTOM,
                synonymsForeign = record.synonyms,
                lastAccessed = now
            )
        }

        wordProgressRepository.clearAll()
        localDataSource.replaceAll(entities)

        val readyMeta = meta.copy(recordsCount = entities.size)
        upsertHistory(readyMeta)
        settingsRepository.setCustomDatasetMeta(readyMeta)
        settingsRepository.setVocabularySource(VocabularySource.CUSTOM)
        settingsRepository.setSelectedCategory(LearningDefaults.CUSTOM_DATASET_CATEGORY)
        return readyMeta
    }

    private suspend fun resetToRemoteMode() {
        wordProgressRepository.clearAll()
        localDataSource.clearAll()
        settingsRepository.setCustomDatasetMeta(null)
        settingsRepository.setVocabularySource(VocabularySource.REMOTE)
        val restoreCategory = settingsRepository.getLastRemoteCategory().first()
        settingsRepository.setSelectedCategory(restoreCategory)
    }

    private suspend fun upsertHistory(meta: CustomDatasetMeta) {
        val history = settingsRepository.getCustomDatasetHistory().first()
        settingsRepository.setCustomDatasetHistory(
            buildList {
                add(meta)
                addAll(history.filterNot { it.id == meta.id })
            }
        )
    }

    private fun readPayloadFromStoredMeta(meta: CustomDatasetMeta): DatasetImportPayload =
        DatasetImportPayload(
            fileName = meta.displayName,
            rawContent = readTextFromUri(meta.sourceUri),
            sourceUri = meta.sourceUri
        )

    private fun readTextFromUri(uri: String): String {
        val input = android.net.Uri.parse(uri)
        val stream = appContext.contentResolver.openInputStream(input)
            ?: throw IllegalArgumentException("Не удалось открыть файл датасета.")
        return stream.bufferedReader().use { it.readText() }
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

    private companion object {
        const val SOURCE_CUSTOM = "custom"
        const val DEFAULT_MANUAL_NAME = "Мой словарь"
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
