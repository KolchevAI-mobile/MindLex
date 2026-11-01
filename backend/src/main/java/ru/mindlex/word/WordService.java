package ru.mindlex.word;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.common.ConflictException;
import ru.mindlex.common.NotFoundException;
import ru.mindlex.common.PageResponse;
import ru.mindlex.common.TextUtils;
import ru.mindlex.deck.DeckService;

import java.util.UUID;

@Service
public class WordService {

    private final WordRepository wordRepository;
    private final DeckService deckService;

    public WordService(WordRepository wordRepository, DeckService deckService) {
        this.wordRepository = wordRepository;
        this.deckService = deckService;
    }

    @Transactional(readOnly = true)
    public PageResponse<WordResponse> list(UUID deckId, int page, int size) {
        deckService.require(deckId);
        Page<Word> words = wordRepository.findByDeckId(deckId, PageRequest.of(page, size));
        return PageResponse.from(words, WordResponse::from);
    }

    @Transactional(readOnly = true)
    public WordResponse get(UUID wordId) {
        return WordResponse.from(require(wordId));
    }

    @Transactional
    public WordResponse create(UUID userId, UUID deckId, WordRequest request) {
        deckService.requireOwned(userId, deckId);
        String term = request.term().trim();
        if (wordRepository.existsByDeckIdAndTermIgnoreCase(deckId, term)) {
            throw new ConflictException("Такое слово уже есть в колоде");
        }
        Word word = new Word();
        word.setDeckId(deckId);
        apply(word, request);
        return WordResponse.from(wordRepository.save(word));
    }

    @Transactional
    public WordResponse update(UUID userId, UUID wordId, WordRequest request) {
        Word word = require(wordId);
        deckService.requireOwned(userId, word.getDeckId());
        apply(word, request);
        return WordResponse.from(wordRepository.save(word));
    }

    @Transactional
    public void delete(UUID userId, UUID wordId) {
        Word word = require(wordId);
        deckService.requireOwned(userId, word.getDeckId());
        wordRepository.delete(word);
    }

    @Transactional(readOnly = true)
    public Word require(UUID wordId) {
        return wordRepository.findById(wordId)
                .orElseThrow(() -> NotFoundException.of("Слово", wordId));
    }

    private void apply(Word word, WordRequest request) {
        word.setTerm(request.term().trim());
        word.setTranslation(request.translation().trim());
        word.setTranscription(request.transcription());
        word.setPartOfSpeech(request.partOfSpeech());
        word.setExample(request.example());
        word.setAlternatives(TextUtils.joinCsv(request.alternatives()));
    }
}
