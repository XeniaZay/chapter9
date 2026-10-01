package com.example.validation.groups;

import java.time.Instant;

public record ProfileResponse(Long id,
                              String name,
                              String email,
                              Integer age,
                              Instant updatedAt) {
}
