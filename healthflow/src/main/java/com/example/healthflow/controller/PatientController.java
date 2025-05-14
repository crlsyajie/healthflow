package com.example.healthflow.controller;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.Patient;
import com.example.healthflow.model.User;
import com.example.healthflow.service.AppointmentService;
import com.example.healthflow.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;
    
    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        Patient patient = patientService.getPatientByUsername(authentication.getName());
        model.addAttribute("patient", patient);
        model.addAttribute("upcomingAppointments", patientService.getUpcomingAppointments(patient));
        return "patient/dashboard";
    }

    @GetMapping("/doctors")
    public String doctorList(Model model) {
        model.addAttribute("doctors", patientService.getAllDoctors());
        model.addAttribute("departments", patientService.getAllDepartments());
        return "patient/doctors";
    }

    @GetMapping("/appointments")
    public String appointmentHistory(Model model, Authentication authentication) {
        Patient patient = patientService.getPatientByUsername(authentication.getName());
        model.addAttribute("appointments", patientService.getAppointmentHistory(patient));
        return "patient/appointments";
    }
    
    @GetMapping("/appointments/{id}")
    public String viewAppointmentDetail(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        Patient patient = patientService.getPatientByUsername(authentication.getName());
        Appointment appointment = appointmentService.getAppointmentById(id);
        
        // Verify appointment belongs to the logged-in patient
        if (appointment == null || !appointment.getPatient().getId().equals(patient.getId())) {
            redirectAttributes.addFlashAttribute("error", "Appointment not found or access denied");
            return "redirect:/patient/appointments";
        }
        
        // Get any follow-up appointments for this appointment
        List<Appointment> followUps = appointmentService.getFollowUpAppointments(id);
        
        model.addAttribute("appointment", appointment);
        model.addAttribute("followUps", followUps);
        return "patient/appointment-detail";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        Patient patient = patientService.getPatientByUsername(authentication.getName());
        model.addAttribute("patient", patient);
        return "patient/profile";
    }
}