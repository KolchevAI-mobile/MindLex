package ru.mindlex.deck;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.common.NotFoundException;
import ru.mindlex.common.PageResponse;
import ru.mindlex.word.WordRepository;

import java.util.Locale;
import java.util.UUID;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final WordRepository wordRepository;

    public DeckService(DeckRepository deckRepository, WordRepository wordRepository) {
        this.deckRepository = deckRepository;
        this.wordRepository = wordRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> list(UUID userId, int page, int size) {
        Page<Deck> decks = deckRepository.findAvailable(userId, PageRequest.of(page, size));
        return PageResponse.from(decks, deck -> DeckResponse.from(deck, wordRepository.countByDeckId(deck.getId())));
    }

    @Transactional(readOnly = true)
    public DeckResponse get(UUID deckId) {
        Deck deck = require(deckId);
        return DeckResponse.from(deck, wordRepository.countByDeckId(deckId));
    }

    @Transactional
    public DeckResponse create(UUID userId, DeckRequest request) {
        Deck deck = new Deck();
        deck.setOwnerId(userId);
        apply(deck, request);
        deck.setPublicDeck(false);
        Deck saved = deckRepository.save(deck);
        return DeckResponse.from(saved, 0);
    }

    @Transactional
    public DeckResponse update(UUID userId, UUID deckId, DeckRequest request) {
        Deck deck = requireOwned(userId, deckId);
        apply(deck, request);
        Deck saved = deckRepository.save(deck);
        return DeckResponse.from(saved, wordRepository.countByDeckId(deckId));
    }

    @Transactional
    public void delete(UUID userId, UUID deckId) {
        Deck deck = requireOwned(userId, deckId);
        deckRepository.delete(deck);
    }

    /** Колода доступна на чтение всем: публичные системные и собственные. */
    @Transactional(readOnly = true)
    public Deck require(UUID deckId) {
        return deckRepository.findById(deckId)
                .orElseThrow(() -> NotFoundException.of("Колода", deckId));
    }

    /** Изменять колоду может только её владелец. */
    @Transactional(readOnly = true)
    public Deck requireOwned(UUID userId, UUID deckId) {
        Deck deck = require(deckId);
        if (deck.getOwnerId() == null || !deck.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Колода принадлежит другому пользователю");
        }
        return deck;
    }

    private void apply(Deck deck, DeckRequest request) {
        deck.setName(request.name().trim());
        deck.setLanguage(request.language() == null || request.language().isBlank()
                ? "en"
                : request.language().trim().toLowerCase(Locale.ROOT));
        deck.setDescription(request.description());
        if (request.isPublic() != null) {
            deck.setPublicDeck(request.isPublic());
        }
    }
}
