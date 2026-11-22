package com.tourist.Yatra_secure.controller;
import com.tourist.Yatra_secure.dto.LoginRequest;

import com.tourist.Yatra_secure.dto.RegisterRequest;
import com.tourist.Yatra_secure.service.UserService;
import com.tourist.Yatra_secure.dto.AuthResponses;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponses.Registration> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponses.Registration response = userService.registerUser(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponses.Login> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponses.Login response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public AuthResponses.Profile profile(Authentication authentication) {
        return userService.profile(authentication.getName());
    }
}


