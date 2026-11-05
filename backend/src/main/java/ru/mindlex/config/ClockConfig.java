package ru.mindlex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Единый источник времени. Вынесен в бин, чтобы бизнес-логику
 * (интервальное повторение, стрики) можно было проверять в тестах
 * с фиксированными часами.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
