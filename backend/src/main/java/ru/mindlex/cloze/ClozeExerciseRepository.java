package ru.mindlex.cloze;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ClozeExerciseRepository extends JpaRepository<ClozeExercise, UUID> {

    List<ClozeExercise> findByWordId(UUID wordId);

    Page<ClozeExercise> findByWordId(UUID wordId, Pageable pageable);

    long countByWordId(UUID wordId);

    @Query(value = """
            select count(*) from cloze_exercises c
            join words w on w.id = c.word_id
            where w.deck_id = :deckId
            """, nativeQuery = true)
    long countByDeckId(@Param("deckId") UUID deckId);

    @Query(value = """
            select c.* from cloze_exercises c
            join words w on w.id = c.word_id
            where w.deck_id = :deckId
            order by random() limit :limit
            """, nativeQuery = true)
    List<ClozeExercise> findRandomByDeck(@Param("deckId") UUID deckId, @Param("limit") int limit);

    @Query(value = """
            select c.* from cloze_exercises c
            join words w on w.id = c.word_id
            where w.deck_id = :deckId and c.id not in (:excludedIds)
            order by random() limit :limit
            """, nativeQuery = true)
    List<ClozeExercise> findRandomByDeckExcluding(@Param("deckId") UUID deckId,
                                                  @Param("excludedIds") Collection<UUID> excludedIds,
                                                  @Param("limit") int limit);
}
