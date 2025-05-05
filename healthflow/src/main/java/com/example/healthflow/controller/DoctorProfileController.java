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

import java.security.Principal;

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
}