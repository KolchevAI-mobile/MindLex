package ru.mindlex.reminder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Запрос на создание или изменение напоминания о занятии.
 * Поле {@code sent} необязательное: при создании без него напоминание считается неотправленным,
 * а при обновлении прежнее значение сохраняется.
 */
public record StudyReminderRequest(
        @NotNull(message = "Укажите время напоминания")
        Instant plannedAt,

        @NotBlank(message = "Укажите заголовок напоминания")
        @Size(max = 160, message = "Заголовок слишком длинный")
        String title,

        @Size(max = 400, message = "Текст напоминания слишком длинный")
        String body,

        Boolean sent
) {
}
