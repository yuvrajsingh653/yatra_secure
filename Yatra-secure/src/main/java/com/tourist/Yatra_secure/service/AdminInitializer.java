package com.tourist.Yatra_secure.service;

import com.tourist.Yatra_secure.Repository.UserRepository;
import com.tourist.Yatra_secure.entity.user;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    @Value("${app.admin.email:}") private String email;
    @Value("${app.admin.password:}") private String password;
    public AdminInitializer(UserRepository users, PasswordEncoder encoder) { this.users=users; this.encoder=encoder; }
    @Override public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) return;
        users.findByEmail(email.trim()).ifPresentOrElse(existing -> {}, () -> {
            user admin = new user(); admin.setName("Administrator"); admin.setEmail(email.trim());
            admin.setPassword(encoder.encode(password)); admin.setRole("ROLE_ADMIN"); users.save(admin);
        });
    }
}
