package com.example.healthflow.controller;

import com.example.healthflow.model.Role;
import com.example.healthflow.model.User;
import com.example.healthflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "home";
    }
    
    /**
     * Alternative combined login and booking page accessible from root URL
     */
    @GetMapping("/loginbook")
    public String combinedLoginBooking() {
        return "combined-login-booking";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null) {
            User user = userService.findByUsername(authentication.getName());

            if (user != null) {
                Role role = user.getRole();

                switch (role) {
                    case PATIENT:
                        return "redirect:/patient/dashboard";
                    case DOCTOR:
                        return "redirect:/doctor/dashboard";
                    case ADMIN:
                        return "redirect:/admin/dashboard";
                }
            }
        }

        return "redirect:/auth/login";
    }
}