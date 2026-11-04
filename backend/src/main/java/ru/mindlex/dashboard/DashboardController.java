package ru.mindlex.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mindlex.security.CurrentUser;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Главный экран", description = "Сводная статистика пользователя")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    public DashboardController(DashboardService dashboardService, CurrentUser currentUser) {
        this.dashboardService = dashboardService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Сводка: серия, цель на день, статистика слов, рекорды")
    public DashboardResponse get() {
        return dashboardService.build(currentUser.id());
    }
}
