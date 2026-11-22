package com.tourist.Yatra_secure.dto;

public final class AuthResponses {
    private AuthResponses() {}
    public record Registration(String message, Long userId, String name, String email) {}
    public record Login(String message, String token, String tokenType, long expiresIn) {}
    public record Profile(Long id, String name, String email, String mobileNumber, String address) {}
    public record Error(String error, String message) {}
}
