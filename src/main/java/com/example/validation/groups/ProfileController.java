package com.example.validation.groups;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final AtomicLong idGenerator = new AtomicLong(0);

    @PostMapping(consumes = "application/json",
            produces = "application/json")
    ResponseEntity<ProfileResponse> register (@Validated(Create.class) @RequestBody ProfileRequest request){

        Long id = idGenerator.incrementAndGet();
        ProfileResponse response = new ProfileResponse(id, request.name(),request.email(), request.age(), Instant.now());
        return ResponseEntity.created(URI.create("/api/profiles/" + id)).body(response);
    }

    @PatchMapping(value = "/{id}",
            consumes = "application/json",
            produces = "application/json")
    ResponseEntity<ProfileResponse> update(@PathVariable Long id, @Validated(Update.class) @RequestBody ProfileRequest request) {

        ProfileResponse response = new ProfileResponse(
                id,
                request.name() != null ? request.name() : "Alice",
                request.email() != null ? request.email() : "alice@example.com",
                request.age() != null ? request.age() : 25,
                Instant.now()
        );

        return ResponseEntity.ok(response);
    }

}