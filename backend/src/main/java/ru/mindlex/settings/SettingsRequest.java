package ru.mindlex.settings;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.UUID;

public record SettingsRequest(
        @Min(value = 5, message = "Цель должна быть не меньше 5 слов")
        @Max(value = 200, message = "Цель должна быть не больше 200 слов")
        Integer dailyGoal,

        @Size(max = 8, message = "Код языка слишком длинный")
        String language,

        UUID selectedDeckId,

        LocalTime preferredTime,

        @Min(value = 60, message = "Раунд не может быть короче 60 секунд")
        @Max(value = 90, message = "Раунд не может быть длиннее 90 секунд")
        Integer rushSeconds,

        @Min(value = 30, message = "Таймер упражнения не может быть короче 30 секунд")
        @Max(value = 45, message = "Таймер упражнения не может быть длиннее 45 секунд")
        Integer clozeSeconds,

        Boolean notificationsEnabled,

        @Size(max = 64, message = "Идентификатор часового пояса слишком длинный")
        String timezone
) {
}
