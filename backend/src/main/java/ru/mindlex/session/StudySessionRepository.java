package ru.mindlex.session;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

    Page<StudySession> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    Optional<StudySession> findFirstByUserIdAndStatusOrderByStartedAtDesc(UUID userId, ru.mindlex.domain.SessionStatus status);

    long countByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
