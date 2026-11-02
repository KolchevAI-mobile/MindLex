package ru.mindlex.practice;

import org.springframework.stereotype.Component;
import ru.mindlex.domain.WordStatus;

import java.time.Duration;
import java.time.Instant;

/**
 * Интервальное повторение по мотивам SM-2.
 *
 * <p>Правила полностью соответствуют исходному приложению:</p>
 * <ul>
 *   <li>коэффициент лёгкости EF меняется на {@code 0.1 - (5-q)(0.08 + 0.02(5-q))} и ограничен 1.3..2.5;</li>
 *   <li>уровень растёт на 2 при q=5, на 1 при q&gt;=3 и падает до нуля при q&lt;=2;</li>
 *   <li>интервал не меньше 30/7/3/1 суток в зависимости от качества;</li>
 *   <li>статус слова выводится из уровня.</li>
 * </ul>
 */
@Component
public class SrsCalculator {

    public static final double DEFAULT_EASE_FACTOR = 2.5;
    public static final double MIN_EASE_FACTOR = 1.3;
    public static final double MAX_EASE_FACTOR = 2.5;
    public static final int MAX_LEVEL = 6;

    public SrsOutcome calculate(int currentLevel,
                                int previousIntervalDays,
                                double currentEaseFactor,
                                int quality,
                                Instant now) {
        int normalizedQuality = Math.max(1, Math.min(5, quality));
        double easeFactor = calculateEaseFactor(currentEaseFactor, normalizedQuality);

        int newLevel = switch (normalizedQuality) {
            case 5 -> Math.min(currentLevel + 2, MAX_LEVEL);
            case 4, 3 -> Math.min(currentLevel + 1, MAX_LEVEL);
            default -> 0;
        };

        int intervalDays = switch (normalizedQuality) {
            case 5 -> newLevel >= 4
                    ? (previousIntervalDays <= 0 ? 30 : Math.max((int) (previousIntervalDays * easeFactor), 30))
                    : (previousIntervalDays <= 0 ? 7 : Math.max((int) (previousIntervalDays * easeFactor), 7));
            case 4 -> previousIntervalDays <= 0 ? 3 : Math.max((int) (previousIntervalDays * easeFactor), 3);
            case 3 -> previousIntervalDays <= 0 ? 1 : Math.max((int) (previousIntervalDays * easeFactor), 1);
            default -> 0;
        };

        Instant nextReviewAt = now.plus(Duration.ofDays(intervalDays));
        return new SrsOutcome(newLevel, easeFactor, intervalDays, nextReviewAt, WordStatus.fromLevel(newLevel));
    }

    public double calculateEaseFactor(double current, int quality) {
        double updated = current + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        return Math.max(MIN_EASE_FACTOR, Math.min(MAX_EASE_FACTOR, updated));
    }

    /**
     * @param level         новый уровень 0..6
     * @param easeFactor   новый коэффициент лёгкости
     * @param intervalDays интервал до следующего повторения
     * @param nextReviewAt момент следующего повторения
     * @param status       статус слова
     */
    public record SrsOutcome(int level,
                             double easeFactor,
                             int intervalDays,
                             Instant nextReviewAt,
                             WordStatus status) {
    }
}
