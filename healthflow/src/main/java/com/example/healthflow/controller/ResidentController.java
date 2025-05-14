package com.example.healthflow.controller;

import com.example.healthflow.model.Doctor;
import com.example.healthflow.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/residents")
@PreAuthorize("hasRole('ADMIN')")
public class ResidentController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public String listResidents(Model model) {
        List<Doctor> residents = doctorService.getResidentDoctors();
        model.addAttribute("residents", residents);
        return "admin/residents/list";
    }

    @GetMapping("/create")
    public String createResidentForm(Model model) {
        // Get doctors who are not yet residents
        List<Doctor> doctors = doctorService.getNonResidentDoctors();
        
        model.addAttribute("doctors", doctors);
        return "admin/residents/form";
    }

    @PostMapping("/create")
    public String createResident(
            @RequestParam Long doctorId,
            RedirectAttributes redirectAttributes) {
        
        try {
            Doctor doctor = doctorService.getDoctorById(doctorId);
            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/admin/residents/create";
            }
            
            // Update doctor's resident status to yes
            doctorService.updateResidentStatus(doctorId, "yes");
            
            redirectAttributes.addFlashAttribute("success", "Resident status updated successfully");
            return "redirect:/admin/residents";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating resident status: " + e.getMessage());
            return "redirect:/admin/residents/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editResidentForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        if (doctor == null || !"yes".equalsIgnoreCase(doctor.getIsResident())) {
            return "redirect:/admin/residents";
        }
        
        model.addAttribute("doctor", doctor);
        return "admin/residents/edit";
    }

    @PostMapping("/remove/{id}")
    public String removeResidentStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/admin/residents";
            }
            
            // Update doctor's resident status to no
            doctorService.updateResidentStatus(id, "no");
            
            redirectAttributes.addFlashAttribute("success", "Resident status removed successfully");
            return "redirect:/admin/residents";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error removing resident status: " + e.getMessage());
            return "redirect:/admin/residents";
        }
    }
} 