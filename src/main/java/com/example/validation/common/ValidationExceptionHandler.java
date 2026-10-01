package com.example.validation.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex){
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        problem.setTitle("Validation failed");
        problem.setDetail("Request contains invalid fields");

        var violations = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "code", error.getCode(),
                        "message", error.getDefaultMessage()
                ))
                .toList();

        problem.setProperty("code", "VALIDATION_FAILED");
        problem.setProperty("violations", violations);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex,
                                            HttpServletRequest request) {

        List<Map<String, String>> violations = ex.getConstraintViolations()
                .stream()
                .map(cv -> {
                    // propertyPath = "search.page" — берём последний сегмент
                    String field = cv.getPropertyPath() != null
                            ? lastSegment(cv.getPropertyPath().toString())
                            : "unknown";
                    String code = cv.getConstraintDescriptor() != null
                            && cv.getConstraintDescriptor().getAnnotation() != null
                            ? cv.getConstraintDescriptor().getAnnotation()
                            .annotationType().getSimpleName()
                            : "INVALID";
                    String message = cv.getMessage() != null
                            ? cv.getMessage()
                            : "invalid value";
                    return Map.of(
                            "field", field,
                            "code", code,
                            "message", message
                    );
                })
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "Validation failed for " + violations.size() + " parameter(s)"
        );
        problem.setTitle("Validation failed");
        problem.setType(URI.create("https://api.example.com/problems/validation-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "VALIDATION_FAILED");
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("violations", violations);
        return problem;
    }

    private String lastSegment(String path) {
        // "search.page" → "page"; "search" → "search"
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }
}
