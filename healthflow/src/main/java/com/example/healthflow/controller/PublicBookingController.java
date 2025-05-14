package com.example.healthflow.controller;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.service.DoctorService;
import com.example.healthflow.service.PublicBookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Controller
@RequestMapping("/book")
@Slf4j
public class PublicBookingController {

    @Autowired
    private PublicBookingService publicBookingService;
    
    @Autowired
    private DoctorService doctorService;
    
    /**
     * Display the public booking form with doctor list
     */
    @GetMapping
    public String showBookingForm(Model model) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);
        return "public/booking-form";
    }
    
    /**
     * Display the form to book an appointment with a specific doctor
     */
    @GetMapping("/doctor/{id}")
    public String showDoctorBookingForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/book";
            }
            
            model.addAttribute("doctor", doctor);
            return "public/doctor-booking-form";
        } catch (Exception e) {
            log.error("Error loading doctor booking form: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred. Please try again.");
            return "redirect:/book";
        }
    }
    
    /**
     * Handle the public appointment booking
     */
    @PostMapping("/submit")
    public String bookAppointment(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String dateOfBirth,
            @RequestParam String gender,
            @RequestParam Long doctorId,
            @RequestParam String appointmentDateTime,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        
        try {
            Appointment appointment = publicBookingService.bookAppointmentForGuest(
                    firstName, lastName, email, phone, dateOfBirth, gender,
                    doctorId, appointmentDateTime, reason, notes);
            
            redirectAttributes.addFlashAttribute("success", 
                    "Appointment booked successfully! Your login credentials have been sent to your email.");
            return "redirect:/book/success";
        } catch (Exception e) {
            log.error("Error booking appointment: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to book appointment: " + e.getMessage());
            return "redirect:/book/doctor/" + doctorId;
        }
    }
    
    /**
     * Show success page after booking
     */
    @GetMapping("/success")
    public String showSuccessPage() {
        return "public/booking-success";
    }
} 