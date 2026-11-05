package ru.mindlex.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Кладёт в MDC контекст HTTP-запроса, чтобы каждая запись журнала несла идентификатор запроса.
 *
 * <p>Идентификатор связывает между собой события одного обращения. Клиент может передать
 * свой заголовком {@code X-Request-Id}, иначе значение генерируется на сервере.
 *
 * <p>Фильтр имеет наивысший приоритет, поэтому контекст доступен фильтрам безопасности
 * и обработчикам исключений.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    /** Входящий заголовок со сквозным идентификатором запроса. */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /** Максимальная длина принимаемого извне идентификатора — защита от мусора в логах. */
    private static final int MAX_REQUEST_ID_LENGTH = 64;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(LogFields.REQUEST_ID, requestId);
        MDC.put(LogFields.HTTP_METHOD, request.getMethod());
        MDC.put(LogFields.URL_PATH, request.getRequestURI());
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Поток возвращается в пул Tomcat, поэтому MDC обязательно очищаем.
            // Код ответа мог быть проставлен обработчиком ошибок — снимаем и его.
            MDC.remove(LogFields.REQUEST_ID);
            MDC.remove(LogFields.HTTP_METHOD);
            MDC.remove(LogFields.URL_PATH);
            MDC.remove(LogFields.HTTP_STATUS);
        }
    }

    private static String resolveRequestId(HttpServletRequest request) {
        String incoming = request.getHeader(REQUEST_ID_HEADER);
        if (incoming != null) {
            String trimmed = incoming.trim();
            // Идентификатор приходит от клиента, поэтому пропускаем только безопасный алфавит.
            if (!trimmed.isEmpty()
                    && trimmed.length() <= MAX_REQUEST_ID_LENGTH
                    && trimmed.matches("[A-Za-z0-9._-]+")) {
                return trimmed;
            }
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Пробы Actuator вызываются каждые несколько секунд, поэтому в журнал не попадают:
     * иначе они вытесняли бы полезные записи из окна «последние N минут».
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/");
    }
}
