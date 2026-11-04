package ru.mindlex.dashboard;

import ru.mindlex.domain.Mechanic;
import ru.mindlex.practice.SessionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Сводка для главного экрана приложения.
 *
 * @param visibleStreak   серия занятий, которую нужно показать (0, если занимались раньше вчера)
 * @param todayAnswered   сколько ответов дано сегодня
 * @param dailyGoal       дневная цель по количеству слов
 */
public record DashboardResponse(int visibleStreak,
                                int dailyGoal,
                                int todayAnswered,
                                int todayCorrect,
                                int dailyGoalPercent,
                                long dueCount,
                                long totalWords,
                                long newWords,
                                long learningWords,
                                long knownWords,
                                int bestRushScore,
                                int maxComboRecord,
                                UUID selectedDeckId,
                                String selectedDeckName,
                                String language,
                                List<MechanicSummary> mechanics,
                                List<SessionResponse> recentSessions) {

    /**
     * Доступность механики и её описание для меню тренировок.
     *
     * @param availableQuestions сколько заданий готово к выдаче
     */
    public record MechanicSummary(Mechanic mechanic,
                                  String title,
                                  String description,
                                  int availableQuestions) {
    }
}
