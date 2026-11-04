package ru.mindlex.practice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerEvaluatorTest {

    private final AnswerEvaluator evaluator = new AnswerEvaluator();

    @Test
    @DisplayName("Точное совпадение даёт высшее качество")
    void exactMatchGivesTopQuality() {
        AnswerEvaluator.Evaluation result = evaluator.evaluate("Journey", List.of("journey"));

        assertThat(result.quality()).isEqualTo(5);
        assertThat(result.correct()).isTrue();
    }

    @Test
    @DisplayName("Одна опечатка не считается ошибкой")
    void singleTypoIsAccepted() {
        AnswerEvaluator.Evaluation result = evaluator.evaluate("jorney", List.of("journey"));

        assertThat(result.quality()).isEqualTo(4);
        assertThat(result.correct()).isTrue();
    }

    @Test
    @DisplayName("Две опечатки — минимально приемлемый ответ")
    void twoTyposIsStillAccepted() {
        AnswerEvaluator.Evaluation result = evaluator.evaluate("jornei", List.of("journey"));

        assertThat(result.quality()).isEqualTo(3);
        assertThat(result.correct()).isTrue();
    }

    @Test
    @DisplayName("Сильное расхождение считается ошибкой")
    void farAnswerIsIncorrect() {
        AnswerEvaluator.Evaluation result = evaluator.evaluate("путешествие", List.of("journey"));

        assertThat(result.quality()).isLessThan(3);
        assertThat(result.correct()).isFalse();
    }

    @Test
    @DisplayName("Диакритика, регистр и пунктуация не влияют на проверку")
    void normalizationIgnoresCaseDiacriticsAndPunctuation() {
        AnswerEvaluator.Evaluation result = evaluator.evaluate("  RÉSUMÉ! ", List.of("resume"));

        assertThat(result.correct()).isTrue();
    }

    @Test
    @DisplayName("Варианты ответа разворачиваются из строки со списком")
    void expandsAlternativeVariants() {
        List<String> candidates = evaluator.expandCandidates(List.of("trip, voyage / journey"));

        assertThat(candidates).containsExactly("trip", "voyage", "journey");
    }

    @Test
    @DisplayName("Подходящий альтернативный перевод принимается")
    void acceptsAlternativeTranslation() {
        AnswerEvaluator.Evaluation result = evaluator.evaluate("courageous", List.of("brave", "courageous, bold"));

        assertThat(result.correct()).isTrue();
        assertThat(result.quality()).isEqualTo(5);
    }

    @Test
    @DisplayName("Пустой ответ всегда ошибка")
    void blankAnswerIsIncorrect() {
        AnswerEvaluator.Evaluation result = evaluator.evaluate("   ", List.of("journey"));

        assertThat(result.correct()).isFalse();
        assertThat(result.quality()).isEqualTo(1);
    }

    @ParameterizedTest(name = "расстояние {0} даёт качество {1}")
    @CsvSource({"0,5", "1,4", "2,3", "3,2", "4,2", "5,1", "40,1"})
    void qualityMapping(int distance, int expectedQuality) {
        assertThat(AnswerEvaluator.qualityForDistance(distance)).isEqualTo(expectedQuality);
    }

    @Test
    @DisplayName("Расстояние Левенштейна считается верно")
    void levenshteinDistance() {
        assertThat(evaluator.levenshtein("journey", "journey")).isZero();
        assertThat(evaluator.levenshtein("journey", "jorney")).isEqualTo(1);
        assertThat(evaluator.levenshtein("", "abc")).isEqualTo(3);
        assertThat(evaluator.levenshtein("abc", "")).isEqualTo(3);
        assertThat(evaluator.levenshtein("kitten", "sitting")).isEqualTo(3);
    }

    @Test
    @DisplayName("Для цепочек синонимов требуется точное совпадение")
    void chainRequiresExactMatch() {
        assertThat(evaluator.matchesExactly("clever", List.of("clever, bright"))).isTrue();
        assertThat(evaluator.matchesExactly("cleve", List.of("clever, bright"))).isFalse();
    }
}
