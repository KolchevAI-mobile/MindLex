package ru.mindlex.reminder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ru.mindlex.common.TimestampedEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Напоминание о занятии. Полноценная CRUD-сущность: клиент создаёт, переносит и удаляет напоминания.
 */
@Entity
@Table(name = "study_reminders")
public class StudyReminder extends TimestampedEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "planned_at", nullable = false)
    private Instant plannedAt;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "body", length = 400)
    private String body;

    @Column(name = "sent", nullable = false)
    private boolean sent;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Instant getPlannedAt() {
        return plannedAt;
    }

    public void setPlannedAt(Instant plannedAt) {
        this.plannedAt = plannedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
