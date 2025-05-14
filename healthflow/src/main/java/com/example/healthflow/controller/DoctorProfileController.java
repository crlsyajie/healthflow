package com.example.healthflow.controller;

import com.example.healthflow.dto.PasswordChangeRequest;
import com.example.healthflow.dto.ProfileUpdateRequest;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.User;
import com.example.healthflow.service.DoctorService;
import com.example.healthflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

// This controller only handles REST API endpoints for doctor profile updates
@RestController
@RequestMapping("/api/doctor")
public class DoctorProfileController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request, Principal principal) {
        try {
            String username = principal.getName();
            Doctor doctor = doctorService.getDoctorByUsername(username);

            doctor.setFirstName(request.getFirstName());
            doctor.setLastName(request.getLastName());
            doctor.setPhoneNumber(request.getPhoneNumber());

            // Update using the correct method from service
            doctorService.updateDoctorInfo(doctor);

            return ResponseEntity.ok().body("Profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating profile: " + e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request, Principal principal) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);

            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Current password is incorrect");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userService.saveUser(user);

            return ResponseEntity.ok().body("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing password: " + e.getMessage());
        }
    }
    
    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile file, Principal principal) {
        try {
            String username = principal.getName();
            Doctor doctor = doctorService.getDoctorByUsername(username);

            if (doctor == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Doctor profile not found");
            }

            // Create directory if it doesn't exist
            String uploadDir = "uploads/doctor-profiles/";
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save the file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // Update doctor profile with the image path
            doctor = doctorService.updateDoctorProfile(doctor, "/uploads/doctor-profiles/" + filename);
            
            return ResponseEntity.ok().body("Profile image uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading profile image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating profile: " + e.getMessage());
        }
    }
}