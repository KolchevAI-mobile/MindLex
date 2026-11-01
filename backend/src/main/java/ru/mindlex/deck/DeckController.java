package ru.mindlex.deck;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/decks")
@Tag(name = "Колоды", description = "CRUD над наборами слов")
public class DeckController {

    private final DeckService deckService;
    private final CurrentUser currentUser;

    public DeckController(DeckService deckService, CurrentUser currentUser) {
        this.deckService = deckService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Список доступных колод")
    public PageResponse<DeckResponse> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return deckService.list(currentUser.id(), page, Math.min(size, 100));
    }

    @GetMapping("/{deckId}")
    @Operation(summary = "Колода по идентификатору")
    public DeckResponse get(@PathVariable UUID deckId) {
        return deckService.get(deckId);
    }

    @PostMapping
    @Operation(summary = "Создать колоду")
    public ResponseEntity<DeckResponse> create(@Valid @RequestBody DeckRequest request) {
        DeckResponse created = deckService.create(currentUser.id(), request);
        return ResponseEntity.created(URI.create("/api/v1/decks/" + created.id())).body(created);
    }

    @PutMapping("/{deckId}")
    @Operation(summary = "Изменить колоду")
    public DeckResponse update(@PathVariable UUID deckId, @Valid @RequestBody DeckRequest request) {
        return deckService.update(currentUser.id(), deckId, request);
    }

    @DeleteMapping("/{deckId}")
    @Operation(summary = "Удалить колоду вместе со словами")
    public ResponseEntity<Void> delete(@PathVariable UUID deckId) {
        deckService.delete(currentUser.id(), deckId);
        return ResponseEntity.noContent().build();
    }
}
