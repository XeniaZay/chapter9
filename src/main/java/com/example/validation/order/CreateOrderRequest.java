package com.example.validation.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(

        @NotBlank(message = "customerId must not be blank")
        String customerId,

        @NotEmpty(message = "order must contain at least one item")
        @Size(max = 100, message = "order must not exceed 100 items")
        List<@Valid OrderItemRequest> items
) {
}