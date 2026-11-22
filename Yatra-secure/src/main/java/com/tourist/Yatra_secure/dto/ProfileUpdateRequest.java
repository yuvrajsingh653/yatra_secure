package com.tourist.Yatra_secure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,
        @Pattern(regexp = "^$|^[+]?[0-9() -]{7,20}$", message = "Enter a valid mobile number") String mobileNumber,
        @Size(max = 1000, message = "Address must be at most 1000 characters") String address) {}
