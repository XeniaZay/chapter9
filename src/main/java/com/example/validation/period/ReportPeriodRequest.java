package com.example.validation.period;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@ValidPeriod
public record ReportPeriodRequest(

        @NotNull(message = "from is required")
        LocalDate from,

        @NotNull(message = "to is required")
        LocalDate to
) {
}