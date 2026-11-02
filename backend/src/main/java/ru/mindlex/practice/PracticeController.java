package ru.mindlex.practice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mindlex.common.PageResponse;
import ru.mindlex.security.CurrentUser;

import java.net.URI;
import java.util.UUID;

/**
 * Игровой цикл: начать сессию, получать вопросы, отвечать и завершать раунд.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Игровые сессии", description = "Механики Active Recall, Cloze, Timed Rush и Synonym Chain")
public class PracticeController {

    private final PracticeService practiceService;
    private final CurrentUser currentUser;

    public PracticeController(PracticeService practiceService, CurrentUser currentUser) {
        this.practiceService = practiceService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @Operation(summary = "Начать сессию выбранной механики")
    public ResponseEntity<SessionResponse> start(@Valid @RequestBody StartSessionRequest request) {
        SessionResponse session = practiceService.startSession(currentUser.id(), request);
        return ResponseEntity.created(URI.create("/api/v1/sessions/" + session.id())).body(session);
    }

    @GetMapping
    @Operation(summary = "История сессий")
    public PageResponse<SessionResponse> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return practiceService.listSessions(currentUser.id(), page, Math.min(size, 100));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Состояние сессии")
    public SessionResponse get(@PathVariable UUID sessionId) {
        return practiceService.getSession(currentUser.id(), sessionId);
    }

    @GetMapping("/{sessionId}/question")
    @Operation(summary = "Следующий вопрос; 204 — вопросы закончились, пора завершать сессию")
    public ResponseEntity<QuestionResponse> nextQuestion(@PathVariable UUID sessionId) {
        return practiceService.nextQuestion(currentUser.id(), sessionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    @PostMapping("/{sessionId}/answers")
    @Operation(summary = "Ответить на вопрос")
    public AnswerResponse answer(@PathVariable UUID sessionId, @Valid @RequestBody AnswerRequest request) {
        return practiceService.answer(currentUser.id(), sessionId, request);
    }

    @PostMapping("/{sessionId}/skip")
    @Operation(summary = "Пропустить вопрос")
    public AnswerResponse skip(@PathVariable UUID sessionId, @Valid @RequestBody SkipRequest request) {
        return practiceService.skip(currentUser.id(), sessionId, request.questionId());
    }

    @PostMapping("/{sessionId}/finish")
    @Operation(summary = "Завершить сессию и получить итоги")
    public SessionSummaryResponse finish(@PathVariable UUID sessionId) {
        return practiceService.finishSession(currentUser.id(), sessionId);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Удалить запись о сессии")
    public ResponseEntity<Void> delete(@PathVariable UUID sessionId) {
        practiceService.deleteSession(currentUser.id(), sessionId);
        return ResponseEntity.noContent().build();
    }
}
