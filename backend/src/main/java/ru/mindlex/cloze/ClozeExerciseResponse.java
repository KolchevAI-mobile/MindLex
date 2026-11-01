package ru.mindlex.cloze;

import java.time.Instant;
import java.util.UUID;

/**
 * Представление cloze-упражнения в API.
 */
public record ClozeExerciseResponse(UUID id,
                                    UUID wordId,
                                    String sentence,
                                    String answer,
                                    String hint,
                                    Instant createdAt,
                                    Instant updatedAt) {

    public static ClozeExerciseResponse from(ClozeExercise exercise) {
        return new ClozeExerciseResponse(exercise.getId(), exercise.getWordId(), exercise.getSentence(),
                exercise.getAnswer(), exercise.getHint(), exercise.getCreatedAt(), exercise.getUpdatedAt());
    }
}
