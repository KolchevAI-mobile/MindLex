package ru.mindlex.practice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.chain.SynonymChainStep;
import ru.mindlex.chain.SynonymChainStepRepository;
import ru.mindlex.cloze.ClozeExercise;
import ru.mindlex.cloze.ClozeExerciseRepository;
import ru.mindlex.common.BusinessException;
import ru.mindlex.common.NotFoundException;
import ru.mindlex.common.PageResponse;
import ru.mindlex.common.TextUtils;
import ru.mindlex.config.MindLexProperties;
import ru.mindlex.deck.Deck;
import ru.mindlex.deck.DeckRepository;
import ru.mindlex.domain.Mechanic;
import ru.mindlex.domain.SessionStatus;
import ru.mindlex.domain.WordStatus;
import ru.mindlex.progress.ProgressService;
import ru.mindlex.progress.ReviewLog;
import ru.mindlex.progress.ReviewLogRepository;
import ru.mindlex.progress.WordProgress;
import ru.mindlex.progress.WordProgressResponse;
import ru.mindlex.settings.SettingsService;
import ru.mindlex.settings.UserSettings;
import ru.mindlex.session.StudySession;
import ru.mindlex.session.StudySessionRepository;
import ru.mindlex.word.Word;
import ru.mindlex.word.WordRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Игровое ядро приложения.
 *
 * <p>Сервер полностью отвечает за механику: выбирает вопрос, проверяет ответ,
 * пересчитывает прогресс слова и начисляет очки. Клиент лишь отображает данные,
 * поэтому правила игры нельзя подделать на устройстве.</p>
 *
 * <p>Сессия считается завершённой, когда пройдено {@code questionLimit} вопросов
 * либо (для Timed Rush) истекло время раунда. Признак завершения возвращается
 * клиенту, а фиксация итогов происходит в отдельном запросе
 * {@code POST /api/v1/sessions/{id}/finish}.</p>
 */
@Service
public class PracticeService {

    private static final Logger log = LoggerFactory.getLogger(PracticeService.class);

    /** Сколько последних вопросов исключать из выдачи, чтобы слова не повторялись. */
    private static final int CANDIDATE_LIMIT = 40;
    private static final int MIN_RUSH_SECONDS = 60;
    private static final int MAX_RUSH_SECONDS = 90;
    private static final int MIN_CLOZE_SECONDS = 30;
    private static final int MAX_CLOZE_SECONDS = 45;
    private static final int CHAIN_HINT_OPTIONS = 3;

    private final StudySessionRepository sessionRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final WordRepository wordRepository;
    private final ClozeExerciseRepository clozeRepository;
    private final SynonymChainStepRepository chainStepRepository;
    private final DeckRepository deckRepository;
    private final SettingsService settingsService;
    private final ProgressService progressService;
    private final AnswerEvaluator answerEvaluator;
    private final RushScorer rushScorer;
    private final MindLexProperties properties;
    private final Clock clock;

    public PracticeService(StudySessionRepository sessionRepository,
                           ReviewLogRepository reviewLogRepository,
                           WordRepository wordRepository,
                           ClozeExerciseRepository clozeRepository,
                           SynonymChainStepRepository chainStepRepository,
                           DeckRepository deckRepository,
                           SettingsService settingsService,
                           ProgressService progressService,
                           AnswerEvaluator answerEvaluator,
                           RushScorer rushScorer,
                           MindLexProperties properties,
                           Clock clock) {
        this.sessionRepository = sessionRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.wordRepository = wordRepository;
        this.clozeRepository = clozeRepository;
        this.chainStepRepository = chainStepRepository;
        this.deckRepository = deckRepository;
        this.settingsService = settingsService;
        this.progressService = progressService;
        this.answerEvaluator = answerEvaluator;
        this.rushScorer = rushScorer;
        this.properties = properties;
        this.clock = clock;
    }

    // ------------------------------------------------------------------
    // Жизненный цикл сессии
    // ------------------------------------------------------------------

