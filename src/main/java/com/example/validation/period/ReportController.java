package com.example.validation.period;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
class ReportController {
    @PostMapping("/search")
    ReportPeriodRequest search(@Valid @RequestBody ReportPeriodRequest request) {
        return request;
    }
}
