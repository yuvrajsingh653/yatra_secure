package com.tourist.Yatra_secure.dto;
import jakarta.validation.constraints.*;
public record IncidentUpdateRequest(@Pattern(regexp="OPEN|IN_PROGRESS|RESOLVED|CANCELLED") String status,@Size(max=160) String assignedTo){}
