package ru.mindlex.reminder;

import java.time.Instant;
import java.util.UUID;

/**
 * Представление напоминания о занятии в API.
 */
public record StudyReminderResponse(UUID id,
                                    UUID userId,
                                    Instant plannedAt,
                                    String title,
                                    String body,
                                    boolean sent,
                                    Instant createdAt,
                                    Instant updatedAt) {

    public static StudyReminderResponse from(StudyReminder reminder) {
        return new StudyReminderResponse(reminder.getId(), reminder.getUserId(), reminder.getPlannedAt(),
                reminder.getTitle(), reminder.getBody(), reminder.isSent(),
                reminder.getCreatedAt(), reminder.getUpdatedAt());
    }
}
