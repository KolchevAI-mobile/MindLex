package ru.mindlex.settings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mindlex.security.CurrentUser;

@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "Настройки", description = "Персональные настройки и серия занятий")
public class SettingsController {

    private final SettingsService settingsService;
    private final CurrentUser currentUser;

    public SettingsController(SettingsService settingsService, CurrentUser currentUser) {
        this.settingsService = settingsService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Текущие настройки")
    public SettingsResponse get() {
        return settingsService.get(currentUser.id());
    }

    @PutMapping
    @Operation(summary = "Изменить настройки")
    public SettingsResponse update(@Valid @RequestBody SettingsRequest request) {
        return settingsService.update(currentUser.id(), request);
    }
}
