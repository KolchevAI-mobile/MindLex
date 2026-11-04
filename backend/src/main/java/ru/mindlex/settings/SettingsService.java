package ru.mindlex.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.common.TimeZones;
import ru.mindlex.deck.DeckService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * Настройки пользователя и правила геймификации, связанные со временем.
 */
@Service
public class SettingsService {

    private final UserSettingsRepository settingsRepository;
    private final DeckService deckService;
    private final Clock clock;

    public SettingsService(UserSettingsRepository settingsRepository, DeckService deckService, Clock clock) {
        this.settingsRepository = settingsRepository;
        this.deckService = deckService;
        this.clock = clock;
    }

    /** Возвращает настройки, создавая их при первом обращении. */
    @Transactional
    public UserSettings require(UUID userId) {
        return settingsRepository.findById(userId)
                .orElseGet(() -> settingsRepository.save(UserSettings.defaultsFor(userId)));
    }

    @Transactional
    public SettingsResponse get(UUID userId) {
        UserSettings settings = require(userId);
        return SettingsResponse.from(settings, visibleStreak(settings));
    }

    @Transactional
    public SettingsResponse update(UUID userId, SettingsRequest request) {
        UserSettings settings = require(userId);
        if (request.dailyGoal() != null) {
            settings.setDailyGoal(request.dailyGoal());
        }
        if (request.language() != null && !request.language().isBlank()) {
            settings.setLanguage(request.language().trim().toLowerCase(Locale.ROOT));
        }
        if (request.selectedDeckId() != null) {
            deckService.require(request.selectedDeckId());
            settings.setSelectedDeckId(request.selectedDeckId());
        }
        if (request.preferredTime() != null) {
            settings.setPreferredTime(request.preferredTime());
        }
        if (request.rushSeconds() != null) {
            settings.setRushSeconds(request.rushSeconds());
        }
        if (request.clozeSeconds() != null) {
            settings.setClozeSeconds(request.clozeSeconds());
        }
        if (request.notificationsEnabled() != null) {
            settings.setNotificationsEnabled(request.notificationsEnabled());
        }
        if (request.timezone() != null && !request.timezone().isBlank()) {
            settings.setTimezone(request.timezone().trim());
        }
        settingsRepository.save(settings);
        return SettingsResponse.from(settings, visibleStreak(settings));
    }

    @Transactional
    public void resetStreak(UUID userId) {
        UserSettings settings = require(userId);
        settings.setCurrentStreak(0);
        settings.setLastStudyDate(null);
        settingsRepository.save(settings);
    }

    /** Серия показывается, только если занимались сегодня или вчера. */
    @Transactional(readOnly = true)
    public int visibleStreak(UserSettings settings) {
        LocalDate last = settings.getLastStudyDate();
        if (last == null) {
            return 0;
        }
        LocalDate today = LocalDate.now(clock.withZone(TimeZones.of(settings.getTimezone())));
        return (last.equals(today) || last.equals(today.minusDays(1))) ? settings.getCurrentStreak() : 0;
    }
}
