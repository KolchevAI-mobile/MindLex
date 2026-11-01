package ru.mindlex.chain;

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

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/synonym-chains")
@Tag(name = "Цепочки синонимов", description = "CRUD над общим справочником цепочек синонимов")
public class SynonymChainController {

    private final SynonymChainService synonymChainService;

    public SynonymChainController(SynonymChainService synonymChainService) {
        this.synonymChainService = synonymChainService;
    }

    @GetMapping
    @Operation(summary = "Список цепочек синонимов")
    public List<SynonymChainResponse> list(@RequestParam(required = false) String language) {
        return synonymChainService.list(language);
    }

    @GetMapping("/{chainId}")
    @Operation(summary = "Цепочка синонимов по идентификатору")
    public SynonymChainResponse get(@PathVariable UUID chainId) {
        return synonymChainService.get(chainId);
    }

    @PostMapping
    @Operation(summary = "Создать цепочку синонимов")
    public ResponseEntity<SynonymChainResponse> create(@Valid @RequestBody SynonymChainRequest request) {
        SynonymChainResponse created = synonymChainService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/synonym-chains/" + created.id())).body(created);
    }

    @PutMapping("/{chainId}")
    @Operation(summary = "Изменить цепочку синонимов вместе со шагами")
    public SynonymChainResponse update(@PathVariable UUID chainId,
                                       @Valid @RequestBody SynonymChainRequest request) {
        return synonymChainService.update(chainId, request);
    }

    @DeleteMapping("/{chainId}")
    @Operation(summary = "Удалить цепочку синонимов")
    public ResponseEntity<Void> delete(@PathVariable UUID chainId) {
        synonymChainService.delete(chainId);
        return ResponseEntity.noContent().build();
    }
}
