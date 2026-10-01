package com.example.validation.password;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
public class PasswordRegistrationController {

    @PostMapping("/change")
    RegisterRequest changePassword (@Valid @RequestBody RegisterRequest request){
        return request;
    }
}
