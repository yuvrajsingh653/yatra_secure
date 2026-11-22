package com.tourist.Yatra_secure.Repository;

import com.tourist.Yatra_secure.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<user, Long> {

    Optional<user> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("update user u set u.role = 'ROLE_TOURIST' where u.role is null")
    int assignDefaultRoleToNullUsers();
}


