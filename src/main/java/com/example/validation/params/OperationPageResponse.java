package com.example.validation.params;

import java.util.List;

public record OperationPageResponse(
        List<OperationResponse> content,
        int page,
        int size,
        long total
) {
}