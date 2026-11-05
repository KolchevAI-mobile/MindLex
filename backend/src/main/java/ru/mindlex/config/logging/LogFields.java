package ru.mindlex.config.logging;

/**
 * Имена полей структурированного журнала.
 *
 * <p>Одни и те же ключи использует MDC (источник значений) и {@link JsonLogEncoder}
 * (он их сериализует), поэтому они собраны в одном месте: переименование поля не должно
 * расходиться между установкой значения и его записью в журнал.
 */
public final class LogFields {

    /** Момент события, RFC 3339 в UTC. Проставляется кодировщиком. */
    public static final String TIMESTAMP = "timestamp";

    /** Уровень события: INFO, WARN, ERROR, DEBUG, TRACE. Проставляется кодировщиком. */
    public static final String LEVEL = "level";

    /** Текст события. Проставляется кодировщиком. */
    public static final String MESSAGE = "message";

    /** Имя сервиса — источник логов. Проставляется кодировщиком. */
    public static final String SERVICE_NAME = "service.name";

    /** Сквозной идентификатор HTTP-запроса; приходит в заголовке X-Request-Id или генерируется. */
    public static final String REQUEST_ID = "request_id";

    /** HTTP-метод запроса. */
    public static final String HTTP_METHOD = "http.request.method";

    /** Путь запроса. */
    public static final String URL_PATH = "url.path";

    /** Числовой код ответа; заполняется только для ответов об ошибке. */
    public static final String HTTP_STATUS = "http.response.status_code";

    /** Класс исключения (для записей со стеком). */
    public static final String ERROR_TYPE = "error.type";

    /** Стек исключения одной строкой. */
    public static final String ERROR_STACK_TRACE = "error.stack_trace";

    private LogFields() {
    }
}
