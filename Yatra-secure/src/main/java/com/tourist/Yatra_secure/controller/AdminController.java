package com.tourist.Yatra_secure.controller;

import com.tourist.Yatra_secure.Repository.UserRepository;
import com.tourist.Yatra_secure.entity.user;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository users;
    public AdminController(UserRepository users) { this.users = users; }
    @GetMapping("/dashboard") public Map<String, Object> dashboard() { return Map.of("message", "Admin dashboard", "touristCount", users.count()); }
    @GetMapping("/tourists") public List<Map<String, Object>> tourists() {
        return users.findAll().stream().filter(u -> !"ROLE_ADMIN".equals(u.getRole()))
                .map(u -> Map.<String,Object>of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"role",u.getRole()==null?"ROLE_TOURIST":u.getRole())).toList();
    }
}
