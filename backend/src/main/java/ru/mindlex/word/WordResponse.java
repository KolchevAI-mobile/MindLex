package ru.mindlex.word;

import ru.mindlex.common.TextUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WordResponse(UUID id,
                           UUID deckId,
                           String term,
                           String translation,
                           String transcription,
                           String partOfSpeech,
                           String example,
                           List<String> alternatives,
                           Instant createdAt,
                           Instant updatedAt) {

    public static WordResponse from(Word word) {
        return new WordResponse(word.getId(), word.getDeckId(), word.getTerm(), word.getTranslation(),
                word.getTranscription(), word.getPartOfSpeech(), word.getExample(),
                TextUtils.splitCsv(word.getAlternatives()), word.getCreatedAt(), word.getUpdatedAt());
    }
}
