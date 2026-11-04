package ru.mindlex;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import ru.mindlex.support.AbstractIntegrationTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Сквозные проверки REST-API: аутентификация, CRUD, игровые механики и статистика.
 * Работает на настоящем PostgreSQL, поднятом внутри тестов.
 */
class ApiFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Миграции наполняют системную колоду и цепочки синонимов")
    void seedContentIsAvailable() throws Exception {
        JsonNode deck = publicDeck();
        assertThat(deck.get("wordCount").asLong()).isEqualTo(40);

        MvcResult chains = mockMvc.perform(authorized(get("/api/v1/synonym-chains")))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json(chains)).hasSize(4);
        assertThat(json(chains).get(0).get("steps")).isNotEmpty();
    }

    @Test
    @DisplayName("Колода и слова поддерживают полный CRUD-цикл")
    void deckAndWordCrud() throws Exception {
        MvcResult created = mockMvc.perform(authorized(post("/api/v1/decks"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Бизнес-лексика","language":"en","description":"Слова для переговоров"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Бизнес-лексика"))
                .andReturn();
        String deckId = json(created).get("id").asText();

        MvcResult word = mockMvc.perform(authorized(post("/api/v1/decks/" + deckId + "/words"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"negotiate","translation":"вести переговоры","partOfSpeech":"verb",
                                 "transcription":"nɪˈɡəʊ.ʃi.eɪt","alternatives":["bargain","haggle"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alternatives[0]").value("bargain"))
                .andReturn();
        String wordId = json(word).get("id").asText();

        mockMvc.perform(authorized(get("/api/v1/decks/" + deckId + "/words")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(authorized(put("/api/v1/words/" + wordId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"negotiate","translation":"договариваться"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.translation").value("договариваться"));

        mockMvc.perform(authorized(delete("/api/v1/words/" + wordId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(authorized(delete("/api/v1/decks/" + deckId)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Механика Active Recall: вопрос, ответ, обновление прогресса и итоги")
    void activeRecallSessionFlow() throws Exception {
        selectPublicDeck();

        String sessionId = startSession("ACTIVE_RECALL");
        JsonNode question = nextQuestion(sessionId);
        assertThat(question.get("promptKind").asText()).isEqualTo("WORD");

        // Правильный ответ узнаём через публичный CRUD-метод, как это сделал бы клиент.
        String wordId = question.get("questionId").asText();
        String term = json(mockMvc.perform(authorized(get("/api/v1/words/" + wordId)))
                .andExpect(status().isOk())
                .andReturn()).get("term").asText();

        MvcResult wrong = mockMvc.perform(authorized(post("/api/v1/sessions/" + sessionId + "/answers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionId":"%s","answer":"совершенно другой ответ","elapsedMs":1500}
                                """.formatted(wordId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andReturn();
        assertThat(json(wrong).get("quality").asInt()).isLessThan(3);

        JsonNode next = nextQuestion(sessionId);
        assertThat(next.get("questionId").asText()).isNotEqualTo(wordId);

        MvcResult correct = mockMvc.perform(authorized(post("/api/v1/sessions/" + sessionId + "/answers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionId":"%s","answer":"%s","elapsedMs":900}
                                """.formatted(next.get("questionId").asText(), answerFor(next))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.quality").value(5))
                .andReturn();
        assertThat(json(correct).get("progress").get("correctCount").asInt()).isEqualTo(1);
        assertThat(json(correct).get("score").asInt()).isGreaterThan(0);

        MvcResult summary = mockMvc.perform(authorized(post("/api/v1/sessions/" + sessionId + "/finish")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctCount").value(1))
                .andExpect(jsonPath("$.incorrectCount").value(1))
                .andReturn();
        assertThat(json(summary).get("answeredCount").asInt()).isEqualTo(2);

        // Серия занятий и дневная статистика должны отразить активность
        mockMvc.perform(authorized(get("/api/v1/dashboard")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibleStreak").value(1))
                .andExpect(jsonPath("$.todayAnswered").value(2))
                .andExpect(jsonPath("$.totalWords").value(40));
    }

    @Test
    @DisplayName("После ответа на все вопросы сессия перестаёт выдавать задания")
    void sessionStopsWhenQuestionLimitReached() throws Exception {
        selectPublicDeck();
        // Дневная цель 5 — быстрее исчерпать сессию
        mockMvc.perform(authorized(put("/api/v1/settings"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dailyGoal":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyGoal").value(5));

        String sessionId = startSession("ACTIVE_RECALL");
        for (int i = 0; i < 5; i++) {
            JsonNode question = nextQuestion(sessionId);
            mockMvc.perform(authorized(post("/api/v1/sessions/" + sessionId + "/answers"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"questionId":"%s","answer":"%s","elapsedMs":800}
                                    """.formatted(question.get("questionId").asText(), answerFor(question))))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(authorized(get("/api/v1/sessions/" + sessionId + "/question")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Механика Cloze: упражнение проверяется без учёта регистра")
    void clozeSessionFlow() throws Exception {
        selectPublicDeck();
        String sessionId = startSession("CLOZE");

        JsonNode question = nextQuestion(sessionId);
        assertThat(question.get("promptKind").asText()).isEqualTo("SENTENCE");
        assertThat(question.get("prompt").asText()).contains("___");
        assertThat(question.get("timeLimitSeconds").asInt()).isBetween(30, 45);

        String exerciseId = question.get("questionId").asText();
        String expected = json(mockMvc.perform(authorized(get("/api/v1/cloze-exercises/" + exerciseId)))
                .andExpect(status().isOk())
                .andReturn()).get("answer").asText();

        mockMvc.perform(authorized(post("/api/v1/sessions/" + sessionId + "/answers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionId":"%s","answer":"%s","elapsedMs":2000}
                                """.formatted(exerciseId, expected.toUpperCase())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    @DisplayName("Механика Timed Rush: очки начисляются, таймер ограничен 90 секундами")
    void rushSessionFlow() throws Exception {
        selectPublicDeck();
        String sessionId = startSession("RUSH");

        MvcResult session = mockMvc.perform(authorized(get("/api/v1/sessions/" + sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationSeconds").value(90))
                .andExpect(jsonPath("$.questionLimit").value(0))
                .andReturn();
        assertThat(json(session).get("timeRemainingSeconds").asInt()).isBetween(1, 90);

        JsonNode question = nextQuestion(sessionId);
        mockMvc.perform(authorized(post("/api/v1/sessions/" + sessionId + "/answers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionId":"%s","answer":"%s","elapsedMs":500}
                                """.formatted(question.get("questionId").asText(), answerFor(question))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                // первое правильное слово: комбо 1, базовые 10 очков и бонус 1.2 за время
                .andExpect(jsonPath("$.scoreDelta").value(12));

        mockMvc.perform(authorized(post("/api/v1/sessions/" + sessionId + "/finish")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bestScore").value(12))
                .andExpect(jsonPath("$.newRecord").value(true));
    }

    @Test
    @DisplayName("Механика Synonym Chain: шаг цепочки принимает точный синоним")
    void synonymChainFlow() throws Exception {
        String sessionId = startSession("SYNONYM_CHAIN");
        JsonNode question = nextQuestion(sessionId);
        assertThat(question.get("promptKind").asText()).isEqualTo("CHAIN_STEP");
        assertThat(question.get("chainStepNumber").asInt()).isEqualTo(1);
        assertThat(question.get("hintOptions")).isNotEmpty();

        String stepId = question.get("questionId").asText();
        String synonym = findSynonym(stepId);

        mockMvc.perform(authorized(post("/api/v1/sessions/" + sessionId + "/answers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionId":"%s","answer":"%s","elapsedMs":1200}
                                """.formatted(stepId, synonym)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.quality").value(4));

        JsonNode next = nextQuestion(sessionId);
        assertThat(next.get("chainStepNumber").asInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("Напоминания доступны только владельцу и проходят полный CRUD")
    void remindersCrud() throws Exception {
        MvcResult created = mockMvc.perform(authorized(post("/api/v1/reminders"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"plannedAt":"2026-03-01T19:00:00Z","title":"Повторить слова","body":"15 минут"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String reminderId = json(created).get("id").asText();

        mockMvc.perform(authorized(get("/api/v1/reminders")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Повторить слова"));

        mockMvc.perform(authorized(put("/api/v1/reminders/" + reminderId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"plannedAt":"2026-03-02T19:00:00Z","title":"Повторить слова","sent":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sent").value(true));

        // Другой пользователь не должен видеть чужое напоминание
        String otherToken = registerUser();
        mockMvc.perform(get("/api/v1/reminders/" + reminderId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(authorized(delete("/api/v1/reminders/" + reminderId)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Валидация настроек и запросов возвращает 400")
    void validationErrors() throws Exception {
        mockMvc.perform(authorized(put("/api/v1/settings"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rushSeconds":200,"dailyGoal":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.rushSeconds").exists())
                .andExpect(jsonPath("$.fieldErrors.dailyGoal").exists());

        mockMvc.perform(authorized(post("/api/v1/sessions"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Без токена защищённые методы недоступны")
    void unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/decks")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Health-проба доступна без аутентификации")
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Прогресс по слову можно сбросить")
    void progressCanBeReset() throws Exception {
        selectPublicDeck();
        String sessionId = startSession("ACTIVE_RECALL");
        JsonNode question = nextQuestion(sessionId);
        String wordId = question.get("questionId").asText();

        mockMvc.perform(authorized(post("/api/v1/sessions/" + sessionId + "/answers"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionId":"%s","answer":"%s"}
                                """.formatted(wordId, answerFor(question))))
                .andExpect(status().isOk());

        mockMvc.perform(authorized(get("/api/v1/progress/" + wordId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(2));

        mockMvc.perform(authorized(delete("/api/v1/progress/" + wordId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(authorized(get("/api/v1/progress/" + wordId)))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Вспомогательные методы
    // ------------------------------------------------------------------

    private void selectPublicDeck() throws Exception {
        String deckId = publicDeck().get("id").asText();
        mockMvc.perform(authorized(put("/api/v1/settings"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selectedDeckId":"%s","dailyGoal":15}
                                """.formatted(deckId)))
                .andExpect(status().isOk());
    }

    private String startSession(String mechanic) throws Exception {
        MvcResult result = mockMvc.perform(authorized(post("/api/v1/sessions"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mechanic":"%s"}
                                """.formatted(mechanic)))
                .andExpect(status().isCreated())
                .andReturn();
        return json(result).get("id").asText();
    }

    private JsonNode nextQuestion(String sessionId) throws Exception {
        MvcResult result = mockMvc.perform(authorized(get("/api/v1/sessions/" + sessionId + "/question")))
                .andExpect(status().isOk())
                .andReturn();
        return json(result);
    }

    /** Правильный ответ для вопроса-слова: подтягиваем слово по идентификатору. */
    private String answerFor(JsonNode question) throws Exception {
        assertThat(question.get("promptKind").asText()).isEqualTo("WORD");
        return json(mockMvc.perform(authorized(get("/api/v1/words/" + question.get("questionId").asText())))
                .andExpect(status().isOk())
                .andReturn()).get("term").asText();
    }

    /** Ищем принимаемый синоним шага в списке цепочек. */
    private String findSynonym(String stepId) throws Exception {
        JsonNode chains = json(mockMvc.perform(authorized(get("/api/v1/synonym-chains")))
                .andExpect(status().isOk())
                .andReturn());
        for (JsonNode chain : chains) {
            for (JsonNode step : chain.get("steps")) {
                if (step.get("id").asText().equals(stepId)) {
                    return step.get("acceptedSynonyms").get(0).asText();
                }
            }
        }
        throw new IllegalStateException("Шаг цепочки " + stepId + " не найден");
    }

    @Test
    @DisplayName("Несуществующая колода возвращает 404")
    void missingDeckReturnsNotFound() throws Exception {
        mockMvc.perform(authorized(get("/api/v1/decks/" + UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }
}
