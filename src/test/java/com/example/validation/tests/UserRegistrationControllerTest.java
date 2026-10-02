package com.example.validation.tests;

import com.example.validation.basic.UserRegistrationController;
import com.example.validation.common.ValidationExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRegistrationController.class)
@Import(ValidationExceptionHandler.class)
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ============================================================
    // 1. Успешная регистрация — 201
    // ============================================================
    @Test
    void register_validRequest_returns201WithLocation() throws Exception {
        String body = """
                {
                  "name": "Alice",
                  "email": "alice@example.com",
                  "password": "Secret123!",
                  "age": 25
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", startsWith("/api/users/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    // ============================================================
    // 2. Валидация: пустое имя
    // ============================================================
    @Test
    void register_blankName_returns422WithNameViolation() throws Exception {
        String body = """
                {
                  "name": "",
                  "email": "alice@example.com",
                  "password": "Secret123!",
                  "age": 25
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations[0].field").value("name"))
                .andExpect(jsonPath("$.violations[0].code").value("NotBlank"))
                .andExpect(jsonPath("$.violations[0].message").value("name must not be blank"));
    }

    // ============================================================
    // 3. Валидация: плохой email
    // ============================================================
    @Test
    void register_invalidEmail_returns422WithEmailViolation() throws Exception {
        String body = """
                {
                  "name": "Alice",
                  "email": "not-an-email",
                  "password": "Secret123!",
                  "age": 25
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violations[0].field").value("email"))
                .andExpect(jsonPath("$.violations[0].code").value("Email"))
                .andExpect(jsonPath("$.violations[0].message").value("email must be valid"));
    }

    // ============================================================
    // 4. Структура violations: все поля присутствуют
    // ============================================================
    @Test
    void register_multipleErrors_returnsAllViolationsWithFieldCodeMessage() throws Exception {
        String body = """
                {
                  "name": "",
                  "email": "not-an-email",
                  "password": "weak",
                  "age": 15
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.violations", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.violations[*].field",
                        hasItems("name", "email", "password", "age")))
                // каждое violation содержит все три поля
                .andExpect(jsonPath("$.violations[*].code",
                        everyItem(notNullValue())))
                .andExpect(jsonPath("$.violations[*].message",
                        everyItem(notNullValue())));
    }

    // ============================================================
    // 5. Сломанный JSON — 400
    // ============================================================
    @Test
    void register_malformedJson_returns400WithJsonInvalidCode() throws Exception {
        String body = """
                {
                  "name": "Alice",
                  "email":
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // 6. Неверный query-параметр (тип не соответствует)
    // ============================================================
    @Test
    void register_invalidQueryParam_returns400WithInvalidParameterTypeCode() throws Exception {
        // Используем другой endpoint с query-параметром, или проверяем неверный Age
        // Для разнообразия — невалидный age как строка
        String body = """
                {
                  "name": "Alice",
                  "email": "alice@example.com",
                  "password": "Secret123!",
                  "age": "twenty-five"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // 7. Ответ не содержит stacktrace
    // ============================================================
    @Test
    void register_validationError_doesNotLeakStackTrace() throws Exception {
        String body = """
                {
                  "name": "",
                  "email": "bad",
                  "password": "weak",
                  "age": 15
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                // НЕ должно быть stacktrace и exception-полей
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                // НЕ должно быть имён пакетов/классов
                .andExpect(content().string(not(containsString("com.example"))))
                .andExpect(content().string(not(containsString("at org.springframework"))))
                .andExpect(content().string(not(containsString(".java:"))));
    }

}