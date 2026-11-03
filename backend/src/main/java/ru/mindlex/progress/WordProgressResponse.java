package ru.mindlex.progress;

import ru.mindlex.domain.WordStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Состояние изучения слова для клиента. Текст слова подставляется, если он ещё существует.
 */
public record WordProgressResponse(UUID id,
                                   UUID wordId,
                                   String term,
                                   String translation,
                                   WordStatus status,
                                   int level,
                                   double easeFactor,
                                   int intervalDays,
                                   Instant nextReviewAt,
                                   Instant lastReviewedAt,
                                   int correctCount,
                                   int incorrectCount,
                                   int accuracyPercent) {

    public static WordProgressResponse from(WordProgress progress, String term, String translation) {
        int total = progress.getCorrectCount() + progress.getIncorrectCount();
        int accuracy = total == 0 ? 0 : (int) Math.round(progress.getCorrectCount() * 100.0 / total);
        return new WordProgressResponse(progress.getId(), progress.getWordId(), term, translation,
                progress.getStatus(), progress.getLevel(), progress.getEaseFactor(), progress.getIntervalDays(),
                progress.getNextReviewAt(), progress.getLastReviewedAt(), progress.getCorrectCount(),
                progress.getIncorrectCount(), accuracy);
    }
}
