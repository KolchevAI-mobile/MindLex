package ru.mindlex.domain;

/**
 * Четыре игровые механики приложения.
 */
public enum Mechanic {

    /** Active Recall — перевод слова с родного языка на изучаемый. */
    ACTIVE_RECALL,

    /** Contextual Cloze — заполнение пропуска в предложении. */
    CLOZE,

    /** Timed Rush — перевод на скорость с комбо-множителем. */
    RUSH,

    /** Synonym Chain — построение цепочки синонимов. */
    SYNONYM_CHAIN
}
