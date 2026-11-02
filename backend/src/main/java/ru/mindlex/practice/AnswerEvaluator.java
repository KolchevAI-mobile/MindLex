package ru.mindlex.practice;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Оценка ответа пользователя.
 *
 * <p>Алгоритм повторяет механику исходного приложения: ответ нормализуется
 * (убираются диакритические знаки, регистр и пунктуация), затем сравнивается
 * с каждым допустимым вариантом по расстоянию Левенштейна. Итоговое качество
 * переводится в пятибалльную шкалу SM-2.</p>
 */
@Component
public class AnswerEvaluator {

    private static final String DIACRITICS = "\\p{M}+";
    private static final String NON_WORD = "[^\\p{L}\\p{N}\\s-]";
    private static final String MULTIPLE_SPACES = "\\s+";

    /** Качество, начиная с которого ответ считается правильным. */
    public static final int ACCEPTANCE_QUALITY = 3;

    /** Качество, которое начисляется при использовании подсказки. */
    public static final int HINT_QUALITY = 3;

    /**
     * @param userInput      то, что ввёл пользователь
     * @param acceptedInputs допустимые варианты ответа (могут содержать списки через запятую)
     * @return результат оценки
     */
    public Evaluation evaluate(String userInput, List<String> acceptedInputs) {
        String normalizedInput = normalize(userInput);
        List<String> candidates = expandCandidates(acceptedInputs);

        if (normalizedInput.isBlank() || candidates.isEmpty()) {
            return new Evaluation(1, false, candidates.isEmpty() ? "" : candidates.get(0));
        }

        String best = candidates.get(0);
        int bestDistance = Integer.MAX_VALUE;
        for (String candidate : candidates) {
            int distance = levenshtein(normalizedInput, candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
            if (distance == 0) {
                break;
            }
        }

        int quality = qualityForDistance(bestDistance);
        return new Evaluation(quality, quality >= ACCEPTANCE_QUALITY, best);
    }

    /** Строгое сравнение без опечаток — используется в механике цепочек синонимов. */
    public boolean matchesExactly(String userInput, List<String> acceptedInputs) {
        String normalizedInput = normalize(userInput);
        if (normalizedInput.isBlank()) {
            return false;
        }
        return expandCandidates(acceptedInputs).contains(normalizedInput);
    }

    public static int qualityForDistance(int distance) {
        if (distance == 0) {
            return 5;
        }
        if (distance <= 1) {
            return 4;
        }
        if (distance <= 2) {
            return 3;
        }
        if (distance <= 4) {
            return 2;
        }
        return 1;
    }

    /** Разворачивает варианты вида «быстрый, скорый / стремительный» в отдельные строки. */
    public List<String> expandCandidates(List<String> acceptedInputs) {
        Set<String> result = new LinkedHashSet<>();
        if (acceptedInputs == null) {
            return new ArrayList<>();
        }
        for (String raw : acceptedInputs) {
            if (raw == null) {
                continue;
            }
            boolean added = false;
            for (String variant : raw.split("[,;/|]")) {
                String normalized = normalize(variant);
                if (!normalized.isBlank()) {
                    result.add(normalized);
                    added = true;
                }
            }
            if (!added) {
                String normalized = normalize(raw);
                if (!normalized.isBlank()) {
                    result.add(normalized);
                }
            }
        }
        return new ArrayList<>(result);
    }

    public String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll(DIACRITICS, "")
                .toLowerCase()
                .replaceAll(NON_WORD, " ")
                .replaceAll(MULTIPLE_SPACES, " ")
                .trim();
    }

    /** Расстояние Левенштейна с двумя строками памяти вместо полной матрицы. */
    public int levenshtein(String left, String right) {
        int m = left.length();
        int n = right.length();
        if (m == 0) {
            return n;
        }
        if (n == 0) {
            return m;
        }

        int[] previous = new int[n + 1];
        int[] current = new int[n + 1];
        for (int j = 0; j <= n; j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= m; i++) {
            current[0] = i;
            char leftChar = left.charAt(i - 1);
            for (int j = 1; j <= n; j++) {
                int cost = leftChar == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[n];
    }

    /**
     * @param quality      качество ответа 1..5
     * @param correct      считать ли ответ правильным
     * @param matchedAnswer нормализованный вариант, оказавшийся ближайшим
     */
    public record Evaluation(int quality, boolean correct, String matchedAnswer) {
    }
}
