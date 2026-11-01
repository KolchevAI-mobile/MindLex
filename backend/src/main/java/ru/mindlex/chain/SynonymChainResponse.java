package ru.mindlex.chain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Представление цепочки синонимов вместе с её шагами.
 */
public record SynonymChainResponse(UUID id,
                                   String title,
                                   String language,
                                   String category,
                                   List<SynonymChainStepResponse> steps,
                                   Instant createdAt,
                                   Instant updatedAt) {

    public static SynonymChainResponse from(SynonymChain chain, List<SynonymChainStep> steps) {
        return new SynonymChainResponse(chain.getId(), chain.getTitle(), chain.getLanguage(), chain.getCategory(),
                steps.stream().map(SynonymChainStepResponse::from).toList(),
                chain.getCreatedAt(), chain.getUpdatedAt());
    }
}
