package com.tourist.Yatra_secure.service;

import com.tourist.Yatra_secure.Repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleMigration implements ApplicationRunner {
    private final UserRepository users;
    public RoleMigration(UserRepository users) { this.users = users; }
    @Override public void run(ApplicationArguments args) { users.assignDefaultRoleToNullUsers(); }
}
