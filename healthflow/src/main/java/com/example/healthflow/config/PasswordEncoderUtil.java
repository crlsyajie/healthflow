package com.example.healthflow.config;

import com.example.healthflow.model.Role;
import com.example.healthflow.model.User;
import com.example.healthflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderUtil implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Password Encoder Utility Running ===");

        // Add any test users you need here
        createUserIfNotExists("admin", "adminpass", "admin@example.com", Role.ADMIN);
        createUserIfNotExists("testdoctor", "password", "testdoctor@example.com", Role.DOCTOR);
        createUserIfNotExists("specialist", "password", "specialist@example.com", Role.DOCTOR);
        createUserIfNotExists("testpatient", "password", "testpatient@example.com", Role.PATIENT);
        createUserIfNotExists("janedoe", "password", "jane.doe@example.com", Role.PATIENT);

        System.out.println("=== Password Encoder Utility Complete ===");
    }

    private void createUserIfNotExists(String username, String password, String email, Role role) {
        User existingUser = userRepository.findByUsername(username);

        if (existingUser == null) {
            // Create new user with properly encoded password
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setEmail(email);
            newUser.setRole(role);
            userRepository.save(newUser);
            System.out.println("Created user: " + username + " with role: " + role);
        } else {
            // Update existing user's password to be properly encoded
            existingUser.setPassword(passwordEncoder.encode(password));
            userRepository.save(existingUser);
            System.out.println("Updated password for existing user: " + username);
        }
    }
}


// accounts
/*

** Patients


testpatient
password
testpatient@example.com

sakit
password
sakit@example.com

patiente
password
patiente@example.com


** Doctors

testdoctor
password
testdoctor@example.com

specialist
password
specialist@example.com

cardo
password
cardo@example.com

ortho
password
ortho@example.com

nero
password
nero@example.com






 */