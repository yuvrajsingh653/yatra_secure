package com.tourist.Yatra_secure.service;
import com.tourist.Yatra_secure.dto.LoginRequest;
import com.tourist.Yatra_secure.dto.RegisterRequest;
import com.tourist.Yatra_secure.entity.user;
import com.tourist.Yatra_secure.Repository.UserRepository;
import com.tourist.Yatra_secure.Security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import com.tourist.Yatra_secure.dto.AuthResponses;
import com.tourist.Yatra_secure.dto.ProfileUpdateRequest;

@Service
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponses.Registration registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException(HttpStatus.CONFLICT, "Email is already registered");
        }

        user user = new user();
        user.setRole("ROLE_TOURIST");

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);

        return new AuthResponses.Registration("User registered successfully", user.getId(), user.getName(), user.getEmail());
    }


    public AuthResponses.Login loginUser(LoginRequest request) {

        // Step 1: Find user by email
        user existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new AuthException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // Step 2: Verify password
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                existingUser.getPassword()
        );

        // Step 3: Check password
        if (!passwordMatches) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // Step 4: Generate JWT token
        String token = jwtService.generateToken(existingUser.getEmail(), normalizeRole(existingUser.getRole()));

        return new AuthResponses.Login("Login successful", token, "Bearer", jwtService.getExpirationSeconds());
    }

    private String normalizeRole(String role) {
        if ("ROLE_ADMIN".equals(role)) return "ROLE_ADMIN";
        return "ROLE_TOURIST";
    }

    public AuthResponses.Profile profile(String email) {
        user current = userRepository.findByEmail(email).orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "User profile was not found"));
        return toProfile(current);
    }

    public AuthResponses.Profile updateProfile(String email, ProfileUpdateRequest request) {
        user current = userRepository.findByEmail(email).orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "User profile was not found"));
        current.setName(request.name().trim());
        current.setMobileNumber(request.mobileNumber() == null || request.mobileNumber().isBlank() ? null : request.mobileNumber().trim());
        current.setAddress(request.address() == null || request.address().isBlank() ? null : request.address().trim());
        return toProfile(userRepository.save(current));
    }

    private AuthResponses.Profile toProfile(user current) {
        return new AuthResponses.Profile(current.getId(), current.getName(), current.getEmail(), current.getMobileNumber(), current.getAddress());
    }
}


