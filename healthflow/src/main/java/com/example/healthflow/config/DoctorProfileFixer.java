package com.example.healthflow.config;

import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Role;
import com.example.healthflow.model.User;
import com.example.healthflow.repository.DoctorRepository;
import com.example.healthflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Component
public class DoctorProfileFixer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("============ Starting Doctor Profile Fixer ============");
        System.out.println("Looking for doctor users without doctor profiles...");

        // Find all users with DOCTOR role
        List<User> doctorUsers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.DOCTOR)
                .toList();

        System.out.println("Found " + doctorUsers.size() + " users with DOCTOR role");

        int fixedCount = 0;

        // For each doctor user, check if they have a doctor profile
        for (User doctorUser : doctorUsers) {
            Doctor doctor = doctorRepository.findByUser(doctorUser);

            // If no doctor profile exists, create one
            if (doctor == null) {
                fixedCount += createDoctorProfile(doctorUser) ? 1 : 0;
            }
        }

        System.out.println("Fixed " + fixedCount + " missing doctor profiles");
        System.out.println("============ Doctor Profile Fixer Complete ============");
    }

    /**
     * Creates a doctor profile for a user that has the DOCTOR role but no profile
     * @param doctorUser The user with DOCTOR role
     * @return true if profile was created, false otherwise
     */
    private boolean createDoctorProfile(User doctorUser) {
        if (doctorUser == null || doctorUser.getRole() != Role.DOCTOR) {
            return false;
        }

        System.out.println("Creating missing doctor profile for user: " + doctorUser.getUsername());

        Doctor doctor = new Doctor();
        doctor.setUser(doctorUser);

        // Extract first/last name from username if possible, or use defaults
        String username = doctorUser.getUsername();
        if (username.contains(".")) {
            String[] parts = username.split("\\.", 2);
            doctor.setFirstName(capitalizeFirstLetter(parts[0]));
            doctor.setLastName(capitalizeFirstLetter(parts[1]));
        } else {
            doctor.setFirstName("Doctor");
            doctor.setLastName(username);
        }

        // Generate a unique license number
        doctor.setLicenseNumber("DR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Set default values for other fields
        doctor.setSpecialization("General Medicine");
        doctor.setPhoneNumber("000-000-0000");
        doctor.setBio("Doctor profile automatically created by system fix.");

        // Save the new doctor profile
        doctorRepository.save(doctor);

        System.out.println("Created doctor profile: " + doctor.getFirstName() + " " + doctor.getLastName());
        return true;
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @RestController
    @RequestMapping("/api/fix")
    public class DoctorProfileFixerController {

        @GetMapping("/doctor/{username}")
        public ResponseEntity<?> fixDoctorProfile(@PathVariable String username) {
            User doctorUser = userRepository.findByUsername(username);

            if (doctorUser == null) {
                return ResponseEntity.badRequest().body("User not found: " + username);
            }

            if (doctorUser.getRole() != Role.DOCTOR) {
                return ResponseEntity.badRequest().body("User is not a doctor: " + username);
            }

            Doctor existingDoctor = doctorRepository.findByUser(doctorUser);
            if (existingDoctor != null) {
                return ResponseEntity.ok("Doctor profile already exists for: " + username);
            }

            boolean created = createDoctorProfile(doctorUser);
            if (created) {
                return ResponseEntity.ok("Created doctor profile for: " + username);
            } else {
                return ResponseEntity.badRequest().body("Failed to create doctor profile for: " + username);
            }
        }
    }
}