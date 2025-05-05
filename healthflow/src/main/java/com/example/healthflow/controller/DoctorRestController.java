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
import java.util.stream.Collectors;

// Simple DTO to avoid circular references in JSON serialization
class AppointmentDTO {
    private Long id;
    private String patientFirstName;
    private String patientLastName;
    private Long patientId;
    private String doctorFirstName;
    private String doctorLastName;
    private Long doctorId;
    private LocalDateTime appointmentTime;
    private String status;
    private String reason;
    private String notes;

    public AppointmentDTO(Appointment appointment) {
        this.id = appointment.getId();
        if (appointment.getPatient() != null) {
            this.patientFirstName = appointment.getPatient().getFirstName();
            this.patientLastName = appointment.getPatient().getLastName();
            this.patientId = appointment.getPatient().getId();
        }
        if (appointment.getDoctor() != null) {
            this.doctorFirstName = appointment.getDoctor().getFirstName();
            this.doctorLastName = appointment.getDoctor().getLastName();
            this.doctorId = appointment.getDoctor().getId();
        }
        this.appointmentTime = appointment.getAppointmentTime();
        this.status = appointment.getStatus().name();
        this.reason = appointment.getReason();
        this.notes = appointment.getNotes();
    }

    // Getters
    public Long getId() { return id; }
    public String getPatientFirstName() { return patientFirstName; }
    public String getPatientLastName() { return patientLastName; }
    public Long getPatientId() { return patientId; }
    public String getDoctorFirstName() { return doctorFirstName; }
    public String getDoctorLastName() { return doctorLastName; }
    public Long getDoctorId() { return doctorId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public String getNotes() { return notes; }
}

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
    public ResponseEntity<List<AppointmentDTO>> getTodaysAppointments(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        List<AppointmentDTO> appointmentDTOs = doctorService.getTodaysAppointments(doctor).stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/pending")
    public ResponseEntity<List<AppointmentDTO>> getPendingAppointments(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        List<AppointmentDTO> appointmentDTOs = doctorService.getPendingAppointments(doctor).stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/period")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        List<Appointment> appointments = doctorService.getAppointmentsForPeriod(doctor, startDate, endDate);

        // Convert entities to DTOs to avoid circular references
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/past")
    public ResponseEntity<List<AppointmentDTO>> getPastAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());

        // Get appointments for the period
        List<Appointment> appointments = doctorService.getAppointmentsForPeriod(doctor, startDate, endDate);

        // Filter to only completed or no-show appointments
        List<Appointment> pastAppointments = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.NO_SHOW)
                .collect(Collectors.toList());

        // Convert to DTOs
        List<AppointmentDTO> appointmentDTOs = pastAppointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/cancelled")
    public ResponseEntity<List<AppointmentDTO>> getCancelledAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());

        // Get appointments for the period
        List<Appointment> appointments = doctorService.getAppointmentsForPeriod(doctor, startDate, endDate);

        // Filter to only cancelled appointments
        List<Appointment> cancelledAppointments = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());

        // Convert to DTOs
        List<AppointmentDTO> appointmentDTOs = cancelledAppointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
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

    @PutMapping("/update-doctor")
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

        // Save the updated doctor using the service method
        doctor = doctorService.updateDoctorInfo(doctor);
        return ResponseEntity.ok(doctor);
    }
}