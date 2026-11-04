package ru.mindlex.dashboard;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.chain.SynonymChainRepository;
import ru.mindlex.cloze.ClozeExerciseRepository;
import ru.mindlex.common.TimeZones;
import ru.mindlex.deck.Deck;
import ru.mindlex.deck.DeckRepository;
import ru.mindlex.domain.Mechanic;
import ru.mindlex.domain.WordStatus;
import ru.mindlex.practice.SessionResponse;
import ru.mindlex.progress.ProgressService;
import ru.mindlex.progress.ReviewLogRepository;
import ru.mindlex.settings.SettingsService;
import ru.mindlex.settings.UserSettings;
import ru.mindlex.session.StudySessionRepository;
import ru.mindlex.word.WordRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Собирает данные главного экрана: серия занятий, дневная цель, статистика слов и рекорды.
 */
@Service
public class DashboardService {

    private static final int RECENT_SESSIONS = 5;

    private final SettingsService settingsService;
    private final ProgressService progressService;
    private final WordRepository wordRepository;
    private final DeckRepository deckRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final StudySessionRepository sessionRepository;
    private final ClozeExerciseRepository clozeRepository;
    private final SynonymChainRepository chainRepository;
    private final Clock clock;

    public DashboardService(SettingsService settingsService,
                            ProgressService progressService,
                            WordRepository wordRepository,
                            DeckRepository deckRepository,
                            ReviewLogRepository reviewLogRepository,
                            StudySessionRepository sessionRepository,
                            ClozeExerciseRepository clozeRepository,
                            SynonymChainRepository chainRepository,
                            Clock clock) {
        this.settingsService = settingsService;
        this.progressService = progressService;
        this.wordRepository = wordRepository;
        this.deckRepository = deckRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.sessionRepository = sessionRepository;
        this.clozeRepository = clozeRepository;
        this.chainRepository = chainRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardResponse build(UUID userId) {
        UserSettings settings = settingsService.require(userId);
        ZoneId zone = TimeZones.of(settings.getTimezone());
        Instant startOfDay = LocalDate.now(clock.withZone(zone)).atStartOfDay(zone).toInstant();

        int todayAnswered = (int) reviewLogRepository.countByUserIdAndCreatedAtGreaterThanEqual(userId, startOfDay);
        int todayCorrect = (int) reviewLogRepository
                .countByUserIdAndCreatedAtGreaterThanEqualAndCorrectTrue(userId, startOfDay);

        Deck deck = settings.getSelectedDeckId() == null
                ? null
                : deckRepository.findById(settings.getSelectedDeckId()).orElse(null);

        long totalWords = deck == null ? 0 : wordRepository.countByDeckId(deck.getId());
        long dueCount = progressService.countDue(userId);
        long knownWords = progressService.countByStatus(userId, Set.of(WordStatus.KNOWN));
        long learningWords = progressService.countByStatus(userId, Set.of(WordStatus.LEARNING, WordStatus.REVIEW));
        long newWords = deck == null ? 0 : Math.max(0, totalWords - knownWords - learningWords);

        int goalPercent = settings.getDailyGoal() == 0
                ? 0
                : (int) Math.min(100, Math.round(todayAnswered * 100.0 / settings.getDailyGoal()));

        return new DashboardResponse(
                settingsService.visibleStreak(settings),
                settings.getDailyGoal(),
                todayAnswered,
                todayCorrect,
                goalPercent,
                dueCount,
                totalWords,
                newWords,
                learningWords,
                knownWords,
                settings.getBestRushScore(),
                settings.getMaxComboRecord(),
                deck == null ? null : deck.getId(),
                deck == null ? null : deck.getName(),
                settings.getLanguage(),
                buildMechanics(deck, dueCount, newWords, learningWords),
                sessionRepository.findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(0, RECENT_SESSIONS))
                        .getContent().stream()
                        .map(session -> SessionResponse.from(session, null))
                        .toList());
    }

    private List<DashboardResponse.MechanicSummary> buildMechanics(Deck deck, long dueCount, long newWords,
                                                                   long learningWords) {
        int wordPool = (int) (dueCount + newWords + learningWords);
        int clozePool = deck == null ? 0 : (int) clozeRepository.countByDeckId(deck.getId());
        int chains = (int) chainRepository.count();

        return List.of(
                new DashboardResponse.MechanicSummary(Mechanic.ACTIVE_RECALL,
                        "Активное припоминание",
                        "Перевод слов с родного языка на изучаемый", wordPool),
                new DashboardResponse.MechanicSummary(Mechanic.CLOZE,
                        "Контекстные пропуски",
                        "Заполнение пропусков в предложениях на время", clozePool),
                new DashboardResponse.MechanicSummary(Mechanic.RUSH,
                        "Скоростной раунд",
                        "Перевод на скорость с комбо-множителем", wordPool),
                new DashboardResponse.MechanicSummary(Mechanic.SYNONYM_CHAIN,
                        "Цепочка синонимов",
                        "Подбор синонимов и построение смысловых связей", chains));
    }
}
