package com.example.healthflow.controller;

import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Patient;
import com.example.healthflow.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
public class PatientRestController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/profile")
    public ResponseEntity<Patient> getProfile(Authentication authentication) {
        Patient patient = patientService.getPatientByUsername(authentication.getName());
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getDoctors(
            @RequestParam(required = false) String specialization) {

        List<Doctor> doctors = patientService.getAllDoctors();

        if (specialization != null && !specialization.isEmpty()) {
            doctors = doctors.stream()
                    .filter(doctor -> specialization.equals(doctor.getSpecialization()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(patientService.getAllDepartments());
    }

    @PutMapping("/profile")
    public ResponseEntity<Patient> updateProfile(
            @RequestBody Patient updatedPatient,
            Authentication authentication) {

        Patient patient = patientService.getPatientByUsername(authentication.getName());

        // Update only allowed fields
        patient.setFirstName(updatedPatient.getFirstName());
        patient.setLastName(updatedPatient.getLastName());
        patient.setPhoneNumber(updatedPatient.getPhoneNumber());
        patient.setAddress(updatedPatient.getAddress());
        patient.setGender(updatedPatient.getGender());
        patient.setDateOfBirth(updatedPatient.getDateOfBirth());

        Patient savedPatient = patientService.updateProfile(patient);
        return ResponseEntity.ok(savedPatient);
    }
}