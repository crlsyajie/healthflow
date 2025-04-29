package com.example.healthflow.controller;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Patient;
import com.example.healthflow.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/patient/appointment")
public class AppointmentController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/book")
    public String bookAppointmentForm(@RequestParam Long doctorId, Model model) {
        model.addAttribute("doctorId", doctorId);
        model.addAttribute("appointment", new Appointment());
        return "patient/book-appointment";
    }

    @PostMapping("/book")
    public String bookAppointment(
            @RequestParam Long doctorId,
            @RequestParam String appointmentDate,
            @RequestParam String appointmentTime,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("Received booking request - Date: " + appointmentDate + ", Time: " + appointmentTime);

            // Parse the appointment date and time
            LocalDate date;
            LocalTime time;

            try {
                date = LocalDate.parse(appointmentDate);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Invalid date format: " + appointmentDate);
                return "redirect:/patient/doctors";
            }

            try {
                // Handle either HH:mm or H:mm format
                time = appointmentTime.contains(":") ?
                        LocalTime.parse(appointmentTime) :
                        LocalTime.parse(appointmentTime, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Invalid time format: " + appointmentTime);
                return "redirect:/patient/doctors";
            }

            LocalDateTime dateTime = LocalDateTime.of(date, time);
            System.out.println("Parsed DateTime: " + dateTime);

            // Create and set up the appointment
            Appointment appointment = new Appointment();
            appointment.setAppointmentTime(dateTime);
            appointment.setReason(reason);
            appointment.setNotes(notes);
            appointment.setDurationMinutes(30); // Default duration

            // Set patient
            try {
                Patient patient = patientService.getPatientByUsername(authentication.getName());
                if (patient == null) {
                    redirectAttributes.addFlashAttribute("error", "Patient profile not found");
                    return "redirect:/patient/doctors";
                }
                appointment.setPatient(patient);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error getting patient profile: " + e.getMessage());
                return "redirect:/patient/doctors";
            }

            // Set doctor
            try {
                Doctor doctor = patientService.getAllDoctors().stream()
                        .filter(d -> d.getId().equals(doctorId))
                        .findFirst()
                        .orElse(null);

                if (doctor == null) {
                    redirectAttributes.addFlashAttribute("error", "Doctor not found with ID: " + doctorId);
                    return "redirect:/patient/doctors";
                }
                appointment.setDoctor(doctor);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error finding doctor: " + e.getMessage());
                return "redirect:/patient/doctors";
            }

            // Save the appointment
            try {
                patientService.bookAppointment(appointment);
                redirectAttributes.addFlashAttribute("success", "Appointment booked successfully");
                return "redirect:/patient/appointments";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error saving appointment: " + e.getMessage());
                return "redirect:/patient/doctors";
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            redirectAttributes.addFlashAttribute("error", "Failed to book appointment: " + e.getMessage());
            return "redirect:/patient/doctors";
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelAppointment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        boolean canceled = patientService.cancelAppointment(id);
        if (canceled) {
            redirectAttributes.addFlashAttribute("success", "Appointment canceled successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel appointment");
        }
        return "redirect:/patient/appointments";
    }

    @GetMapping("/reschedule/{id}")
    public String rescheduleForm(@PathVariable Long id, Model model) {
        model.addAttribute("appointmentId", id);
        model.addAttribute("appointment", new Appointment());
        return "patient/reschedule-appointment";
    }

    @PostMapping("/reschedule/{id}")
    public String rescheduleAppointment(
            @PathVariable Long id,
            @RequestParam String appointmentDate,
            @RequestParam String appointmentTime,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            // Parse the appointment date and time
            LocalDate date = LocalDate.parse(appointmentDate);
            LocalTime time = LocalTime.parse(appointmentTime);
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            // Create a new appointment object with the updated time
            Appointment appointment = new Appointment();
            appointment.setAppointmentTime(dateTime);
            appointment.setNotes(notes);

            boolean rescheduled = patientService.rescheduleAppointment(id, appointment);

            if (rescheduled) {
                redirectAttributes.addFlashAttribute("success", "Appointment rescheduled successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to reschedule appointment");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reschedule appointment: " + e.getMessage());
        }

        return "redirect:/patient/appointments";
    }

    @GetMapping("/available-slots")
    @ResponseBody
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Logic to fetch available time slots for the doctor on the given date
        // This would typically involve checking the doctor's schedule and existing appointments

        // For simplicity, this is a placeholder implementation
        // In a real app, this would check against existing appointments
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        int slotDurationMinutes = 30;

        return ResponseEntity.ok("Available slots logic would go here");
    }
}