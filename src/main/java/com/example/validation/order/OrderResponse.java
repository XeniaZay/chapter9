package com.example.validation.order;

import java.util.List;

public record OrderResponse(
        String orderId,
        String customerId,
        List<OrderItemResponse> items,
        long totalMinor,
        String status
) {
    public record OrderItemResponse(
            String sku,
            int quantity,
            long priceMinor,
            long subtotalMinor
    ) {
    }
}