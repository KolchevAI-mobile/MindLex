package com.example.mindlex.data.local.mapper

import com.example.mindlex.domain.model.Vocabulary
import com.example.mindlex.domain.model.Word

/**
 * Маппер для конвертации Vocabulary (словарь) в Word (модель для обучения).
 */
object VocabularyToWordMapper {

    /**
     * Конвертирует Vocabulary в Word.
     */
    fun toWord(vocabulary: Vocabulary): Word {
        return Word(
            id = vocabulary.id,
            wordForeign = vocabulary.word,
            wordNative = vocabulary.translation,
            alternativeTranslations = vocabulary.synonymsForeign,
            targetLanguage = vocabulary.targetLanguage,
            example = vocabulary.example,
            phonetic = vocabulary.phonetic,
            partOfSpeech = vocabulary.partOfSpeech,
            category = vocabulary.category
        )
    }
}
