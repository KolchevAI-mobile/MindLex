package ru.mindlex.reminder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.common.NotFoundException;
import ru.mindlex.common.PageResponse;

import java.util.List;
import java.util.UUID;

/**
 * CRUD над напоминаниями текущего пользователя.
 * Каждый метод работает только с напоминаниями самого пользователя.
 */
@Service
public class StudyReminderService {

    private final StudyReminderRepository studyReminderRepository;

    public StudyReminderService(StudyReminderRepository studyReminderRepository) {
        this.studyReminderRepository = studyReminderRepository;
    }

    /** Полный список напоминаний пользователя в порядке времени занятия. */
    @Transactional(readOnly = true)
    public List<StudyReminderResponse> list(UUID userId) {
        return studyReminderRepository.findByUserIdOrderByPlannedAtAsc(userId).stream()
                .map(StudyReminderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<StudyReminderResponse> list(UUID userId, int page, int size) {
        Page<StudyReminder> reminders =
                studyReminderRepository.findByUserIdOrderByPlannedAtAsc(userId, PageRequest.of(page, size));
        return PageResponse.from(reminders, StudyReminderResponse::from);
    }

    @Transactional(readOnly = true)
    public StudyReminderResponse get(UUID userId, UUID reminderId) {
        return StudyReminderResponse.from(requireOwned(userId, reminderId));
    }

    @Transactional
    public StudyReminderResponse create(UUID userId, StudyReminderRequest request) {
        StudyReminder reminder = new StudyReminder();
        reminder.setUserId(userId);
        apply(reminder, request);
        return StudyReminderResponse.from(studyReminderRepository.save(reminder));
    }

    @Transactional
    public StudyReminderResponse update(UUID userId, UUID reminderId, StudyReminderRequest request) {
        StudyReminder reminder = requireOwned(userId, reminderId);
        apply(reminder, request);
        return StudyReminderResponse.from(studyReminderRepository.save(reminder));
    }

    @Transactional
    public void delete(UUID userId, UUID reminderId) {
        StudyReminder reminder = requireOwned(userId, reminderId);
        studyReminderRepository.delete(reminder);
    }

    @Transactional(readOnly = true)
    public StudyReminder require(UUID reminderId) {
        return studyReminderRepository.findById(reminderId)
                .orElseThrow(() -> NotFoundException.of("Напоминание", reminderId));
    }

    /** Чужое напоминание недоступно даже на чтение: это личные данные пользователя. */
    @Transactional(readOnly = true)
    public StudyReminder requireOwned(UUID userId, UUID reminderId) {
        StudyReminder reminder = require(reminderId);
        if (!reminder.getUserId().equals(userId)) {
            throw new AccessDeniedException("Напоминание принадлежит другому пользователю");
        }
        return reminder;
    }

    private void apply(StudyReminder reminder, StudyReminderRequest request) {
        reminder.setPlannedAt(request.plannedAt());
        reminder.setTitle(request.title().trim());
        reminder.setBody(request.body());
        if (request.sent() != null) {
            reminder.setSent(request.sent());
        }
    }
}
