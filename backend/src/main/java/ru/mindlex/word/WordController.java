package ru.mindlex.word;

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
@Tag(name = "Слова", description = "CRUD над словами внутри колоды")
public class WordController {

    private final WordService wordService;
    private final CurrentUser currentUser;

    public WordController(WordService wordService, CurrentUser currentUser) {
        this.wordService = wordService;
        this.currentUser = currentUser;
    }

    @GetMapping("/decks/{deckId}/words")
    @Operation(summary = "Слова колоды")
    public PageResponse<WordResponse> list(@PathVariable UUID deckId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        return wordService.list(deckId, page, Math.min(size, 200));
    }

    @GetMapping("/words/{wordId}")
    @Operation(summary = "Слово по идентификатору")
    public WordResponse get(@PathVariable UUID wordId) {
        return wordService.get(wordId);
    }

    @PostMapping("/decks/{deckId}/words")
    @Operation(summary = "Добавить слово в колоду")
    public ResponseEntity<WordResponse> create(@PathVariable UUID deckId, @Valid @RequestBody WordRequest request) {
        WordResponse created = wordService.create(currentUser.id(), deckId, request);
        return ResponseEntity.created(URI.create("/api/v1/words/" + created.id())).body(created);
    }

    @PutMapping("/words/{wordId}")
    @Operation(summary = "Изменить слово")
    public WordResponse update(@PathVariable UUID wordId, @Valid @RequestBody WordRequest request) {
        return wordService.update(currentUser.id(), wordId, request);
    }

    @DeleteMapping("/words/{wordId}")
    @Operation(summary = "Удалить слово")
    public ResponseEntity<Void> delete(@PathVariable UUID wordId) {
        wordService.delete(currentUser.id(), wordId);
        return ResponseEntity.noContent().build();
    }
}
