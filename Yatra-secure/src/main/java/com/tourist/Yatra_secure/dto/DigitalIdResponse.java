package com.tourist.Yatra_secure.dto;
import java.time.Instant;
public record DigitalIdResponse(String touristId,String status,Instant issuedAt,Instant createdAt,Instant updatedAt,String verificationUrl){}
