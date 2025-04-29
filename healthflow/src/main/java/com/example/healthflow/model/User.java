package com.example.healthflow.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    // Add references to doctor and patient profiles
    @OneToOne(mappedBy = "user")
    @Transient // This field is not stored in the database, but calculated
    private Doctor doctorProfile;

    @OneToOne(mappedBy = "user")
    @Transient // This field is not stored in the database, but calculated
    private Patient patientProfile;
}