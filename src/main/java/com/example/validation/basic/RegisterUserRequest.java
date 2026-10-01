package com.example.validation.basic;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

        @NotBlank(message = "name must not be blank")
        @Size(max = 100, message = "name must not exceed 100 characters")
        String name,

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        String email,

        @NotBlank(message = "password must not be blank")
        @Size(min = 8, max = 64, message = "password must be 8-64 characters")
        String password,

        @NotNull(message = "age must not be null")
        @Min(value = 18, message = "age must be at least 18")
        @Max(value = 120, message = "age must not exceed 120")
        Integer age
) {
}