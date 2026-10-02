//package com.example.validation.tests;
//
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.net.URI;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicLong;
//
//@RestController
//@RequestMapping("/api/users")
//class UserRegistrationController {
//
//    private final AtomicLong idGenerator = new AtomicLong(0);
//    private final Set<String> existingEmails = ConcurrentHashMap.newKeySet();
//
//    @PostMapping(
//            value = "/register",
//            consumes = "application/json",
//            produces = "application/json"
//    )
//    ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
//
//        if (!existingEmails.add(request.email())) {
//            throw new UserAlreadyExistsException(request.email());
//        }
//
//        Long id = idGenerator.incrementAndGet();
//        UserResponse response = new UserResponse(id, request.name(), request.email(), request.age());
//
//        return ResponseEntity
//                .created(URI.create("/api/users/" + id))
//                .body(response);
//    }
//}