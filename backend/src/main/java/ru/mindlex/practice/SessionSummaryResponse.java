package ru.mindlex.practice;

import ru.mindlex.domain.Mechanic;

import java.time.Instant;
import java.util.UUID;

/**
 * Итоги завершённой сессии.
 *
 * @param newRecord был ли побит личный рекорд по очкам
 */
public record SessionSummaryResponse(UUID sessionId,
                                     Mechanic mechanic,
                                     int score,
                                     int correctCount,
                                     int incorrectCount,
                                     int skipCount,
                                     int answeredCount,
                                     int maxCombo,
                                     int accuracyPercent,
                                     int wordsPerMinute,
                                     int durationSeconds,
                                     int bestScore,
                                     int comboRecord,
                                     boolean newRecord,
                                     Instant startedAt,
                                     Instant finishedAt) {
}
