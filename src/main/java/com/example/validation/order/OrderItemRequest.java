package com.example.validation.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderItemRequest(@NotBlank(message = "sku must not be blank")
                               @Size(max = 50, message = "sku must not exceed 50 characters")
                               String sku,

                               @NotNull(message = "quantity is required")
                               @Min(value = 1, message = "quantity must be at least 1")
                               Integer quantity,

                               @NotNull(message = "priceMinor is required")
                               @Positive(message = "priceMinor must be positive")
                               Long priceMinor) {
}
