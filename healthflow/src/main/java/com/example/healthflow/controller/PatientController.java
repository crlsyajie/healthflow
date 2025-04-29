package com.example.healthflow.controller;

import com.example.healthflow.model.Patient;
import com.example.healthflow.model.User;
import com.example.healthflow.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

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

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        Patient patient = patientService.getPatientByUsername(authentication.getName());
        model.addAttribute("patient", patient);
        return "patient/profile";
    }
}