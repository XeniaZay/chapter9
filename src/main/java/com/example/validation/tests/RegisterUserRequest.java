package com.example.validation.tests;

import com.example.validation.password.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(

        @NotBlank(message = "name must not be blank")
        String name,

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        String email,

        @NotBlank(message = "password must not be blank")
        @StrongPassword(min = 8)
        String password,

        @NotNull(message = "age must not be null")
        @Min(value = 18, message = "age must be at least 18")
        @Max(value = 120, message = "age must not exceed 120")
        Integer age
) {
}