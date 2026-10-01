package com.example.validation.basic;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/users")
public class UserRegistrationController {

    private final AtomicLong idGenerator = new AtomicLong(0);
    @PostMapping(value = "/register",
            consumes = "application/json",
            produces = "application/json")
    ResponseEntity<UserResponse> register (@Valid @RequestBody RegisterUserRequest request){
        Long id = idGenerator.incrementAndGet();
        UserResponse response = new UserResponse(id, request.name(),request.email(), request.age());
        return ResponseEntity.created(URI.create("/api/users/" + id)).body(response);
    }

}
