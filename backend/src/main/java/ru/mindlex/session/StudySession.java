package ru.mindlex.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import ru.mindlex.common.TimestampedEntity;
import ru.mindlex.domain.Mechanic;
import ru.mindlex.domain.SessionStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Игровая сессия одной из механик. Сервер хранит её состояние, поэтому клиент
 * остаётся «тонким», а логика начисления очков не может быть подделана на устройстве.
 */
@Entity
@Table(name = "study_sessions")
public class StudySession extends TimestampedEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "deck_id")
    private UUID deckId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mechanic", nullable = false, length = 24)
    private Mechanic mechanic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SessionStatus status = SessionStatus.ACTIVE;

    /** Общая длительность сессии в секундах; 0 — без ограничения по времени. */
    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    /** Сколько вопросов нужно пройти; 0 — без ограничения по количеству. */
    @Column(name = "question_limit", nullable = false)
    private int questionLimit;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "combo_streak", nullable = false)
    private int comboStreak;

    @Column(name = "max_combo", nullable = false)
    private int maxCombo;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "incorrect_count", nullable = false)
    private int incorrectCount;

    @Column(name = "skip_count", nullable = false)
    private int skipCount;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getDeckId() {
        return deckId;
    }

    public void setDeckId(UUID deckId) {
        this.deckId = deckId;
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getQuestionLimit() {
        return questionLimit;
    }

    public void setQuestionLimit(int questionLimit) {
        this.questionLimit = questionLimit;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getComboStreak() {
        return comboStreak;
    }

    public void setComboStreak(int comboStreak) {
        this.comboStreak = comboStreak;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public void setMaxCombo(int maxCombo) {
        this.maxCombo = maxCombo;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public void setIncorrectCount(int incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(int skipCount) {
        this.skipCount = skipCount;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
}
