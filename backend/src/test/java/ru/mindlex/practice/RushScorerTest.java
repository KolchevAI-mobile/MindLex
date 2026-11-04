package ru.mindlex.practice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class RushScorerTest {

    private final RushScorer scorer = new RushScorer();

    @ParameterizedTest(name = "комбо {0} даёт множитель {1}")
    @CsvSource({"0,1.0", "1,1.0", "4,1.0", "5,1.5", "9,1.5", "10,2.0", "19,2.0", "20,3.0", "50,3.0"})
    void comboMultiplier(int combo, double expected) {
        assertThat(scorer.comboMultiplier(combo)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Базовые очки без комбо и без бонуса времени")
    void baseScore() {
        assertThat(scorer.score(1, 10)).isEqualTo(10);
    }

    @Test
    @DisplayName("Бонус 1.2 начисляется, пока осталось больше 30 секунд")
    void timeBonus() {
        assertThat(scorer.score(1, 31)).isEqualTo(12);
        assertThat(scorer.score(1, 30)).isEqualTo(10);
    }

    @Test
    @DisplayName("Комбо и бонус времени перемножаются")
    void comboWithTimeBonus() {
        assertThat(scorer.score(10, 60)).isEqualTo(24);
        assertThat(scorer.score(20, 60)).isEqualTo(36);
    }

    @Test
    @DisplayName("Очки всегда не меньше единицы")
    void scoreIsAtLeastOne() {
        assertThat(scorer.score(0, 0)).isEqualTo(10);
    }

    @Test
    @DisplayName("Слова в минуту считаются по длительности раунда")
    void wordsPerMinute() {
        assertThat(scorer.wordsPerMinute(30, 90)).isEqualTo(20);
        assertThat(scorer.wordsPerMinute(10, 0)).isZero();
    }
}
