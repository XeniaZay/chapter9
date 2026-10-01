package com.example.validation.params;

import java.time.LocalDate;

public record OperationResponse(
        String id,
        String type,
        long amountMinor,
        String currency,
        OperationStatus status,
        LocalDate date
) {
}