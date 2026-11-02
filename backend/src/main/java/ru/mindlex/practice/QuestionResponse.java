package ru.mindlex.practice;

import ru.mindlex.domain.Mechanic;

import java.util.List;
import java.util.UUID;

/**
 * Очередной вопрос сессии.
 *
 * @param questionId       идентификатор вопроса, который нужно вернуть при ответе
 * @param promptKind       тип вопроса: WORD, SENTENCE или CHAIN_STEP
 * @param prompt           основной текст вопроса
 * @param hint             подсказка (пример употребления, перевод или часть синонимов)
 * @param timeLimitSeconds ограничение времени на вопрос; {@code null} — нет
 */
public record QuestionResponse(UUID sessionId,
                               UUID questionId,
                               Mechanic mechanic,
                               int index,
                               int totalQuestions,
                               String promptKind,
                               String prompt,
                               String hint,
                               String phonetic,
                               String partOfSpeech,
                               String example,
                               Integer chainStepNumber,
                               Integer chainLength,
                               Integer timeLimitSeconds,
                               List<String> hintOptions) {

    public static final String KIND_WORD = "WORD";
    public static final String KIND_SENTENCE = "SENTENCE";
    public static final String KIND_CHAIN_STEP = "CHAIN_STEP";
}
