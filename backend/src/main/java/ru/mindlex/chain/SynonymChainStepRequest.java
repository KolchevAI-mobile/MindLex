package ru.mindlex.chain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Запрос на создание или изменение одного шага цепочки синонимов.
 */
public record SynonymChainStepRequest(
        @Min(value = 1, message = "Номер шага должен быть не меньше 1")
        int stepNumber,

        @NotBlank(message = "Укажите слово-подсказку")
        @Size(max = 160, message = "Слово-подсказка слишком длинное")
        String promptWord,

        @NotEmpty(message = "Укажите хотя бы один синоним")
        List<String> acceptedSynonyms,

        @Size(max = 255, message = "Подсказка слишком длинная")
        String hint
) {
}
