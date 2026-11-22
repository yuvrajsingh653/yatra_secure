package com.tourist.Yatra_secure.controller;

import com.tourist.Yatra_secure.dto.AuthResponses;
import com.tourist.Yatra_secure.dto.ProfileUpdateRequest;
import com.tourist.Yatra_secure.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }
    @GetMapping public AuthResponses.Profile get(Authentication authentication) { return userService.profile(authentication.getName()); }
    @PutMapping public AuthResponses.Profile update(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest request) {
        return userService.updateProfile(authentication.getName(), request);
    }
}
