package ru.mindlex.deck;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record DeckResponse(UUID id,
                           UUID ownerId,
                           String name,
                           String language,
                           String description,
                           @JsonProperty("isPublic") boolean isPublic,
                           long wordCount,
                           Instant createdAt,
                           Instant updatedAt) {

    public static DeckResponse from(Deck deck, long wordCount) {
        return new DeckResponse(deck.getId(), deck.getOwnerId(), deck.getName(), deck.getLanguage(),
                deck.getDescription(), deck.isPublicDeck(), wordCount, deck.getCreatedAt(), deck.getUpdatedAt());
    }
}
