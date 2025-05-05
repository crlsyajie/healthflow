package com.example.healthflow.controller;

import com.example.healthflow.dto.ProfileUpdateRequest;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Gender;
import com.example.healthflow.model.Patient;
import com.example.healthflow.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
public class PatientRestController {

    private static final Logger log = LoggerFactory.getLogger(PatientRestController.class);

    @Autowired
    private PatientService patientService;

    @GetMapping("/profile")
    public ResponseEntity<Patient> getProfile(Authentication authentication) {
        Patient patient = patientService.getPatientByUsername(authentication.getName());
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Long doctorId) {

        List<Doctor> doctors;

        if (doctorId != null) {
            // If doctorId is provided, return a list with just that doctor
            Doctor doctor = patientService.getDoctorById(doctorId);
            doctors = doctor != null ? Arrays.asList(doctor) : Collections.emptyList();
        } else {
            // Otherwise get all doctors
            doctors = patientService.getAllDoctors();

            // Filter by specialization if provided
            if (specialization != null && !specialization.isEmpty()) {
                doctors = doctors.stream()
                        .filter(doctor -> specialization.equals(doctor.getSpecialization()))
                        .collect(Collectors.toList());
            }
        }

        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(patientService.getAllDepartments());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody ProfileUpdateRequest updateRequest,
            Authentication authentication) {

        try {
            log.info("Profile update request received for user: {}", authentication.getName());
            log.info("Auth details: Principal={}, Authorities={}",
                    authentication.getPrincipal(), authentication.getAuthorities());
            log.info("Received profile data: {}", updateRequest);

            Patient patient = patientService.getPatientByUsername(authentication.getName());
            if (patient == null) {
                log.error("Patient not found for username: {}", authentication.getName());
                return ResponseEntity.badRequest().body("Patient not found");
            }

            // Update only allowed fields
            patient.setFirstName(updateRequest.getFirstName());
            patient.setLastName(updateRequest.getLastName());
            patient.setPhoneNumber(updateRequest.getPhoneNumber());
            patient.setAddress(updateRequest.getAddress());

            try {
                if (updateRequest.getGender() != null && !updateRequest.getGender().isEmpty()) {
                    Gender gender = Gender.valueOf(updateRequest.getGender().toUpperCase());
                    patient.setGender(gender);
                }
            } catch (IllegalArgumentException e) {
                log.error("Invalid gender value: {}", updateRequest.getGender());
                return ResponseEntity.badRequest().body("Invalid gender value: " + updateRequest.getGender());
            }

            try {
                if (updateRequest.getDateOfBirth() != null && !updateRequest.getDateOfBirth().isEmpty()) {
                    LocalDate dateOfBirth = LocalDate.parse(updateRequest.getDateOfBirth());
                    patient.setDateOfBirth(dateOfBirth);
                }
            } catch (DateTimeParseException e) {
                log.error("Invalid date format: {}", updateRequest.getDateOfBirth());
                return ResponseEntity.badRequest().body("Invalid date format: " + updateRequest.getDateOfBirth());
            }

            Patient savedPatient = patientService.updateProfile(patient);
            log.info("Profile updated successfully for patient ID: {}", savedPatient.getId());
            return ResponseEntity.ok(savedPatient);
        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating profile: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint(Authentication authentication) {
        log.info("Test endpoint called by user: {}", authentication.getName());
        return ResponseEntity.ok("API access is working for user: " + authentication.getName());
    }
}