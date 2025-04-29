package com.example.healthflow.controller;

import com.example.healthflow.service.DoctorProfileFixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/fix")
public class AdminFixController {

    @Autowired
    private DoctorProfileFixService doctorProfileFixService;

    /**
     * Shows the doctor profile fix page
     */
    @GetMapping("/doctors")
    public String showDoctorFixPage(Model model) {
        List<String> doctorsWithoutProfiles = doctorProfileFixService.findDoctorUsersWithoutProfiles();
        model.addAttribute("doctorsWithoutProfiles", doctorsWithoutProfiles);
        return "admin/fix-doctors";
    }

    /**
     * REST API to list doctors without profiles
     */
    @GetMapping("/api/doctors/list")
    @ResponseBody
    public ResponseEntity<List<String>> listDoctorsWithoutProfiles() {
        List<String> doctorsWithoutProfiles = doctorProfileFixService.findDoctorUsersWithoutProfiles();
        return ResponseEntity.ok(doctorsWithoutProfiles);
    }

    /**
     * REST API to fix a specific doctor profile
     */
    @PostMapping("/api/doctors/{username}")
    @ResponseBody
    public ResponseEntity<?> fixDoctorProfile(@PathVariable String username) {
        boolean fixed = doctorProfileFixService.fixDoctorProfileByUsername(username);
        if (fixed) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Created doctor profile for: " + username));
        } else {
            return ResponseEntity.ok(Map.of("status", "skipped", "message", "Doctor profile already exists or user not found: " + username));
        }
    }

    /**
     * REST API to fix all doctor profiles
     */
    @PostMapping("/api/doctors/fix-all")
    @ResponseBody
    public ResponseEntity<?> fixAllDoctorProfiles() {
        int count = doctorProfileFixService.fixAllDoctorProfiles();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Fixed " + count + " doctor profiles",
                "count", count
        ));
    }
}