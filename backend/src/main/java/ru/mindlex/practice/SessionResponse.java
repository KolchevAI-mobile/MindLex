package ru.mindlex.practice;

import ru.mindlex.domain.Mechanic;
import ru.mindlex.domain.SessionStatus;
import ru.mindlex.session.StudySession;

import java.time.Instant;
import java.util.UUID;

/**
 * Состояние игровой сессии.
 *
 * @param questionLimit        сколько вопросов нужно пройти; 0 — без ограничения
 * @param timeRemainingSeconds сколько секунд осталось; {@code null} — таймера нет
 */
public record SessionResponse(UUID id,
                              Mechanic mechanic,
                              SessionStatus status,
                              UUID deckId,
                              int durationSeconds,
                              int questionLimit,
                              int answeredCount,
                              int correctCount,
                              int incorrectCount,
                              int skipCount,
                              int score,
                              int comboStreak,
                              int maxCombo,
                              Integer timeRemainingSeconds,
                              Instant startedAt,
                              Instant finishedAt) {

    public static SessionResponse from(StudySession session, Integer timeRemainingSeconds) {
        int answered = session.getCorrectCount() + session.getIncorrectCount() + session.getSkipCount();
        return new SessionResponse(session.getId(), session.getMechanic(), session.getStatus(), session.getDeckId(),
                session.getDurationSeconds(), session.getQuestionLimit(), answered, session.getCorrectCount(),
                session.getIncorrectCount(), session.getSkipCount(), session.getScore(), session.getComboStreak(),
                session.getMaxCombo(), timeRemainingSeconds, session.getStartedAt(), session.getFinishedAt());
    }
}