    @Transactional
    public SessionResponse startSession(UUID userId, StartSessionRequest request) {
        UserSettings settings = settingsService.require(userId);
        UUID deckId = resolveDeckId(userId, settings, request.deckId());
        finishStaleSessions(userId);

        StudySession session = new StudySession();
        session.setUserId(userId);
        session.setDeckId(deckId);
        session.setMechanic(request.mechanic());
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(clock.instant());

        if (request.mechanic() == Mechanic.RUSH) {
            session.setDurationSeconds(clamp(settings.getRushSeconds(), MIN_RUSH_SECONDS, MAX_RUSH_SECONDS));
            session.setQuestionLimit(0);
        } else {
            session.setDurationSeconds(0);
            session.setQuestionLimit(Math.max(1, settings.getDailyGoal()));
        }

        sessionRepository.save(session);
        log.debug("Пользователь {} начал сессию {} механики {}", userId, session.getId(), request.mechanic());
        return SessionResponse.from(session, timeRemainingSeconds(session));
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(UUID userId, UUID sessionId) {
        StudySession session = requireOwnedSession(userId, sessionId);
        return SessionResponse.from(session, timeRemainingSeconds(session));
    }

    @Transactional(readOnly = true)
    public PageResponse<SessionResponse> listSessions(UUID userId, int page, int size) {
        Page<StudySession> sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(userId,
                PageRequest.of(page, size));
        return PageResponse.from(sessions, session -> SessionResponse.from(session, null));
    }

    @Transactional
    public void deleteSession(UUID userId, UUID sessionId) {
        sessionRepository.delete(requireOwnedSession(userId, sessionId));
    }

    // ------------------------------------------------------------------
    // Вопросы
    // ------------------------------------------------------------------

    /**
     * Следующий вопрос сессии.
     *
     * @return вопрос или {@link Optional#empty()}, если сессия исчерпана —
     *         клиент должен вызвать завершение сессии
     */
    @Transactional
    public Optional<QuestionResponse> nextQuestion(UUID userId, UUID sessionId) {
        StudySession session = requireOwnedSession(userId, sessionId);
        if (session.getStatus() != SessionStatus.ACTIVE) {
            return Optional.empty();
        }
        if (isExpired(session)) {
            return Optional.empty();
        }
        int answered = answeredCount(session);
        if (session.getQuestionLimit() > 0 && answered >= session.getQuestionLimit()) {
            return Optional.empty();
        }

        Set<UUID> excluded = new HashSet<>(reviewLogRepository.findQuestionRefs(sessionId));
        UserSettings settings = settingsService.require(userId);
        int index = answered + 1;

        return switch (session.getMechanic()) {
            case ACTIVE_RECALL, RUSH -> buildWordQuestion(userId, session, excluded, index, settings);
            case CLOZE -> buildClozeQuestion(session, excluded, index, settings);
            case SYNONYM_CHAIN -> buildChainQuestion(session, index);
        };
    }

    private Optional<QuestionResponse> buildWordQuestion(UUID userId,
                                                         StudySession session,
                                                         Set<UUID> excluded,
                                                         int index,
                                                         UserSettings settings) {
        Word word = selectWord(userId, session.getDeckId(), excluded);
        if (word == null) {
            throw new BusinessException("В выбранной колоде нет слов для тренировки");
        }
        String hint = word.getExample() != null ? word.getExample() : word.getTranscription();
        return Optional.of(new QuestionResponse(
                session.getId(),
                word.getId(),
                session.getMechanic(),
                index,
                session.getQuestionLimit(),
                QuestionResponse.KIND_WORD,
                word.getTranslation(),
                hint,
                word.getTranscription(),
                word.getPartOfSpeech(),
                word.getExample(),
                null,
                null,
                null,
                null));
    }

    private Optional<QuestionResponse> buildClozeQuestion(StudySession session,
                                                          Set<UUID> excluded,
                                                          int index,
                                                          UserSettings settings) {
        ClozeExercise exercise = selectCloze(session.getDeckId(), excluded);
        if (exercise == null) {
            throw new BusinessException("Для выбранной колоды нет упражнений с пропусками");
        }
        int timeLimit = clamp(settings.getClozeSeconds(), MIN_CLOZE_SECONDS, MAX_CLOZE_SECONDS);
        return Optional.of(new QuestionResponse(
                session.getId(),
                exercise.getId(),
                session.getMechanic(),
                index,
                session.getQuestionLimit(),
                QuestionResponse.KIND_SENTENCE,
                exercise.getSentence(),
                exercise.getHint(),
                null,
                null,
                null,
                null,
                null,
                timeLimit,
                null));
    }

    private Optional<QuestionResponse> buildChainQuestion(StudySession session, int index) {
        SynonymChainStep step = selectChainStep(session.getId());
        if (step == null) {
            throw new BusinessException("В приложении нет цепочек синонимов");
        }
        int chainLength = (int) chainStepRepository.countByChainId(step.getChainId());
        List<String> synonyms = answerEvaluator.expandCandidates(List.of(step.getAcceptedSynonyms()));
        List<String> hintOptions = randomSubset(synonyms, CHAIN_HINT_OPTIONS);

        String hint = step.getHint() != null ? step.getHint() : String.join(", ", hintOptions);
        return Optional.of(new QuestionResponse(
                session.getId(),
                step.getId(),
                session.getMechanic(),
                index,
                session.getQuestionLimit(),
                QuestionResponse.KIND_CHAIN_STEP,
                step.getPromptWord(),
                hint,
                null,
                null,
                null,
                step.getStepNumber(),
                chainLength,
                null,
                hintOptions));
    }

    // ------------------------------------------------------------------
    // Ответы
    // ------------------------------------------------------------------

    @Transactional
    public AnswerResponse answer(UUID userId, UUID sessionId, AnswerRequest request) {
        StudySession session = requireActiveSession(userId, sessionId);
        ensureNotAnswered(sessionId, request.questionId());

        return switch (session.getMechanic()) {
            case ACTIVE_RECALL, RUSH -> evaluateWordAnswer(userId, session, request);
            case CLOZE -> evaluateClozeAnswer(userId, session, request);
            case SYNONYM_CHAIN -> evaluateChainAnswer(userId, session, request);
        };
    }

    /** Пропуск вопроса: серия сбрасывается, прогресс слова не меняется. */
    @Transactional
    public AnswerResponse skip(UUID userId, UUID sessionId, UUID questionId) {
        StudySession session = requireActiveSession(userId, sessionId);
        ensureNotAnswered(sessionId, questionId);

        session.setComboStreak(0);
        session.setSkipCount(session.getSkipCount() + 1);
        sessionRepository.save(session);

        writeLog(userId, session, questionId, resolveWordId(session.getMechanic(), questionId), 1,
                null, null, false, 0L);

        return new AnswerResponse(session.getId(), questionId, false, 1, null, 0, session.getScore(), 0,
                session.getMaxCombo(), session.getCorrectCount(), session.getIncorrectCount(),
                session.getSkipCount(), timeRemainingSeconds(session), isCompleted(session), null);
    }

    @Transactional
    public SessionSummaryResponse finishSession(UUID userId, UUID sessionId) {
        StudySession session = requireOwnedSession(userId, sessionId);
        UserSettings settings = settingsService.require(userId);

        if (session.getStatus() == SessionStatus.ACTIVE) {
            session.setStatus(SessionStatus.FINISHED);
            session.setFinishedAt(clock.instant());
            sessionRepository.save(session);
        }

        boolean newRecord = false;
        int score = session.getScore();
        if (session.getMechanic() == Mechanic.RUSH) {
            if (score > settings.getBestRushScore()) {
                settings.setBestRushScore(score);
                newRecord = true;
            }
            if (session.getMaxCombo() > settings.getMaxComboRecord()) {
                settings.setMaxComboRecord(session.getMaxCombo());
            }
        }

        return buildSummary(session, settings, newRecord);
    }

    // ------------------------------------------------------------------
    // Проверка ответов по механикам
    // ------------------------------------------------------------------

    private AnswerResponse evaluateWordAnswer(UUID userId, StudySession session, AnswerRequest request) {
        Word word = wordRepository.findById(request.questionId())
                .orElseThrow(() -> NotFoundException.of("Слово", request.questionId()));

        List<String> accepted = new ArrayList<>();
        accepted.add(word.getTerm());
        accepted.addAll(TextUtils.splitCsv(word.getAlternatives()));

        AnswerEvaluator.Evaluation evaluation = answerEvaluator.evaluate(request.answer(), accepted);
        int quality = request.isHintUsed() ? AnswerEvaluator.HINT_QUALITY : evaluation.quality();
        boolean correct = quality >= AnswerEvaluator.ACCEPTANCE_QUALITY;

        return registerAnswer(userId, session, request, word.getId(), word.getId(),
                quality, correct, word, word.getTerm());
    }

    private AnswerResponse evaluateClozeAnswer(UUID userId, StudySession session, AnswerRequest request) {
        ClozeExercise exercise = clozeRepository.findById(request.questionId())
                .orElseThrow(() -> NotFoundException.of("Упражнение cloze", request.questionId()));

        UserSettings settings = settingsService.require(userId);
        int limitSeconds = clamp(settings.getClozeSeconds(), MIN_CLOZE_SECONDS, MAX_CLOZE_SECONDS);
        boolean timeout = request.isTimedOut() || request.elapsedMillis() > limitSeconds * 1000L;

        int quality;
        boolean correct;
        if (timeout) {
            // Как и в исходном приложении, истёкшее время засчитывается как ошибка,
            // но прогресс слова не обнуляется полностью.
            quality = AnswerEvaluator.HINT_QUALITY;
            correct = false;
        } else {
            AnswerEvaluator.Evaluation evaluation =
                    answerEvaluator.evaluate(request.answer(), List.of(exercise.getAnswer()));
            quality = request.isHintUsed() ? AnswerEvaluator.HINT_QUALITY : evaluation.quality();
            correct = quality >= AnswerEvaluator.ACCEPTANCE_QUALITY;
        }

        Word word = wordRepository.findById(exercise.getWordId()).orElse(null);
        return registerAnswer(userId, session, request, exercise.getWordId(), exercise.getId(),
                quality, correct, word, exercise.getAnswer());
    }

    private AnswerResponse evaluateChainAnswer(UUID userId, StudySession session, AnswerRequest request) {
        SynonymChainStep step = chainStepRepository.findById(request.questionId())
                .orElseThrow(() -> NotFoundException.of("Шаг цепочки синонимов", request.questionId()));

        List<String> accepted = TextUtils.splitCsv(step.getAcceptedSynonyms());
        boolean correct = answerEvaluator.matchesExactly(request.answer(), accepted);
        int quality = correct ? 4 : 2;

        AnswerResponse response = registerAnswer(userId, session, request, null, step.getId(),
                quality, correct, null, accepted.isEmpty() ? null : accepted.get(0));
        return response;
    }

    /** Общее обновление счётчиков, очков, прогресса и журнала. */
    private AnswerResponse registerAnswer(UUID userId,
                                          StudySession session,
                                          AnswerRequest request,
                                          UUID wordId,
                                          UUID questionRef,
                                          int quality,
                                          boolean correct,
                                          Word word,
                                          String expectedAnswer) {
        int combo = correct ? session.getComboStreak() + 1 : 0;
        Integer remaining = timeRemainingSeconds(session);
        int scoreDelta = correct ? rushScorer.score(combo, remaining == null ? 0 : remaining) : 0;

        session.setComboStreak(combo);
        session.setMaxCombo(Math.max(session.getMaxCombo(), combo));
        session.setScore(session.getScore() + scoreDelta);
        if (correct) {
            session.setCorrectCount(session.getCorrectCount() + 1);
        } else {
            session.setIncorrectCount(session.getIncorrectCount() + 1);
        }
        sessionRepository.save(session);

        WordProgress progress = progressService.applyReview(userId, wordId, quality);
        WordProgressResponse progressResponse = progress == null
                ? null
                : WordProgressResponse.from(progress,
                        word == null ? null : word.getTerm(),
                        word == null ? null : word.getTranslation());

        writeLog(userId, session, request.questionId(), wordId, quality,
                request.answer(), expectedAnswer, correct, request.elapsedMillis());

        return new AnswerResponse(session.getId(), request.questionId(), correct, quality, expectedAnswer,
                scoreDelta, session.getScore(), combo, session.getMaxCombo(), session.getCorrectCount(),
                session.getIncorrectCount(), session.getSkipCount(), remaining, isCompleted(session),
                progressResponse);
    }

    private void writeLog(UUID userId, StudySession session, UUID questionRef, UUID wordId, int quality,
                          String answer, String expectedAnswer, boolean correct, long elapsedMs) {
        ReviewLog logEntry = new ReviewLog();
        logEntry.setUserId(userId);
        logEntry.setSessionId(session.getId());
        logEntry.setQuestionRef(questionRef);
        logEntry.setWordId(wordId);
        logEntry.setMechanic(session.getMechanic());
        logEntry.setQuality(quality);
        logEntry.setAnswer(answer);
        logEntry.setExpectedAnswer(expectedAnswer);
        logEntry.setCorrect(correct);
        logEntry.setResponseTimeMs(elapsedMs);
        reviewLogRepository.save(logEntry);
    }

    // ------------------------------------------------------------------
    // Выбор вопросов
    // ------------------------------------------------------------------

    /** Порядок выбора слова повторяет исходное приложение: повторение → новое → изучаемое → случайное. */
    private Word selectWord(UUID userId, UUID deckId, Set<UUID> excluded) {
        Instant now = clock.instant();
        List<Word> due = filterExcluded(wordRepository.findDueWords(userId, deckId, now,
                Set.of(WordStatus.NEW, WordStatus.LEARNING, WordStatus.REVIEW), PageRequest.of(0, CANDIDATE_LIMIT)),
                excluded);
        if (!due.isEmpty()) {
            return pickRandom(due);
        }

        List<Word> fresh = filterExcluded(wordRepository.findNotStarted(deckId, userId,
                PageRequest.of(0, properties.practice().newWordsPerSession())), excluded);
        if (!fresh.isEmpty()) {
            return pickRandom(fresh);
        }

        List<Word> learning = filterExcluded(wordRepository.findLearningWords(userId, deckId,
                Set.of(WordStatus.LEARNING, WordStatus.REVIEW), PageRequest.of(0, CANDIDATE_LIMIT)), excluded);
        if (!learning.isEmpty()) {
            return pickRandom(learning);
        }

        List<Word> random = excluded.isEmpty()
                ? wordRepository.findRandomByDeck(deckId, 1)
                : wordRepository.findRandomByDeckExcluding(deckId, excluded, 1);
        if (random.isEmpty() && !excluded.isEmpty()) {
            random = wordRepository.findRandomByDeck(deckId, 1);
        }
        return random.isEmpty() ? null : random.get(0);
    }

    private ClozeExercise selectCloze(UUID deckId, Set<UUID> excluded) {
        List<ClozeExercise> pool = excluded.isEmpty()
                ? clozeRepository.findRandomByDeck(deckId, 1)
                : clozeRepository.findRandomByDeckExcluding(deckId, excluded, 1);
        if (pool.isEmpty() && !excluded.isEmpty()) {
            pool = clozeRepository.findRandomByDeck(deckId, 1);
        }
        return pool.isEmpty() ? null : pool.get(0);
    }

    /** Продолжает текущую цепочку синонимов, а когда она закончилась — начинает новую. */
    private SynonymChainStep selectChainStep(UUID sessionId) {
        Optional<ReviewLog> lastChainLog =
                reviewLogRepository.findFirstBySessionIdAndMechanicOrderByCreatedAtDesc(sessionId,
                        Mechanic.SYNONYM_CHAIN);

        if (lastChainLog.isPresent() && lastChainLog.get().getQuestionRef() != null) {
            Optional<SynonymChainStep> previous =
                    chainStepRepository.findById(lastChainLog.get().getQuestionRef());
            if (previous.isPresent()) {
                SynonymChainStep previousStep = previous.get();
                List<SynonymChainStep> nextSteps =
                        chainStepRepository.findNextSteps(previousStep.getChainId(), previousStep.getStepNumber());
                if (!nextSteps.isEmpty()) {
                    return nextSteps.get(0);
                }
            }
        }

        UUID chainId = chainStepRepository.findRandomChainId();
        if (chainId == null) {
            return null;
        }
        Integer firstStepNumber = chainStepRepository.findFirstStepNumber(chainId);
        if (firstStepNumber == null) {
            return null;
        }
        return chainStepRepository.findByChainIdAndStepNumber(chainId, firstStepNumber).orElse(null);
    }

    private List<Word> filterExcluded(List<Word> words, Set<UUID> excluded) {
        if (excluded.isEmpty()) {
            return words;
        }
        return words.stream().filter(word -> !excluded.contains(word.getId())).toList();
    }

    private <T> T pickRandom(List<T> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

    private List<String> randomSubset(List<String> values, int size) {
        if (values.size() <= size) {
            return List.copyOf(values);
        }
        List<String> copy = new ArrayList<>(values);
        Collections.shuffle(copy);
        return List.copyOf(copy.subList(0, size));
    }

    // ------------------------------------------------------------------
    // Вспомогательные методы
    // ------------------------------------------------------------------

    private UUID resolveDeckId(UUID userId, UserSettings settings, UUID requestedDeckId) {
        if (requestedDeckId != null) {
            Deck deck = deckRepository.findById(requestedDeckId)
                    .orElseThrow(() -> NotFoundException.of("Колода", requestedDeckId));
            if (!deck.isPublicDeck() && !userId.equals(deck.getOwnerId())) {
                throw new AccessDeniedException("Колода принадлежит другому пользователю");
            }
            return deck.getId();
        }
        if (settings.getSelectedDeckId() != null && deckRepository.existsById(settings.getSelectedDeckId())) {
            return settings.getSelectedDeckId();
        }
        List<Deck> available = deckRepository.findAvailable(userId);
        if (available.isEmpty()) {
            throw new BusinessException("Нет доступных колод: создайте колоду и добавьте слова");
        }
        return available.get(0).getId();
    }

    private void finishStaleSessions(UUID userId) {
        sessionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(userId, SessionStatus.ACTIVE)
                .ifPresent(stale -> {
                    stale.setStatus(SessionStatus.FINISHED);
                    stale.setFinishedAt(clock.instant());
                    sessionRepository.save(stale);
                });
    }

    private StudySession requireOwnedSession(UUID userId, UUID sessionId) {
        StudySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> NotFoundException.of("Сессия", sessionId));
        if (!session.getUserId().equals(userId)) {
            throw new AccessDeniedException("Сессия принадлежит другому пользователю");
        }
        return session;
    }

    private StudySession requireActiveSession(UUID userId, UUID sessionId) {
        StudySession session = requireOwnedSession(userId, sessionId);
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BusinessException("Сессия уже завершена");
        }
        if (isExpired(session)) {
            throw new BusinessException("Время раунда истекло");
        }
        return session;
    }

