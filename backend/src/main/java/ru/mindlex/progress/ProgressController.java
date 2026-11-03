package ru.mindlex.progress;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mindlex.common.PageResponse;
import ru.mindlex.security.CurrentUser;

import java.util.UUID;

/**
 * Просмотр и сброс прогресса изучения слов.
 */
@RestController
@RequestMapping("/api/v1/progress")
@Tag(name = "Прогресс", description = "Состояние интервального повторения по словам")
public class ProgressController {

    private final ProgressService progressService;
    private final CurrentUser currentUser;

    public ProgressController(ProgressService progressService, CurrentUser currentUser) {
        this.progressService = progressService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Прогресс по всем словам пользователя")
    public PageResponse<WordProgressResponse> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "50") int size) {
        return progressService.list(currentUser.id(), page, Math.min(size, 200));
    }

    @GetMapping("/{wordId}")
    @Operation(summary = "Прогресс по конкретному слову")
    public WordProgressResponse get(@PathVariable UUID wordId) {
        return progressService.get(currentUser.id(), wordId);
    }

    @DeleteMapping("/{wordId}")
    @Operation(summary = "Сбросить прогресс по слову")
    public ResponseEntity<Void> reset(@PathVariable UUID wordId) {
        progressService.reset(currentUser.id(), wordId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Сбросить весь прогресс")
    public ResponseEntity<Void> resetAll() {
        progressService.resetAll(currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
