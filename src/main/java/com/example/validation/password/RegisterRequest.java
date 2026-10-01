package com.example.validation.password;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank
                              @StrongPassword(min = 10)
                              String password) {
}
