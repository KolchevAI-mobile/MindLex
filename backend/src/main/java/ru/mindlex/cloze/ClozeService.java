package ru.mindlex.cloze;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.common.BusinessException;
import ru.mindlex.common.ConflictException;
import ru.mindlex.common.NotFoundException;
import ru.mindlex.common.PageResponse;
import ru.mindlex.deck.DeckService;
import ru.mindlex.word.Word;
import ru.mindlex.word.WordService;

import java.util.UUID;

/**
 * CRUD над cloze-упражнениями слова. Упражнения вложены в слово,
 * поэтому право на изменение проверяется через колоду слова.
 */
@Service
public class ClozeService {

    /** Обязательный маркер пропуска в предложении. */
    private static final String GAP = "___";

    private final ClozeExerciseRepository clozeExerciseRepository;
    private final WordService wordService;
    private final DeckService deckService;

    public ClozeService(ClozeExerciseRepository clozeExerciseRepository,
                        WordService wordService,
                        DeckService deckService) {
        this.clozeExerciseRepository = clozeExerciseRepository;
        this.wordService = wordService;
        this.deckService = deckService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ClozeExerciseResponse> list(UUID wordId, int page, int size) {
        wordService.require(wordId);
        Page<ClozeExercise> exercises = clozeExerciseRepository.findByWordId(wordId, PageRequest.of(page, size));
        return PageResponse.from(exercises, ClozeExerciseResponse::from);
    }

    @Transactional(readOnly = true)
    public ClozeExerciseResponse get(UUID exerciseId) {
        return ClozeExerciseResponse.from(require(exerciseId));
    }

    @Transactional
    public ClozeExerciseResponse create(UUID userId, UUID wordId, ClozeExerciseRequest request) {
        Word word = wordService.require(wordId);
        deckService.requireOwned(userId, word.getDeckId());
        String sentence = normalizeSentence(request.sentence());
        ensureSentenceIsUnique(wordId, sentence, null);
        ClozeExercise exercise = new ClozeExercise();
        exercise.setWordId(wordId);
        apply(exercise, request);
        return ClozeExerciseResponse.from(clozeExerciseRepository.save(exercise));
    }

    @Transactional
    public ClozeExerciseResponse update(UUID userId, UUID exerciseId, ClozeExerciseRequest request) {
        ClozeExercise exercise = require(exerciseId);
        Word word = wordService.require(exercise.getWordId());
        deckService.requireOwned(userId, word.getDeckId());
        String sentence = normalizeSentence(request.sentence());
        if (!sentence.equalsIgnoreCase(exercise.getSentence())) {
            ensureSentenceIsUnique(exercise.getWordId(), sentence, exerciseId);
        }
        apply(exercise, request);
        return ClozeExerciseResponse.from(clozeExerciseRepository.save(exercise));
    }

    @Transactional
    public void delete(UUID userId, UUID exerciseId) {
        ClozeExercise exercise = require(exerciseId);
        Word word = wordService.require(exercise.getWordId());
        deckService.requireOwned(userId, word.getDeckId());
        clozeExerciseRepository.delete(exercise);
    }

    /** Упражнение читают все, у кого есть доступ к колоде слова. */
    @Transactional(readOnly = true)
    public ClozeExercise require(UUID exerciseId) {
        return clozeExerciseRepository.findById(exerciseId)
                .orElseThrow(() -> NotFoundException.of("Упражнение cloze", exerciseId));
    }

    /** Проверяет наличие пропуска и возвращает нормализованное предложение. */
    private String normalizeSentence(String sentence) {
        String normalized = sentence == null ? "" : sentence.trim();
        if (!normalized.contains(GAP)) {
            throw new BusinessException("Предложение должно содержать пропуск ___");
        }
        return normalized;
    }

    /**
     * Запрещает два одинаковых предложения у одного слова.
     * При обновлении упражнение исключается из проверки по {@code excludedId}.
     */
    private void ensureSentenceIsUnique(UUID wordId, String sentence, UUID excludedId) {
        boolean duplicate = clozeExerciseRepository.findByWordId(wordId).stream()
                .filter(existing -> excludedId == null || !excludedId.equals(existing.getId()))
                .anyMatch(existing -> existing.getSentence().equalsIgnoreCase(sentence));
        if (duplicate) {
            throw new ConflictException("Такое упражнение cloze уже есть у этого слова");
        }
    }

    private void apply(ClozeExercise exercise, ClozeExerciseRequest request) {
        exercise.setSentence(normalizeSentence(request.sentence()));
        exercise.setAnswer(request.answer().trim());
        exercise.setHint(request.hint());
    }
}
