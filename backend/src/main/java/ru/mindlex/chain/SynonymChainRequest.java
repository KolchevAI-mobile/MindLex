package ru.mindlex.chain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Запрос на создание или изменение цепочки синонимов вместе с шагами.
 * При обновлении шаги заменяются целиком.
 */
public record SynonymChainRequest(
        @NotBlank(message = "Укажите название цепочки")
        @Size(max = 200, message = "Название слишком длинное")
        String title,

        @Size(max = 8, message = "Код языка слишком длинный")
        String language,

        @Size(max = 80, message = "Категория слишком длинная")
        String category,

        @NotEmpty(message = "Цепочка должна содержать хотя бы один шаг")
        @Valid
        List<SynonymChainStepRequest> steps
) {
}
