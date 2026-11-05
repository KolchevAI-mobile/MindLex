package ru.mindlex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Настройки приложения. Значения приходят из переменных окружения
 * (фактор III методологии 12 факторов) — см. {@code application.yml}.
 */
@ConfigurationProperties(prefix = "mindlex")
public record MindLexProperties(Jwt jwt, Cors cors, Practice practice) {

    public record Jwt(String secret, String issuer, Duration ttl) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Practice(int newWordsPerSession, int rushSeconds, int clozeSeconds) {
    }
}
