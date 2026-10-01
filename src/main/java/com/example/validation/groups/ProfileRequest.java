package com.example.validation.groups;

import jakarta.validation.constraints.*;

public record ProfileRequest(@NotBlank(groups = Create.class, message = "name is required")
                             @Size(min = 2, max = 100, groups = { Create.class, Update.class },
                                     message = "name must be 2-100 characters")
                             String name,

                             @NotBlank(groups = Create.class, message = "email is required")
                             @Email(groups = { Create.class, Update.class },
                                     message = "email must be valid")
                             String email,

                             @NotNull(groups = Create.class, message = "age is required")
                             @Min(value = 18, groups = { Create.class, Update.class },
                                     message = "age must be at least 18")
                             @Max(value = 120, groups = { Create.class, Update.class },
                                     message = "age must not exceed 120")
                             Integer age) {
}
