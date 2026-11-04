package ru.mindlex.settings;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Настройки пользователя. Поле {@code visibleStreak} — серия, которую нужно показывать:
 * она обнуляется, если последнее занятие было раньше вчерашнего дня.
 */
public record SettingsResponse(UUID userId,
                               int dailyGoal,
                               String language,
                               UUID selectedDeckId,
                               LocalTime preferredTime,
                               int rushSeconds,
                               int clozeSeconds,
                               boolean notificationsEnabled,
                               int currentStreak,
                               int visibleStreak,
                               LocalDate lastStudyDate,
                               int bestRushScore,
                               int maxComboRecord,
                               String timezone,
                               Instant updatedAt) {

    public static SettingsResponse from(UserSettings settings, int visibleStreak) {
        return new SettingsResponse(settings.getUserId(), settings.getDailyGoal(), settings.getLanguage(),
                settings.getSelectedDeckId(), settings.getPreferredTime(), settings.getRushSeconds(),
                settings.getClozeSeconds(), settings.isNotificationsEnabled(), settings.getCurrentStreak(),
                visibleStreak, settings.getLastStudyDate(), settings.getBestRushScore(),
                settings.getMaxComboRecord(), settings.getTimezone(), settings.getUpdatedAt());
    }
}
