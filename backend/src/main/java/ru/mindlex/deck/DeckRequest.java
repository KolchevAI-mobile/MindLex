package ru.mindlex.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeckRequest(
        @NotBlank(message = "Укажите название колоды")
        @Size(max = 160, message = "Название слишком длинное")
        String name,

        @Size(max = 8, message = "Код языка слишком длинный")
        String language,

        @Size(max = 500, message = "Описание слишком длинное")
        String description,

        Boolean isPublic
) {
}
