package com.tourist.Yatra_secure.controller;
import com.tourist.Yatra_secure.dto.*;import com.tourist.Yatra_secure.service.IncidentService;import jakarta.validation.Valid;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/incidents") public class IncidentController{
 private final IncidentService service;public IncidentController(IncidentService service){this.service=service;}
 @PostMapping public Map<String,Object> report(Authentication a,@Valid @RequestBody IncidentRequest r){return service.create(a.getName(),r,false);}
 @PostMapping("/sos") public Map<String,Object> sos(Authentication a,@Valid @RequestBody IncidentRequest r){return service.create(a.getName(),r,true);}
 @GetMapping("/mine") public List<Map<String,Object>> mine(Authentication a){return service.mine(a.getName());}
 @PostMapping("/{id}/cancel") public Map<String,Object> cancel(Authentication a,@PathVariable Long id){return service.cancel(a.getName(),id);}
 @GetMapping("/admin") public List<Map<String,Object>> all(@RequestParam(required=false) String status){return service.all(status);}
 @PatchMapping("/admin/{id}") public Map<String,Object> update(@PathVariable Long id,@Valid @RequestBody IncidentUpdateRequest r){return service.adminUpdate(id,r);}
}
