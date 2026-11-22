package com.tourist.Yatra_secure.controller;
import com.tourist.Yatra_secure.service.DigitalTouristIdService;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController public class DigitalTouristIdController{
 private final DigitalTouristIdService service;public DigitalTouristIdController(DigitalTouristIdService service){this.service=service;}
 @PostMapping("/api/tourist-id") public Map<String,Object> issue(Authentication auth){return service.privateView(service.issue(auth.getName()));}
 @GetMapping("/api/tourist-id/me") public Map<String,Object> mine(Authentication auth){return service.privateView(service.own(auth.getName()));}
 @GetMapping("/api/verify/tourist/{touristId}") public Map<String,Object> verify(@PathVariable String touristId){return service.publicVerify(touristId);}
 @GetMapping("/api/admin/tourist-ids") public List<Map<String,Object>> all(){return service.adminList();}
 @DeleteMapping("/api/admin/tourist-ids/{id}") public Map<String,Object> revoke(@PathVariable Long id){return service.revoke(id);}
}
