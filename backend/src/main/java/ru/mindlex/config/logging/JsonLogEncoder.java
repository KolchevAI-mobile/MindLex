package ru.mindlex.config.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.encoder.EncoderBase;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Кодировщик журнала в JSON Lines: одна запись — одна строка.
 *
 * <p>Приложение пишет в stdout поток структурированных событий вместо текста, поэтому
 * сборщику не нужно разбирать строки регулярными выражениями: он переносит готовый
 * JSON в хранилище, а поля записи становятся доступны в запросах.
 *
 * <p>Набор полей:
 * <ul>
 *   <li>{@code timestamp} — момент события в UTC, RFC 3339 с миллисекундами;</li>
 *   <li>{@code level} — уровень (INFO, WARN, ERROR, DEBUG, TRACE);</li>
 *   <li>{@code message} — текст события;</li>
 *   <li>{@code service.name} — имя сервиса (источник журналов);</li>
 *   <li>{@code logger_name}, {@code thread_name}, {@code schema_version} — служебные сведения о записи;</li>
 *   <li>значения из {@link org.slf4j.MDC}, например {@code request_id}, {@code http.request.method};</li>
 *   <li>{@code error.type}, {@code error.message}, {@code error.stack_trace} — при наличии исключения.</li>
 * </ul>
 *
 * <p>Зависимостей, кроме Logback, не требует.
 */
public class JsonLogEncoder extends EncoderBase<ILoggingEvent> {

    /** Имя сервиса, попадающее в поле {@code service.name}. */
    private static final String SERVICE_NAME = "mindlex-api";

    /** Версия схемы — чтобы потребитель логов мог отличить формат при его изменении. */
    private static final String SCHEMA_VERSION = "1.0";

    /**
     * Служебные ключи MDC, которые не нужно дублировать в журнале:
     * {@code service.name} и {@code schema_version} добавляет сам кодировщик.
     */
    private static final Map<String, String> RESERVED = Map.of(
            LogFields.SERVICE_NAME, LogFields.SERVICE_NAME,
            "schema_version", "schema_version");

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    /** Буфер переиспользуется: кодировщик вызывается на каждое событие, лишние аллокации ни к чему. */
    private final StringBuilder buffer = new StringBuilder(512);

    @Override
    public byte[] encode(ILoggingEvent event) {
        buffer.setLength(0);
        buffer.append('{');

        appendField(buffer, LogFields.TIMESTAMP, TIMESTAMP.format(Instant.ofEpochMilli(event.getTimeStamp())));
        appendField(buffer, LogFields.LEVEL, event.getLevel().toString());
        appendField(buffer, LogFields.MESSAGE, event.getFormattedMessage());
        appendField(buffer, LogFields.SERVICE_NAME, SERVICE_NAME);
        appendField(buffer, "schema_version", SCHEMA_VERSION);
        appendField(buffer, "logger_name", event.getLoggerName());
        appendField(buffer, "thread_name", event.getThreadName());

        // Контекст запроса и прочие значения, положенные в MDC фильтрами и кодом приложения.
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null) {
            for (Map.Entry<String, String> entry : new LinkedHashMap<>(mdc).entrySet()) {
                if (entry.getValue() == null || RESERVED.containsKey(entry.getKey())) {
                    continue;
                }
                appendField(buffer, entry.getKey(), entry.getValue());
            }
        }

        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable != null) {
            appendField(buffer, LogFields.ERROR_TYPE, throwable.getClassName());
            appendField(buffer, "error.message", throwable.getMessage());
            appendField(buffer, LogFields.ERROR_STACK_TRACE, stackTrace(throwable));
        }

        buffer.append("}\n");
        return buffer.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] headerBytes() {
        // JSON Lines: заголовок не нужен, но ConsoleAppender вызывает метод всегда.
        return new byte[0];
    }

    @Override
    public byte[] footerBytes() {
        return new byte[0];
    }

    /** Стек исключения одной строкой: переводы строк экранируются, поэтому запись остаётся одной строкой. */
    private static String stackTrace(IThrowableProxy throwable) {
        StringBuilder sb = new StringBuilder(256);
        appendThrowable(sb, throwable);
        return sb.toString();
    }

    private static void appendThrowable(StringBuilder sb, IThrowableProxy throwable) {
        if (sb.length() > 0) {
            sb.append("\nCaused by: ");
        }
        sb.append(throwable.getClassName());
        if (throwable.getMessage() != null) {
            sb.append(": ").append(throwable.getMessage());
        }
        StackTraceElementProxy[] frames = throwable.getStackTraceElementProxyArray();
        if (frames != null) {
            for (StackTraceElementProxy frame : frames) {
                sb.append("\n\tat ").append(frame.getSTEAsString());
            }
        }
        // Цепочка причин и подавленные исключения — рекурсивно.
        IThrowableProxy cause = throwable.getCause();
        if (cause != null) {
            appendThrowable(sb, cause);
        }
        IThrowableProxy[] suppressed = throwable.getSuppressed();
        if (suppressed != null) {
            for (IThrowableProxy s : suppressed) {
                sb.append("\nSuppressed: ");
                appendThrowable(sb, s);
            }
        }
    }

    private static void appendField(StringBuilder sb, String name, String value) {
        if (value == null) {
            return;
        }
        if (sb.length() > 1) {
            sb.append(',');
        }
        appendString(sb, name);
        sb.append(':');
        appendString(sb, value);
    }

    private static void appendString(StringBuilder sb, String value) {
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    // Управляющие символы обязаны быть экранированы, иначе JSON станет невалидным.
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }
}
