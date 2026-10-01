package com.example.validation.order;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @PostMapping
    ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request){

        String orderId = UUID.randomUUID().toString();

        List<OrderResponse.OrderItemResponse> items = request.items().stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.sku(),
                        item.quantity(),
                        item.priceMinor(),
                        item.priceMinor() * item.quantity()
                ))
                .toList();

        long total = items.stream()
                .mapToLong(OrderResponse.OrderItemResponse::subtotalMinor)
                .sum();

        OrderResponse response = new OrderResponse(
                orderId,
                request.customerId(),
                items,
                total,
                "CREATED"
        );

        return ResponseEntity
                .created(URI.create("/api/orders/" + orderId))
                .body(response);
    }
}
