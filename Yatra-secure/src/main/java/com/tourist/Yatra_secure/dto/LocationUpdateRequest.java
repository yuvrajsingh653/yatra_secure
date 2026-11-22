package com.tourist.Yatra_secure.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record LocationUpdateRequest(
 @DecimalMin(value="-90.0",message="Latitude must be at least -90") @DecimalMax(value="90.0",message="Latitude must be at most 90") double latitude,
 @DecimalMin(value="-180.0",message="Longitude must be at least -180") @DecimalMax(value="180.0",message="Longitude must be at most 180") double longitude) {}
