package ru.mindlex.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;

/**
 * Вспомогательный процесс для локального запуска бэкенда без Docker.
 *
 * <p>Поднимает настоящий PostgreSQL на фиксированном порту и остаётся в foreground.
 * Используется так (из каталога {@code backend}):</p>
 *
 * <pre>
 * ./mvnw -q test-compile
 * ./mvnw -q dependency:build-classpath -Dmdep.outputFile=cp.txt -Dmdep.includeScope=test
 * java -cp "target/classes;target/test-classes;$(cat cp.txt)" ru.mindlex.support.DevPostgresServer 55432
 * </pre>
 *
 * <p>После этого бэкенд запускается с переменной окружения</p>
 *
 * <pre>
 * MINDLEX_DB_URL=jdbc:postgresql://localhost:55432/postgres \
 * MINDLEX_DB_USER=postgres MINDLEX_DB_PASSWORD=postgres ./mvnw spring-boot:run
 * </pre>
 */
public final class DevPostgresServer {

    private DevPostgresServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 55432;
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().setPort(port).start();
        System.out.println("Dev PostgreSQL запущен: " + postgres.getJdbcUrl("postgres", "postgres"));
        System.out.println("Нажмите Ctrl+C для остановки.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                postgres.close();
            } catch (IOException ignored) {
                // процесс завершается
            }
        }));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
