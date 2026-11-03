package ru.mindlex.progress;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mindlex.domain.WordStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WordProgressRepository extends JpaRepository<WordProgress, UUID> {

    Optional<WordProgress> findByUserIdAndWordId(UUID userId, UUID wordId);

    Page<WordProgress> findByUserId(UUID userId, Pageable pageable);

    List<WordProgress> findByUserId(UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(UUID userId, WordStatus status);

    long countByUserIdAndStatusIn(UUID userId, Collection<WordStatus> statuses);

    void deleteByUserIdAndWordId(UUID userId, UUID wordId);

    void deleteByUserId(UUID userId);

    /** Слова, срок повторения которых уже наступил. */
    @Query("""
            select p from WordProgress p
            where p.userId = :userId and p.nextReviewAt <= :now and p.status in :statuses
            order by p.nextReviewAt asc
            """)
    List<WordProgress> findDue(@Param("userId") UUID userId,
                               @Param("now") Instant now,
                               @Param("statuses") Collection<WordStatus> statuses,
                               Pageable pageable);

    @Query("""
            select p from WordProgress p
            where p.userId = :userId and p.status in :statuses
            order by p.nextReviewAt asc
            """)
    List<WordProgress> findByStatuses(@Param("userId") UUID userId,
                                      @Param("statuses") Collection<WordStatus> statuses,
                                      Pageable pageable);

    long countByUserIdAndNextReviewAtLessThanEqualAndStatusIn(UUID userId, Instant now, Collection<WordStatus> statuses);
}
