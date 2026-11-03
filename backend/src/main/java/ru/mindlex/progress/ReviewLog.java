package ru.mindlex.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import ru.mindlex.common.UuidEntity;
import ru.mindlex.domain.Mechanic;

import java.time.Instant;
import java.util.UUID;

/**
 * Журнал одного ответа пользователя. Используется для аналитики и как отдельная CRUD-сущность.
 */
@Entity
@Table(name = "review_logs")
public class ReviewLog extends UuidEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "word_id")
    private UUID wordId;

    /** Идентификатор заданного вопроса: слово, упражнение cloze или шаг цепочки. */
    @Column(name = "question_ref")
    private UUID questionRef;

    @Column(name = "session_id")
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mechanic", nullable = false, length = 24)
    private Mechanic mechanic;

    /** Качество ответа по шкале 1..5, где 5 — точное совпадение. */
    @Column(name = "quality", nullable = false)
    private int quality;

    @Column(name = "answer")
    private String answer;

    @Column(name = "expected_answer", length = 255)
    private String expectedAnswer;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "response_time_ms", nullable = false)
    private long responseTimeMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getWordId() {
        return wordId;
    }

    public void setWordId(UUID wordId) {
        this.wordId = wordId;
    }

    public UUID getQuestionRef() {
        return questionRef;
    }

    public void setQuestionRef(UUID questionRef) {
        this.questionRef = questionRef;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
