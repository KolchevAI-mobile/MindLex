package ru.mindlex.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Базовый класс интеграционных тестов: поднимает настоящее приложение
 * на встроенном PostgreSQL с прогоном миграций Flyway.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String accessToken;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        var postgres = EmbeddedPostgresHolder.instance();
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    @BeforeEach
    void authenticate() throws Exception {
        accessToken = registerUser();
    }

    /** Регистрирует нового пользователя с уникальным e-mail и возвращает JWT. */
    protected String registerUser() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@mindlex.test";
        String body = """
                {"email":"%s","password":"secret123","displayName":"Тестовый пользователь","language":"en"}
                """.formatted(email);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return json(result).get("accessToken").asText();
    }

    protected MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + accessToken);
    }

    protected JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    /** Возвращает системную публичную колоду — она наполняется миграцией V2. */
    protected JsonNode publicDeck() throws Exception {
        MvcResult result = mockMvc.perform(authorized(get("/api/v1/decks")).param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode deck : json(result).get("content")) {
            if (deck.get("isPublic").asBoolean()) {
                return deck;
            }
        }
        throw new IllegalStateException("Публичная колода не найдена — проверьте миграцию V2");
    }
}
