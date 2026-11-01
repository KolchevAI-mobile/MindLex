package ru.mindlex.cloze;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание или изменение cloze-упражнения.
 * Предложение обязано содержать пропуск {@code ___} — это дополнительно проверяет сервис.
 */
public record ClozeExerciseRequest(
        @NotBlank(message = "Укажите предложение с пропуском")
        @Size(max = 600, message = "Предложение слишком длинное")
        String sentence,

        @NotBlank(message = "Укажите правильный ответ")
        @Size(max = 160, message = "Ответ слишком длинный")
        String answer,

        @Size(max = 255, message = "Подсказка слишком длинная")
        String hint
) {
}
