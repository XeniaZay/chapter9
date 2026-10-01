package com.example.validation.params;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/operations")
@Validated
class OperationController {

    @GetMapping()
    ResponseEntity<OperationPageResponse> search(

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page must be >= 0")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size must be between 1 and 100")
            @Max(value = 100, message = "size must be between 1 and 100")
            int size,

            @RequestParam(required = false)
            OperationStatus status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestHeader(value = "X-Client-Version", required = true)
            @NotBlank(message = "X-Client-Version header is required")
            String clientVersion
    ) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new InvalidPeriodException("from must be strictly before to");
        }

        // Mock-данные
        List<OperationResponse> content = List.of(
                new OperationResponse("op-1", "PAYMENT", 19900, "RUB",
                        OperationStatus.DONE, LocalDate.of(2026, 10, 1)),
                new OperationResponse("op-2", "REFUND", 5000, "RUB",
                        OperationStatus.PROCESSING, LocalDate.of(2026, 10, 2))
        );

        OperationPageResponse response = new OperationPageResponse(content, page, size, content.size());
        return ResponseEntity.ok(response);
    }
}