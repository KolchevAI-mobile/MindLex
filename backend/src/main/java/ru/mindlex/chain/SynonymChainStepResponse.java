package ru.mindlex.chain;

import ru.mindlex.common.TextUtils;

import java.util.List;
import java.util.UUID;

/**
 * Представление шага цепочки синонимов в API.
 */
public record SynonymChainStepResponse(UUID id,
                                       int stepNumber,
                                       String promptWord,
                                       List<String> acceptedSynonyms,
                                       String hint) {

    public static SynonymChainStepResponse from(SynonymChainStep step) {
        return new SynonymChainStepResponse(step.getId(), step.getStepNumber(), step.getPromptWord(),
                TextUtils.splitCsv(step.getAcceptedSynonyms()), step.getHint());
    }
}
