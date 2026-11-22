package com.tourist.Yatra_secure.controller;

import com.tourist.Yatra_secure.Repository.SafetyZoneRepository;
import com.tourist.Yatra_secure.dto.SafetyZoneRequest;
import com.tourist.Yatra_secure.entity.SafetyZone;
import com.tourist.Yatra_secure.service.AuthException;
import com.tourist.Yatra_secure.service.SafetyZoneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/safety-zones")
public class SafetyZoneController {
 private final SafetyZoneRepository repository;private final SafetyZoneService service;
 public SafetyZoneController(SafetyZoneRepository repository,SafetyZoneService service){this.repository=repository;this.service=service;}
 @GetMapping("/active") @PreAuthorize("hasRole('TOURIST')") public List<Map<String,Object>> active(){return repository.findByActiveTrueAndVerifiedTrue().stream().map(this::publicZone).toList();}
 @PostMapping @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> create(@Valid @RequestBody SafetyZoneRequest request){return adminZone(service.save(new SafetyZone(),request));}
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> update(@PathVariable Long id,@Valid @RequestBody SafetyZoneRequest request){SafetyZone zone=repository.findById(id).orElseThrow(()->new AuthException(HttpStatus.NOT_FOUND,"Safety zone not found"));return adminZone(service.save(zone,request));}
 @PatchMapping("/{id}/status") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> status(@PathVariable Long id,@RequestBody Map<String,Boolean> request){SafetyZone zone=repository.findById(id).orElseThrow(()->new AuthException(HttpStatus.NOT_FOUND,"Safety zone not found"));Boolean active=request.get("active");if(active==null)throw new AuthException(HttpStatus.BAD_REQUEST,"active is required");SafetyZoneRequest dto=new SafetyZoneRequest(zone.getName(),zone.getDescription(),zone.getZoneType(),zone.getBoundaryGeoJson(),zone.getSource(),zone.isVerified(),active);return adminZone(service.save(zone,dto));}
 @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<Map<String,Object>> all(){return repository.findAll().stream().map(this::adminZone).toList();}
 @GetMapping("/check") @PreAuthorize("hasRole('TOURIST')") public Map<String,Object> check(@RequestParam double latitude,@RequestParam double longitude){if(!Double.isFinite(latitude)||latitude < -90||latitude>90||!Double.isFinite(longitude)||longitude < -180||longitude>180)throw new AuthException(HttpStatus.BAD_REQUEST,"Invalid coordinates");List<Map<String,Object>> zones=service.checkPoint(latitude,longitude);return Map.of("zones",zones,"status",zones.isEmpty()?"NO_VERIFIED_ZONE_MATCH":"ZONE_MATCH","message",zones.isEmpty()?"No active verified zone matches this location. This does not confirm the area is safe.":"A verified zone matches this location. Review the advisory and current local guidance.");}
 private Map<String,Object> publicZone(SafetyZone z){return Map.of("id",z.getId(),"name",z.getName(),"description",Objects.toString(z.getDescription(),""),"zoneType",z.getZoneType(),"boundary",service.validateGeoJson(z.getBoundaryGeoJson()),"source",z.getSource(),"verified",true);}
 private Map<String,Object> adminZone(SafetyZone z){Map<String,Object> out=new LinkedHashMap<>(publicZone(z));out.put("active",z.isActive());out.put("createdAt",z.getCreatedAt());out.put("updatedAt",z.getUpdatedAt());return out;}
}
