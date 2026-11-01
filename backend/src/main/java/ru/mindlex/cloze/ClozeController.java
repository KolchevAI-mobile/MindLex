package ru.mindlex.cloze;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mindlex.common.PageResponse;
import ru.mindlex.security.CurrentUser;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Cloze-упражнения", description = "CRUD над контекстными упражнениями слова")
public class ClozeController {

    private final ClozeService clozeService;
    private final CurrentUser currentUser;

    public ClozeController(ClozeService clozeService, CurrentUser currentUser) {
        this.clozeService = clozeService;
        this.currentUser = currentUser;
    }

    @GetMapping("/words/{wordId}/cloze-exercises")
    @Operation(summary = "Упражнения cloze слова")
    public PageResponse<ClozeExerciseResponse> list(@PathVariable UUID wordId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "50") int size) {
        return clozeService.list(wordId, page, Math.min(size, 200));
    }

    @GetMapping("/cloze-exercises/{exerciseId}")
    @Operation(summary = "Упражнение cloze по идентификатору")
    public ClozeExerciseResponse get(@PathVariable UUID exerciseId) {
        return clozeService.get(exerciseId);
    }

    @PostMapping("/words/{wordId}/cloze-exercises")
    @Operation(summary = "Добавить упражнение cloze слову")
    public ResponseEntity<ClozeExerciseResponse> create(@PathVariable UUID wordId,
                                                        @Valid @RequestBody ClozeExerciseRequest request) {
        ClozeExerciseResponse created = clozeService.create(currentUser.id(), wordId, request);
        return ResponseEntity.created(URI.create("/api/v1/cloze-exercises/" + created.id())).body(created);
    }

    @PutMapping("/cloze-exercises/{exerciseId}")
    @Operation(summary = "Изменить упражнение cloze")
    public ClozeExerciseResponse update(@PathVariable UUID exerciseId,
                                        @Valid @RequestBody ClozeExerciseRequest request) {
        return clozeService.update(currentUser.id(), exerciseId, request);
    }

    @DeleteMapping("/cloze-exercises/{exerciseId}")
    @Operation(summary = "Удалить упражнение cloze")
    public ResponseEntity<Void> delete(@PathVariable UUID exerciseId) {
        clozeService.delete(currentUser.id(), exerciseId);
        return ResponseEntity.noContent().build();
    }
}
