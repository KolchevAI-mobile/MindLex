package ru.mindlex.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Один экземпляр встроенного PostgreSQL на весь прогон тестов.
 *
 * <p>Благодаря этому интеграционные тесты работают с настоящей СУБД
 * и не требуют Docker или установленного PostgreSQL.</p>
 */
public final class EmbeddedPostgresHolder {

    private static EmbeddedPostgres instance;

    private EmbeddedPostgresHolder() {
    }

    public static synchronized EmbeddedPostgres instance() {
        if (instance == null) {
            try {
                instance = EmbeddedPostgres.builder().start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        instance.close();
                    } catch (IOException ignored) {
                        // приложение завершается — подавляем шум в логе
                    }
                }));
            } catch (IOException ex) {
                throw new UncheckedIOException("Не удалось запустить встроенный PostgreSQL", ex);
            }
        }
        return instance;
    }
}
