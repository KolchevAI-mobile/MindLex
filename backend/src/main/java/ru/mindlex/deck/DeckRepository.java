package ru.mindlex.deck;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, UUID> {

    /** Колоды, доступные пользователю: его собственные и публичные системные. */
    @Query("select d from Deck d where d.publicDeck = true or d.ownerId = :userId order by d.publicDeck desc, d.name asc")
    List<Deck> findAvailable(@Param("userId") UUID userId);

    @Query("select d from Deck d where d.publicDeck = true or d.ownerId = :userId")
    Page<Deck> findAvailable(@Param("userId") UUID userId, Pageable pageable);

    List<Deck> findByOwnerIdOrderByNameAsc(UUID ownerId);

    List<Deck> findByPublicDeckTrueOrderByNameAsc();
}
