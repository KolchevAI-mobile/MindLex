package ru.mindlex.practice;

import ru.mindlex.progress.WordProgressResponse;

import java.util.UUID;

/**
 * Результат проверки ответа.
 *
 * @param quality      качество ответа 1..5
 * @param scoreDelta   начисленные очки
 * @param progress     новое состояние прогресса слова, если оно применимо
 */
public record AnswerResponse(UUID sessionId,
                             UUID questionId,
                             boolean correct,
                             int quality,
                             String expectedAnswer,
                             int scoreDelta,
                             int score,
                             int comboStreak,
                             int maxCombo,
                             int correctCount,
                             int incorrectCount,
                             int skipCount,
                             Integer timeRemainingSeconds,
                             boolean sessionFinished,
                             WordProgressResponse progress) {
}
