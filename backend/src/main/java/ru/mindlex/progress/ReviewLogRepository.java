package ru.mindlex.progress;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mindlex.domain.Mechanic;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, UUID> {

    Page<ReviewLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndCreatedAtGreaterThanEqual(UUID userId, Instant from);

    long countByUserIdAndCreatedAtGreaterThanEqualAndCorrectTrue(UUID userId, Instant from);

    void deleteByUserId(UUID userId);

    /** Идентификаторы вопросов, уже заданных в рамках сессии. */
    @Query("select l.questionRef from ReviewLog l where l.sessionId = :sessionId and l.questionRef is not null")
    List<UUID> findQuestionRefs(@Param("sessionId") UUID sessionId);

    Optional<ReviewLog> findFirstBySessionIdOrderByCreatedAtDesc(UUID sessionId);

    Optional<ReviewLog> findFirstBySessionIdAndMechanicOrderByCreatedAtDesc(UUID sessionId, Mechanic mechanic);
}
