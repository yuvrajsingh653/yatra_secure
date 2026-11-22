package com.tourist.Yatra_secure.dto;
import jakarta.validation.constraints.*;
public record SafetyZoneRequest(@NotBlank @Size(max=120) String name,@Size(max=1000) String description,@NotBlank @Pattern(regexp="SAFE|CAUTION|RESTRICTED") String zoneType,@NotBlank String boundaryGeoJson,@NotBlank @Size(max=120) String source,boolean verified,boolean active){}
