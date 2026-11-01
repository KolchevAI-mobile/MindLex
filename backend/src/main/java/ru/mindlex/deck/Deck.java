package ru.mindlex.deck;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ru.mindlex.common.TimestampedEntity;

import java.util.UUID;

/**
 * Набор слов (колода). Публичные колоды с {@code ownerId == null} — системный контент,
 * личные колоды создают сами пользователи (CRUD-сущность).
 */
@Entity
@Table(name = "decks")
public class Deck extends TimestampedEntity {

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "language", nullable = false, length = 8)
    private String language = "en";

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean publicDeck;

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublicDeck() {
        return publicDeck;
    }

    public void setPublicDeck(boolean publicDeck) {
        this.publicDeck = publicDeck;
    }
}
