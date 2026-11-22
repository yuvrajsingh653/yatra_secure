package com.tourist.Yatra_secure.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class user {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String mobileNumber;

    @Column(length = 1000)
    private String address;

    @Column(nullable = false)
    private String role = "ROLE_TOURIST";
}
