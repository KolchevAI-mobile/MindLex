package ru.mindlex.reminder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudyReminderRepository extends JpaRepository<StudyReminder, UUID> {

    List<StudyReminder> findByUserIdOrderByPlannedAtAsc(UUID userId);

    Page<StudyReminder> findByUserIdOrderByPlannedAtAsc(UUID userId, Pageable pageable);

    long countByUserIdAndSentFalse(UUID userId);
}
