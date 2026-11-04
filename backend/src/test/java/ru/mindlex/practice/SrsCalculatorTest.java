package ru.mindlex.practice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mindlex.domain.WordStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SrsCalculatorTest {

    private final SrsCalculator calculator = new SrsCalculator();
    private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    @DisplayName("Отличный ответ повышает уровень на два и даёт интервал от 30 дней")
    void excellentAnswerRaisesLevelByTwo() {
        SrsCalculator.SrsOutcome outcome = calculator.calculate(4, 20, 2.5, 5, now);

        assertThat(outcome.level()).isEqualTo(6);
        assertThat(outcome.intervalDays()).isGreaterThanOrEqualTo(30);
        assertThat(outcome.status()).isEqualTo(WordStatus.KNOWN);
    }

    @Test
    @DisplayName("Первый отличный ответ на новом слове даёт неделю")
    void firstExcellentAnswerGivesWeek() {
        SrsCalculator.SrsOutcome outcome = calculator.calculate(0, 0, 2.5, 5, now);

        assertThat(outcome.level()).isEqualTo(2);
        assertThat(outcome.intervalDays()).isEqualTo(7);
        assertThat(outcome.status()).isEqualTo(WordStatus.LEARNING);
    }

    @Test
    @DisplayName("Хороший ответ даёт минимум три дня")
    void goodAnswerGivesThreeDays() {
        SrsCalculator.SrsOutcome outcome = calculator.calculate(1, 0, 2.5, 4, now);

        assertThat(outcome.level()).isEqualTo(2);
        assertThat(outcome.intervalDays()).isEqualTo(3);
    }

    @Test
    @DisplayName("Приемлемый ответ даёт один день")
    void acceptableAnswerGivesOneDay() {
        SrsCalculator.SrsOutcome outcome = calculator.calculate(1, 0, 2.5, 3, now);

        assertThat(outcome.level()).isEqualTo(2);
        assertThat(outcome.intervalDays()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ошибка обнуляет уровень и интервал")
    void failureResetsLevel() {
        SrsCalculator.SrsOutcome outcome = calculator.calculate(5, 30, 2.5, 2, now);

        assertThat(outcome.level()).isZero();
        assertThat(outcome.intervalDays()).isZero();
        assertThat(outcome.status()).isEqualTo(WordStatus.NEW);
    }

    @Test
    @DisplayName("Коэффициент лёгкости ограничен диапазоном 1.3..2.5")
    void easeFactorIsClamped() {
        double afterExcellent = calculator.calculateEaseFactor(2.5, 5);
        assertThat(afterExcellent).isCloseTo(2.5, within(0.0001));

        double afterFailure = calculator.calculateEaseFactor(1.4, 1);
        assertThat(afterFailure).isEqualTo(1.3);
    }

    @Test
    @DisplayName("Интервал умножается на коэффициент лёгкости")
    void intervalGrowsWithEaseFactor() {
        SrsCalculator.SrsOutcome outcome = calculator.calculate(4, 10, 2.5, 4, now);

        assertThat(outcome.level()).isEqualTo(5);
        assertThat(outcome.intervalDays()).isEqualTo(25);
    }

    @Test
    @DisplayName("Уровень не превышает шести")
    void levelIsCapped() {
        SrsCalculator.SrsOutcome outcome = calculator.calculate(6, 100, 2.5, 5, now);

        assertThat(outcome.level()).isEqualTo(6);
        assertThat(outcome.status()).isEqualTo(WordStatus.KNOWN);
    }

    @Test
    @DisplayName("Статус выводится из уровня")
    void statusFollowsLevel() {
        assertThat(WordStatus.fromLevel(0)).isEqualTo(WordStatus.NEW);
        assertThat(WordStatus.fromLevel(1)).isEqualTo(WordStatus.LEARNING);
        assertThat(WordStatus.fromLevel(2)).isEqualTo(WordStatus.LEARNING);
        assertThat(WordStatus.fromLevel(3)).isEqualTo(WordStatus.REVIEW);
        assertThat(WordStatus.fromLevel(4)).isEqualTo(WordStatus.REVIEW);
        assertThat(WordStatus.fromLevel(5)).isEqualTo(WordStatus.KNOWN);
        assertThat(WordStatus.fromLevel(6)).isEqualTo(WordStatus.KNOWN);
    }
}
