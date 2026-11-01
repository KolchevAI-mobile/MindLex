package ru.mindlex.chain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import ru.mindlex.common.UuidEntity;

import java.util.UUID;

/**
 * Один шаг цепочки синонимов: слово-подсказка и список принимаемых синонимов.
 */
@Entity
@Table(name = "synonym_chain_steps", uniqueConstraints = @UniqueConstraint(name = "uq_chain_step", columnNames = {"chain_id", "step_number"}))
public class SynonymChainStep extends UuidEntity {

    @Column(name = "chain_id", nullable = false)
    private UUID chainId;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(name = "prompt_word", nullable = false, length = 160)
    private String promptWord;

    /** Принимаемые синонимы через запятую. */
    @Column(name = "accepted_synonyms", nullable = false)
    private String acceptedSynonyms;

    @Column(name = "hint", length = 255)
    private String hint;

    public UUID getChainId() {
        return chainId;
    }

    public void setChainId(UUID chainId) {
        this.chainId = chainId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getPromptWord() {
        return promptWord;
    }

    public void setPromptWord(String promptWord) {
        this.promptWord = promptWord;
    }

    public String getAcceptedSynonyms() {
        return acceptedSynonyms;
    }

    public void setAcceptedSynonyms(String acceptedSynonyms) {
        this.acceptedSynonyms = acceptedSynonyms;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
