package com.example.healthflow.controller;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.AppointmentStatus;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Patient;
import com.example.healthflow.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
            if (doctor == null) {
                // If doctor profile doesn't exist, return to login page with error
                return "redirect:/auth/login?error=doctor_not_found";
            }

            model.addAttribute("doctor", doctor);

            // Safely get appointments with null checks
            List<Appointment> todaysAppointments = doctorService.getTodaysAppointments(doctor);
            model.addAttribute("todaysAppointments", todaysAppointments != null ? todaysAppointments : new ArrayList<>());

            List<Appointment> pendingAppointments = doctorService.getPendingAppointments(doctor);
            model.addAttribute("pendingAppointments", pendingAppointments != null ? pendingAppointments : new ArrayList<>());

            List<Appointment> notifications = doctorService.getNotifications(doctor);
            model.addAttribute("notifications", notifications != null ? notifications : new ArrayList<>());

            return "doctor/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            // Log the error and redirect to an error page
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/appointments")
    public String appointments(Model model, Authentication authentication) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
            if (doctor == null) {
                return "redirect:/auth/login?error=doctor_not_found";
            }

            model.addAttribute("doctor", doctor);

            List<Appointment> todaysAppointments = doctorService.getTodaysAppointments(doctor);
            model.addAttribute("todaysAppointments", todaysAppointments != null ? todaysAppointments : new ArrayList<>());

            return "doctor/appointments";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/calendar/weekly")
    public String weeklyCalendar(Model model, Authentication authentication) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
            if (doctor == null) {
                return "redirect:/auth/login?error=doctor_not_found";
            }

            Map<LocalDate, List<Appointment>> calendar = doctorService.getWeeklyCalendar(doctor);
            if (calendar == null) {
                calendar = new HashMap<>();
            }

            model.addAttribute("doctor", doctor);
            model.addAttribute("calendar", calendar);
            model.addAttribute("view", "weekly");
            return "doctor/calendar";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/calendar/monthly")
    public String monthlyCalendar(Model model, Authentication authentication) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
            if (doctor == null) {
                return "redirect:/auth/login?error=doctor_not_found";
            }

            Map<LocalDate, List<Appointment>> calendar = doctorService.getMonthlyCalendar(doctor);
            if (calendar == null) {
                calendar = new HashMap<>();
            }

            model.addAttribute("doctor", doctor);
            model.addAttribute("calendar", calendar);
            model.addAttribute("view", "monthly");
            return "doctor/calendar";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/patients/{patientId}")
    public String patientDetails(@PathVariable Long patientId, Model model, Authentication authentication) {
        try {
            // First, ensure the doctor is authenticated
            Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
            if (doctor == null) {
                return "redirect:/auth/login?error=doctor_not_found";
            }

            // Get patient details
            Patient patient = doctorService.getPatientDetails(patientId);
            model.addAttribute("patient", patient);

            // If the patient exists, try to load their appointments
            if (patient != null) {
                // In a real application, you would load the patient's appointments
                // Here we're just creating an empty list as a placeholder
                List<Appointment> appointments = new ArrayList<>();
                // This would be replaced with an actual repository call like:
                // List<Appointment> appointments = appointmentRepository.findByPatient(patient);
                model.addAttribute("appointments", appointments);
            }

            return "doctor/patient-details";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while loading patient details: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/appointments/{appointmentId}/status")
    public String updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestParam AppointmentStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            Appointment appointment = doctorService.updateAppointmentStatus(appointmentId, status);

            if (appointment != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Appointment status updated to " + status.toString());
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update appointment status");
            }

            return "redirect:/doctor/appointments";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            return "redirect:/doctor/appointments";
        }
    }

    @GetMapping("/availability")
    public String manageAvailability(Model model, Authentication authentication) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
            if (doctor == null) {
                return "redirect:/auth/login?error=doctor_not_found";
            }

            model.addAttribute("doctor", doctor);
            return "doctor/availability";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/availability")
    public String setAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endTime,
            @RequestParam boolean available,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
            if (doctor == null) {
                return "redirect:/auth/login?error=doctor_not_found";
            }

            doctorService.setAvailability(doctor, startTime, endTime, available);

            redirectAttributes.addFlashAttribute("success", "Availability updated successfully");
            return "redirect:/doctor/availability";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            return "redirect:/doctor/availability";
        }
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        try {
            Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
            if (doctor == null) {
                return "redirect:/auth/login?error=doctor_not_found";
            }

            model.addAttribute("doctor", doctor);
            return "doctor/profile";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "error";
        }
    }
}