package ru.mindlex.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.mindlex.auth.AuthService;
import ru.mindlex.auth.RegisterRequest;
import ru.mindlex.deck.Deck;
import ru.mindlex.deck.DeckRepository;
import ru.mindlex.settings.SettingsRequest;
import ru.mindlex.settings.SettingsService;
import ru.mindlex.user.UserRepository;

import java.util.List;

/**
 * Административный процесс (фактор XII): создаёт демонстрационного пользователя
 * и подключает его к системной колоде. Работает один раз при старте приложения
 * и отключается переменной окружения {@code MINDLEX_DEMO_USER_ENABLED=false}.
 */
@Component
@ConditionalOnProperty(name = "mindlex.demo-user.enabled", havingValue = "true", matchIfMissing = true)
public class DemoUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserInitializer.class);

    public static final String DEMO_EMAIL = "demo@mindlex.local";
    public static final String DEMO_PASSWORD = "demo1234";

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final AuthService authService;
    private final SettingsService settingsService;

    public DemoUserInitializer(UserRepository userRepository,
                               DeckRepository deckRepository,
                               AuthService authService,
                               SettingsService settingsService) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.authService = authService;
        this.settingsService = settingsService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase(DEMO_EMAIL)) {
            return;
        }

        var auth = authService.register(new RegisterRequest(DEMO_EMAIL, DEMO_PASSWORD, "Демо-пользователь", "en"));
        settingsService.update(auth.user().id(), new SettingsRequest(15, "en", publicDeckId(), null, null, null,
                true, "Europe/Moscow"));
        // Пароль в журнал не попадает: демонстрационный он или нет, учётные данные
        // в логах — это утечка: журналы уезжают в хранилище, читаются широким кругом
        // и переживают сам стенд. Тот, кому пароль нужен, берёт его из README.
        log.info("Создан демонстрационный пользователь {}", DEMO_EMAIL);
    }

    private java.util.UUID publicDeckId() {
        List<Deck> publicDecks = deckRepository.findByPublicDeckTrueOrderByNameAsc();
        return publicDecks.isEmpty() ? null : publicDecks.get(0).getId();
    }
}
