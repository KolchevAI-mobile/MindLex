package ru.mindlex.practice;

import jakarta.validation.constraints.NotNull;
import ru.mindlex.domain.Mechanic;

import java.util.UUID;

/**
 * Запуск игровой сессии. Колода необязательна: если не указана, берётся выбранная в настройках.
 */
public record StartSessionRequest(
        @NotNull(message = "Укажите механику") Mechanic mechanic,
        UUID deckId
) {
}
