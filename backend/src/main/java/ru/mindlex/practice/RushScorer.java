package ru.mindlex.practice;

import org.springframework.stereotype.Component;

/**
 * Начисление очков в механике Timed Rush.
 *
 * <p>Комбо-множитель зависит от длины серии правильных ответов,
 * бонус 1.2 начисляется, пока в запасе больше 30 секунд.</p>
 */
@Component
public class RushScorer {

    public static final int BASE_POINTS = 10;
    /** Порог, выше которого действует бонус за оставшееся время. */
    public static final int TIME_BONUS_THRESHOLD_SECONDS = 30;
    public static final double TIME_BONUS_MULTIPLIER = 1.2;

    public int score(int comboAfterCorrect, int timeRemainingSeconds) {
        double total = BASE_POINTS * comboMultiplier(comboAfterCorrect);
        if (timeRemainingSeconds > TIME_BONUS_THRESHOLD_SECONDS) {
            total *= TIME_BONUS_MULTIPLIER;
        }
        return Math.max(1, (int) Math.round(total));
    }

    public double comboMultiplier(int comboAfterCorrect) {
        if (comboAfterCorrect >= 20) {
            return 3.0;
        }
        if (comboAfterCorrect >= 10) {
            return 2.0;
        }
        if (comboAfterCorrect >= 5) {
            return 1.5;
        }
        return 1.0;
    }

    /** Слова в минуту по числу просмотренных слов и длительности сессии. */
    public int wordsPerMinute(int wordsSeen, int totalSeconds) {
        if (totalSeconds <= 0) {
            return 0;
        }
        return (int) Math.round(wordsSeen / (totalSeconds / 60.0));
    }
}
