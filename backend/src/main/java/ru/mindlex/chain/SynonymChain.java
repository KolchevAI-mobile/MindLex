package ru.mindlex.chain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ru.mindlex.common.TimestampedEntity;

/**
 * Цепочка синонимов: набор последовательных шагов, которые пользователь проходит подряд.
 */
@Entity
@Table(name = "synonym_chains")
public class SynonymChain extends TimestampedEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "language", nullable = false, length = 8)
    private String language = "en";

    @Column(name = "category", length = 80)
    private String category;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
