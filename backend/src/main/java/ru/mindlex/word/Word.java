package ru.mindlex.word;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ru.mindlex.common.TimestampedEntity;

import java.util.UUID;

/**
 * Слово изучаемого языка вместе с переводом. Основная CRUD-сущность приложения.
 */
@Entity
@Table(name = "words")
public class Word extends TimestampedEntity {

    @Column(name = "deck_id", nullable = false)
    private UUID deckId;

    /** Слово на изучаемом языке. */
    @Column(name = "term", nullable = false, length = 160)
    private String term;

    /** Перевод на родной язык. */
    @Column(name = "translation", nullable = false, length = 255)
    private String translation;

    /** Транскрипция или произношение. */
    @Column(name = "transcription", length = 160)
    private String transcription;

    @Column(name = "part_of_speech", length = 32)
    private String partOfSpeech;

    @Column(name = "example", length = 500)
    private String example;

    /** Дополнительные варианты перевода через запятую. */
    @Column(name = "alternatives")
    private String alternatives;

    public UUID getDeckId() {
        return deckId;
    }

    public void setDeckId(UUID deckId) {
        this.deckId = deckId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(String alternatives) {
        this.alternatives = alternatives;
    }
}
