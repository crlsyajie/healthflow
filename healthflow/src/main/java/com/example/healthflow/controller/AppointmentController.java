package com.example.healthflow.controller;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Patient;
import com.example.healthflow.service.DoctorService;
import com.example.healthflow.service.PatientService;
import com.example.healthflow.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/patient/appointment")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

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
            log.info("Received booking request - Date: {}, Time: {}", appointmentDate, appointmentTime);

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
            log.info("Parsed DateTime: {}", dateTime);

            // Create and set up the appointment
            Appointment appointment = new Appointment();
            appointment.setAppointmentTime(dateTime);
            appointment.setReason(reason);
            appointment.setNotes(notes);
            appointment.setDurationMinutes(30); // Default duration

            // Set patient
            Patient patient = patientService.getPatientByUsername(authentication.getName());
            if (patient == null) {
                log.error("Patient profile not found for username: {}", authentication.getName());
                redirectAttributes.addFlashAttribute("error", "Patient profile not found");
                return "redirect:/patient/doctors";
            }
            appointment.setPatient(patient);

            // Set doctor - Using direct lookup instead of stream filtering
            Doctor doctor = patientService.getDoctorById(doctorId);
            if (doctor == null) {
                log.error("Doctor not found with ID: {}", doctorId);
                redirectAttributes.addFlashAttribute("error", "Doctor not found. Please try again or contact support if the issue persists.");
                return "redirect:/patient/doctors";
            }
            appointment.setDoctor(doctor);

            // Save the appointment
            try {
                patientService.bookAppointment(appointment);
                redirectAttributes.addFlashAttribute("success", "Appointment booked successfully");
                return "redirect:/patient/appointments";
            } catch (Exception e) {
                log.error("Error saving appointment: ", e);
                redirectAttributes.addFlashAttribute("error", "Error saving appointment: " + e.getMessage());
                return "redirect:/patient/doctors";
            }

        } catch (Exception e) {
            log.error("Error booking appointment: ", e);
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
            @PathVariable("id") Long appointmentId,
            @RequestParam("appointmentDate") String appointmentDate,
            @RequestParam("appointmentTime") String appointmentTime,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {

        log.info("Received reschedule request for appointment ID: {}, date: {}, time: {}",
                appointmentId, appointmentDate, appointmentTime);

        // Parse appointment date and time
        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(appointmentDate);
            log.info("Parsed date: {}", date);
        } catch (Exception e) {
            log.error("Error parsing date: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Invalid date format. Please try again.");
            return "redirect:/patient/appointments";
        }

        try {
            time = LocalTime.parse(appointmentTime);
            log.info("Parsed time: {}", time);
        } catch (Exception e) {
            log.error("Error parsing time: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Invalid time format. Please try again.");
            return "redirect:/patient/appointments";
        }

        LocalDateTime newAppointmentTime = LocalDateTime.of(date, time);
        log.info("Computed new appointment time: {}", newAppointmentTime);

        try {
            // Get the appointment
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment == null) {
                log.error("Appointment with ID {} not found", appointmentId);
                redirectAttributes.addFlashAttribute("error", "Appointment not found.");
                return "redirect:/patient/appointments";
            }

            // Verify the appointment belongs to the logged-in patient
            if (!appointment.getPatient().getUser().getUsername().equals(authentication.getName())) {
                log.error("User {} attempted to reschedule appointment {} belonging to another patient",
                        authentication.getName(), appointmentId);
                redirectAttributes.addFlashAttribute("error", "You can only reschedule your own appointments.");
                return "redirect:/patient/appointments";
            }

            // Update the appointment time
            appointment.setAppointmentTime(newAppointmentTime);

            // Save the updated appointment
            appointmentService.saveAppointment(appointment);
            log.info("Successfully rescheduled appointment {} to {}", appointmentId, newAppointmentTime);

            redirectAttributes.addFlashAttribute("success", "Appointment rescheduled successfully.");
        } catch (Exception e) {
            log.error("Error during appointment reschedule: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to reschedule appointment. Please try again.");
        }

        return "redirect:/patient/appointments";
    }
}