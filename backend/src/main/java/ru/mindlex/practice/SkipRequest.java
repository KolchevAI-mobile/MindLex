package ru.mindlex.practice;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SkipRequest(@NotNull(message = "Укажите идентификатор вопроса") UUID questionId) {
}
