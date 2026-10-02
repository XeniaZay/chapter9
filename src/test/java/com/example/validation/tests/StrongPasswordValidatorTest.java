package com.example.validation.tests;

import com.example.validation.password.RegisterRequest;
import com.example.validation.password.StrongPassword;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) factory.close();
    }

    /**
     * Простой DTO для изоляции валидатора.
     */
    record PasswordHolder(
            @StrongPassword(min = 8)
            String password
    ) {
    }

    @ParameterizedTest(name = "valid password: {0}")
    @ValueSource(strings = {
            "Secret123!",
            "Aa1!aaaa",
            "HelloWorld9#"
    })
    void validPassword_noViolations(String password) {
        Set<ConstraintViolation<PasswordHolder>> violations =
                validator.validate(new PasswordHolder(password));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest(name = "too short: {0}")
    @ValueSource(strings = { "Aa1!", "A1!aa", "Short1!" })
    void tooShort_violation(String password) {
        Set<ConstraintViolation<PasswordHolder>> violations =
                validator.validate(new PasswordHolder(password));
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Password is too weak");
    }

    @ParameterizedTest(name = "no uppercase: {0}")
    @ValueSource(strings = { "secret123!", "mypassword1@" })
    void noUppercase_violation(String password) {
        assertThat(validator.validate(new PasswordHolder(password))).hasSize(1);
    }

    @ParameterizedTest(name = "no digit: {0}")
    @ValueSource(strings = { "SecretPass!", "MyPassword@" })
    void noDigit_violation(String password) {
        assertThat(validator.validate(new PasswordHolder(password))).hasSize(1);
    }

    @ParameterizedTest(name = "no special: {0}")
    @ValueSource(strings = { "SecretPass123", "MyPassword123" })
    void noSpecial_violation(String password) {
        assertThat(validator.validate(new PasswordHolder(password))).hasSize(1);
    }
}