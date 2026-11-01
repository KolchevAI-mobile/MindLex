package ru.mindlex.reminder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mindlex.security.CurrentUser;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reminders")
@Tag(name = "Напоминания", description = "CRUD над напоминаниями текущего пользователя")
public class StudyReminderController {

    private final StudyReminderService studyReminderService;
    private final CurrentUser currentUser;

    public StudyReminderController(StudyReminderService studyReminderService, CurrentUser currentUser) {
        this.studyReminderService = studyReminderService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Напоминания текущего пользователя")
    public List<StudyReminderResponse> list() {
        return studyReminderService.list(currentUser.id());
    }

    @GetMapping("/{reminderId}")
    @Operation(summary = "Напоминание по идентификатору")
    public StudyReminderResponse get(@PathVariable UUID reminderId) {
        return studyReminderService.get(currentUser.id(), reminderId);
    }

    @PostMapping
    @Operation(summary = "Создать напоминание")
    public ResponseEntity<StudyReminderResponse> create(@Valid @RequestBody StudyReminderRequest request) {
        StudyReminderResponse created = studyReminderService.create(currentUser.id(), request);
        return ResponseEntity.created(URI.create("/api/v1/reminders/" + created.id())).body(created);
    }

    @PutMapping("/{reminderId}")
    @Operation(summary = "Изменить напоминание")
    public StudyReminderResponse update(@PathVariable UUID reminderId,
                                        @Valid @RequestBody StudyReminderRequest request) {
        return studyReminderService.update(currentUser.id(), reminderId, request);
    }

    @DeleteMapping("/{reminderId}")
    @Operation(summary = "Удалить напоминание")
    public ResponseEntity<Void> delete(@PathVariable UUID reminderId) {
        studyReminderService.delete(currentUser.id(), reminderId);
        return ResponseEntity.noContent().build();
    }
}
