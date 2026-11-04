package ru.mindlex.domain;

/**
 * Статус изучения слова. Вычисляется из уровня интервального повторения.
 */
public enum WordStatus {

    /** Уровень 0 — слово ещё не закреплено. */
    NEW,

    /** Уровни 1-2 — слово в процессе изучения. */
    LEARNING,

    /** Уровни 3-4 — слово на этапе повторения. */
    REVIEW,

    /** Уровни 5-6 — слово считается выученным. */
    KNOWN;

    public static WordStatus fromLevel(int level) {
        if (level <= 0) {
            return NEW;
        }
        if (level <= 2) {
            return LEARNING;
        }
        if (level >= 5) {
            return KNOWN;
        }
        return REVIEW;
    }
}