    private void ensureNotAnswered(UUID sessionId, UUID questionRef) {
        if (reviewLogRepository.findQuestionRefs(sessionId).contains(questionRef)) {
            throw new BusinessException("На этот вопрос уже дан ответ");
        }
    }

    private boolean isExpired(StudySession session) {
        return session.getDurationSeconds() > 0 && timeRemainingSeconds(session) <= 0;
    }

    private boolean isCompleted(StudySession session) {
        if (isExpired(session)) {
            return true;
        }
        return session.getQuestionLimit() > 0 && answeredCount(session) >= session.getQuestionLimit();
    }

    private int answeredCount(StudySession session) {
        return session.getCorrectCount() + session.getIncorrectCount() + session.getSkipCount();
    }

    private Integer timeRemainingSeconds(StudySession session) {
        if (session.getDurationSeconds() <= 0) {
            return null;
        }
        long elapsed = Duration.between(session.getStartedAt(), clock.instant()).toSeconds();
        return (int) Math.max(0, session.getDurationSeconds() - elapsed);
    }

    private UUID resolveWordId(Mechanic mechanic, UUID questionRef) {
        return switch (mechanic) {
            case ACTIVE_RECALL, RUSH -> questionRef;
            case CLOZE -> clozeRepository.findById(questionRef).map(ClozeExercise::getWordId).orElse(null);
            case SYNONYM_CHAIN -> null;
        };
    }

    private SessionSummaryResponse buildSummary(StudySession session, UserSettings settings, boolean newRecord) {
        int answered = answeredCount(session);
        int accuracy = answered == 0 ? 0 : (int) Math.round(session.getCorrectCount() * 100.0 / answered);
        int duration = session.getFinishedAt() == null
                ? session.getDurationSeconds()
                : (int) Duration.between(session.getStartedAt(), session.getFinishedAt()).toSeconds();
        return new SessionSummaryResponse(session.getId(), session.getMechanic(), session.getScore(),
                session.getCorrectCount(), session.getIncorrectCount(), session.getSkipCount(), answered,
                session.getMaxCombo(), accuracy, rushScorer.wordsPerMinute(answered, duration),
                session.getDurationSeconds(), settings.getBestRushScore(), settings.getMaxComboRecord(),
                newRecord, session.getStartedAt(), session.getFinishedAt());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
