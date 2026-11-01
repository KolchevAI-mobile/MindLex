package ru.mindlex.word;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WordRequest(
        @NotBlank(message = "Укажите слово")
        @Size(max = 160, message = "Слово слишком длинное")
        String term,

        @NotBlank(message = "Укажите перевод")
        @Size(max = 255, message = "Перевод слишком длинный")
        String translation,

        @Size(max = 160, message = "Транскрипция слишком длинная")
        String transcription,

        @Size(max = 32, message = "Часть речи слишком длинная")
        String partOfSpeech,

        @Size(max = 500, message = "Пример слишком длинный")
        String example,

        List<String> alternatives
) {
}
