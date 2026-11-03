package ru.mindlex.progress;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.common.NotFoundException;
import ru.mindlex.common.PageResponse;
import ru.mindlex.common.TimeZones;
import ru.mindlex.domain.WordStatus;
import ru.mindlex.practice.SrsCalculator;
import ru.mindlex.settings.SettingsService;
import ru.mindlex.settings.UserSettings;
import ru.mindlex.word.Word;
import ru.mindlex.word.WordRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Прогресс изучения слова: пересчёт параметров интервального повторения и серия занятий.
 */
@Service
public class ProgressService {

    private final WordProgressRepository progressRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final WordRepository wordRepository;
    private final SettingsService settingsService;
    private final SrsCalculator srsCalculator;
    private final Clock clock;

    public ProgressService(WordProgressRepository progressRepository,
                           ReviewLogRepository reviewLogRepository,
                           WordRepository wordRepository,
                           SettingsService settingsService,
                           SrsCalculator srsCalculator,
                           Clock clock) {
        this.progressRepository = progressRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.wordRepository = wordRepository;
        this.settingsService = settingsService;
        this.srsCalculator = srsCalculator;
        this.clock = clock;
    }

    /**
     * Пересчитывает прогресс слова после ответа и обновляет серию занятий.
     *
     * @param userId  пользователь
     * @param wordId  слово (может быть {@code null} для шагов цепочки синонимов)
     * @param quality качество ответа 1..5
     * @return новое состояние прогресса или {@code null}, если слово не привязано
     */
    @Transactional
    public WordProgress applyReview(UUID userId, UUID wordId, int quality) {
        registerActivity(userId);
        if (wordId == null) {
            return null;
        }

        Instant now = clock.instant();
        Optional<WordProgress> existing = progressRepository.findByUserIdAndWordId(userId, wordId);
        int currentLevel = existing.map(WordProgress::getLevel).orElse(0);
        int previousInterval = existing.map(WordProgress::getIntervalDays).orElse(0);
        double currentEase = existing.map(WordProgress::getEaseFactor).orElse(SrsCalculator.DEFAULT_EASE_FACTOR);

        SrsCalculator.SrsOutcome outcome = srsCalculator.calculate(currentLevel, previousInterval, currentEase,
                quality, now);

        WordProgress progress = existing.orElseGet(() -> {
            WordProgress created = new WordProgress();
            created.setUserId(userId);
            created.setWordId(wordId);
            return created;
        });
        progress.setStatus(outcome.status());
        progress.setLevel(outcome.level());
        progress.setEaseFactor(outcome.easeFactor());
        progress.setIntervalDays(outcome.intervalDays());
        progress.setNextReviewAt(outcome.nextReviewAt());
        progress.setLastReviewedAt(now);
        if (quality >= 3) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
        } else {
            progress.setIncorrectCount(progress.getIncorrectCount() + 1);
        }
        return progressRepository.save(progress);
    }

    /** Серия занятий увеличивается один раз в календарный день (в часовом поясе пользователя). */
    @Transactional
    public void registerActivity(UUID userId) {
        UserSettings settings = settingsService.require(userId);
        LocalDate today = LocalDate.now(clock.withZone(zoneOf(settings)));
        LocalDate lastStudyDate = settings.getLastStudyDate();
        if (today.equals(lastStudyDate)) {
            return;
        }
        int nextStreak = today.minusDays(1).equals(lastStudyDate) ? settings.getCurrentStreak() + 1 : 1;
        settings.setCurrentStreak(nextStreak);
        settings.setLastStudyDate(today);
    }

    @Transactional(readOnly = true)
    public PageResponse<WordProgressResponse> list(UUID userId, int page, int size) {
        Page<WordProgress> pageData = progressRepository.findByUserId(userId, PageRequest.of(page, size));
        Map<UUID, Word> words = loadWords(pageData.getContent().stream().map(WordProgress::getWordId).toList());
        return PageResponse.from(pageData, progress -> toResponse(progress, words));
    }

    @Transactional(readOnly = true)
    public WordProgressResponse get(UUID userId, UUID wordId) {
        WordProgress progress = progressRepository.findByUserIdAndWordId(userId, wordId)
                .orElseThrow(() -> NotFoundException.of("Прогресс по слову", wordId));
        return toResponse(progress, loadWords(List.of(wordId)));
    }

    @Transactional
    public void reset(UUID userId, UUID wordId) {
        progressRepository.deleteByUserIdAndWordId(userId, wordId);
    }

    @Transactional
    public void resetAll(UUID userId) {
        progressRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countDue(UUID userId) {
        return progressRepository.countByUserIdAndNextReviewAtLessThanEqualAndStatusIn(
                userId, clock.instant(), dueStatuses());
    }

    @Transactional(readOnly = true)
    public long countByStatus(UUID userId, Set<WordStatus> statuses) {
        return progressRepository.countByUserIdAndStatusIn(userId, statuses);
    }

    public static Set<WordStatus> dueStatuses() {
        return Set.of(WordStatus.NEW, WordStatus.LEARNING, WordStatus.REVIEW);
    }

    private WordProgressResponse toResponse(WordProgress progress, Map<UUID, Word> words) {
        Word word = words.get(progress.getWordId());
        return WordProgressResponse.from(progress,
                word == null ? null : word.getTerm(),
                word == null ? null : word.getTranslation());
    }

    private Map<UUID, Word> loadWords(List<UUID> wordIds) {
        if (wordIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Word> result = new HashMap<>();
        wordRepository.findAllByIds(wordIds).forEach(word -> result.put(word.getId(), word));
        return result;
    }

    private ZoneId zoneOf(UserSettings settings) {
        return TimeZones.of(settings.getTimezone());
    }
}
