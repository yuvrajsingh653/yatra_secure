package com.tourist.Yatra_secure.controller;

import com.tourist.Yatra_secure.Repository.TouristLocationRepository;
import com.tourist.Yatra_secure.Repository.UserRepository;
import com.tourist.Yatra_secure.dto.LocationUpdateRequest;
import com.tourist.Yatra_secure.entity.TouristLocation;
import com.tourist.Yatra_secure.entity.user;
import com.tourist.Yatra_secure.service.AuthException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/location")
public class LocationController {
 private final TouristLocationRepository locations; private final UserRepository users;
 public LocationController(TouristLocationRepository locations,UserRepository users){this.locations=locations;this.users=users;}
 @GetMapping("/sharing") @PreAuthorize("hasRole('TOURIST')")
 public Map<String,Object> sharingStatus(Authentication auth){
  return locations.findByTourist_Email(auth.getName())
   .<Map<String,Object>>map(location->Map.of("sharingEnabled",location.isSharingEnabled(),"updatedAt",location.getUpdatedAt()))
   .orElseGet(()->Map.of("sharingEnabled",false));
 }
 @PutMapping("/sharing") @PreAuthorize("hasRole('TOURIST')")
 public Map<String,Object> update(Authentication auth,@Valid @RequestBody LocationUpdateRequest req){
  user tourist=users.findByEmail(auth.getName()).orElseThrow(()->new AuthException(HttpStatus.NOT_FOUND,"Tourist not found"));
  TouristLocation location=locations.findByTourist_Email(auth.getName()).orElseGet(()->new TouristLocation(tourist,req.latitude(),req.longitude(),true,Instant.now()));
  location.setLatitude(req.latitude());location.setLongitude(req.longitude());location.setSharingEnabled(true);location.setUpdatedAt(Instant.now());locations.save(location);
  return Map.of("sharingEnabled",true,"updatedAt",location.getUpdatedAt());
 }
 @PutMapping("/sharing/location") @PreAuthorize("hasRole('TOURIST')")
 public Map<String,Object> updateLocation(Authentication auth,@Valid @RequestBody LocationUpdateRequest req){
  TouristLocation loc=locations.findByTourist_Email(auth.getName()).filter(TouristLocation::isSharingEnabled).orElseThrow(()->new AuthException(HttpStatus.CONFLICT,"Location sharing is disabled"));
  loc.setLatitude(req.latitude());loc.setLongitude(req.longitude());loc.setUpdatedAt(Instant.now());locations.save(loc);return Map.of("updatedAt",loc.getUpdatedAt());
 }
 @DeleteMapping("/sharing") @PreAuthorize("hasRole('TOURIST')")
 public Map<String,Object> stop(Authentication auth){locations.findByTourist_Email(auth.getName()).ifPresent(loc->{loc.setSharingEnabled(false);locations.save(loc);});return Map.of("sharingEnabled",false);}
 @GetMapping("/admin/shared") @PreAuthorize("hasRole('ADMIN')")
 public List<Map<String,Object>> shared(){return locations.findBySharingEnabledTrue().stream().map(loc->Map.<String,Object>of("touristId",loc.getTourist().getId(),"name",loc.getTourist().getName(),"latitude",loc.getLatitude(),"longitude",loc.getLongitude(),"updatedAt",loc.getUpdatedAt())).toList();}
}
