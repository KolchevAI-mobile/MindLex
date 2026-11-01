package ru.mindlex.cloze;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ru.mindlex.common.TimestampedEntity;

import java.util.UUID;

/**
 * Контекстное упражнение: предложение с пропуском {@code ___} и правильным ответом.
 */
@Entity
@Table(name = "cloze_exercises")
public class ClozeExercise extends TimestampedEntity {

    @Column(name = "word_id", nullable = false)
    private UUID wordId;

    @Column(name = "sentence", nullable = false, length = 600)
    private String sentence;

    @Column(name = "answer", nullable = false, length = 160)
    private String answer;

    @Column(name = "hint", length = 255)
    private String hint;

    public UUID getWordId() {
        return wordId;
    }

    public void setWordId(UUID wordId) {
        this.wordId = wordId;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
