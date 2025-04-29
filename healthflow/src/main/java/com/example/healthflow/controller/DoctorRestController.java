package com.example.healthflow.controller;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.AppointmentStatus;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Patient;
import com.example.healthflow.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class DoctorRestController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/profile")
    public ResponseEntity<Doctor> getProfile(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<List<Appointment>> getTodaysAppointments(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctorService.getTodaysAppointments(doctor));
    }

    @GetMapping("/appointments/pending")
    public ResponseEntity<List<Appointment>> getPendingAppointments(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctorService.getPendingAppointments(doctor));
    }

    @GetMapping("/appointments/period")
    public ResponseEntity<List<Appointment>> getAppointmentsForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctorService.getAppointmentsForPeriod(doctor, startDate, endDate));
    }

    @GetMapping("/calendar/weekly")
    public ResponseEntity<Map<LocalDate, List<Appointment>>> getWeeklyCalendar(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctorService.getWeeklyCalendar(doctor));
    }

    @GetMapping("/calendar/monthly")
    public ResponseEntity<Map<LocalDate, List<Appointment>>> getMonthlyCalendar(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctorService.getMonthlyCalendar(doctor));
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<Patient> getPatientDetails(@PathVariable Long patientId) {
        Patient patient = doctorService.getPatientDetails(patientId);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestParam AppointmentStatus status) {

        Appointment appointment = doctorService.updateAppointmentStatus(appointmentId, status);
        if (appointment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(appointment);
    }

    @PostMapping("/availability")
    public ResponseEntity<?> setAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam boolean available,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        doctorService.setAvailability(doctor, startTime, endTime, available);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Appointment>> getNotifications(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctorService.getNotifications(doctor));
    }

    @PutMapping("/profile")
    public ResponseEntity<Doctor> updateProfile(
            @RequestBody Doctor updatedDoctor,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());

        // Update only allowed fields
        doctor.setFirstName(updatedDoctor.getFirstName());
        doctor.setLastName(updatedDoctor.getLastName());
        doctor.setPhoneNumber(updatedDoctor.getPhoneNumber());
        doctor.setBio(updatedDoctor.getBio());
        doctor.setSpecialization(updatedDoctor.getSpecialization());

        // Save the updated doctor
        // A separate service method could be implemented for this
        return ResponseEntity.ok(doctor);
    }
}