package com.example.healthflow.controller;

import com.example.healthflow.model.Patient;
import com.example.healthflow.model.Role;
import com.example.healthflow.model.User;
import com.example.healthflow.repository.PatientRepository;
import com.example.healthflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, BindingResult result) {
        if (!userService.isUsernameAvailable(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
        }
        if (!userService.isEmailAvailable(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
        }
        if (result.hasErrors()) {
            return "register";
        }

        User savedUser = userService.registerNewUser(user, Role.PATIENT);

        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setFirstName("New");
        patient.setLastName("User");
        patient.setDateOfBirth(LocalDate.now());
        patientRepository.save(patient);

        return "redirect:/auth/login?success";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // After successful login, users will be redirected based on their role
    // This is handled by the AuthenticationSuccessHandler in SecurityConfig
}