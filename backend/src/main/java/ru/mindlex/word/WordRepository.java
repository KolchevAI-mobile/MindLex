package ru.mindlex.word;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WordRepository extends JpaRepository<Word, UUID> {

    List<Word> findByDeckIdOrderByTermAsc(UUID deckId);

    Page<Word> findByDeckId(UUID deckId, Pageable pageable);

    long countByDeckId(UUID deckId);

    boolean existsByDeckIdAndTermIgnoreCase(UUID deckId, String term);

    List<Word> findByDeckIdIn(Collection<UUID> deckIds);

    @Query("select w from Word w where w.id in :ids")
    List<Word> findAllByIds(@Param("ids") Collection<UUID> ids);

    /**
     * Случайное слово колоды. {@code ORDER BY random()} выполняет PostgreSQL,
     * поэтому в приложение не выгружается весь набор слов.
     */
    @Query(value = "select * from words where deck_id = :deckId order by random() limit :limit", nativeQuery = true)
    List<Word> findRandomByDeck(@Param("deckId") UUID deckId, @Param("limit") int limit);

    @Query(value = "select * from words where deck_id = :deckId and id not in (:excludedIds) order by random() limit :limit",
            nativeQuery = true)
    List<Word> findRandomByDeckExcluding(@Param("deckId") UUID deckId,
                                         @Param("excludedIds") Collection<UUID> excludedIds,
                                         @Param("limit") int limit);

    /** Слова колоды, которые пользователь ещё ни разу не проходил. */
    @Query("""
            select w from Word w
            where w.deckId = :deckId
              and not exists (select 1 from WordProgress p where p.wordId = w.id and p.userId = :userId)
            order by w.term asc
            """)
    List<Word> findNotStarted(@Param("deckId") UUID deckId, @Param("userId") UUID userId, Pageable pageable);

    /** Слова колоды, у которых уже наступил срок повторения. */
    @Query("""
            select w from Word w
            join WordProgress p on p.wordId = w.id
            where p.userId = :userId and w.deckId = :deckId
              and p.nextReviewAt <= :now and p.status in :statuses
            order by p.nextReviewAt asc
            """)
    List<Word> findDueWords(@Param("userId") UUID userId,
                            @Param("deckId") UUID deckId,
                            @Param("now") java.time.Instant now,
                            @Param("statuses") Collection<ru.mindlex.domain.WordStatus> statuses,
                            Pageable pageable);

    /** Слова колоды, которые уже находятся в процессе изучения. */
    @Query("""
            select w from Word w
            join WordProgress p on p.wordId = w.id
            where p.userId = :userId and w.deckId = :deckId and p.status in :statuses
            order by p.nextReviewAt asc
            """)
    List<Word> findLearningWords(@Param("userId") UUID userId,
                                 @Param("deckId") UUID deckId,
                                 @Param("statuses") Collection<ru.mindlex.domain.WordStatus> statuses,
                                 Pageable pageable);
}
