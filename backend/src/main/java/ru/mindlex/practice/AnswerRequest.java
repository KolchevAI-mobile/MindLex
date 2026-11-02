package ru.mindlex.practice;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Ответ пользователя на вопрос сессии.
 *
 * @param questionId идентификатор вопроса из {@link QuestionResponse}
 * @param answer     введённый текст
 * @param hintUsed   была ли использована подсказка
 * @param timedOut   истекло ли время на вопрос (для механики cloze)
 * @param elapsedMs  сколько миллисекунд занял ответ
 */
public record AnswerRequest(
        @NotNull(message = "Укажите идентификатор вопроса") UUID questionId,
        String answer,
        Boolean hintUsed,
        Boolean timedOut,
        Long elapsedMs
) {
    public boolean isHintUsed() {
        return Boolean.TRUE.equals(hintUsed);
    }

    public boolean isTimedOut() {
        return Boolean.TRUE.equals(timedOut);
    }

    public long elapsedMillis() {
        return elapsedMs == null ? 0L : elapsedMs;
    }
}
