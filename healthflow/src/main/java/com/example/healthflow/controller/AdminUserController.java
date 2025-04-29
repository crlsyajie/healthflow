package com.example.healthflow.controller;

import com.example.healthflow.model.Role;
import com.example.healthflow.model.User;
import com.example.healthflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String getUsers(@RequestParam(required = false) String username,
                           @RequestParam(required = false) Role role,
                           Model model) {

        List<User> users;

        if (username != null && !username.isEmpty()) {
            // Filter by username
            User user = userService.findByUsername(username);
            users = user != null ? List.of(user) : List.of();
        } else if (role != null) {
            // Filter by role
            users = userService.findAllByRole(role);
        } else {
            // No filters, get all users
            users = userService.findAll();
        }

        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam Role role,
                          @RequestParam(required = false) Boolean enabled,
                          RedirectAttributes redirectAttributes) {

        try {
            // Check if username already exists
            if (userService.findByUsername(username) != null) {
                redirectAttributes.addFlashAttribute("error", "Username already exists: " + username);
                return "redirect:/admin/users";
            }

            // Create new user
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setEnabled(enabled != null ? enabled : true);

            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("success", "User created successfully: " + username);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String email,
                             @RequestParam Role role,
                             @RequestParam(required = false) Boolean enabled,
                             RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findById(id);

            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found with ID: " + id);
                return "redirect:/admin/users";
            }

            // Check if username is changed and already exists
            if (!user.getUsername().equals(username) && userService.findByUsername(username) != null) {
                redirectAttributes.addFlashAttribute("error", "Username already exists: " + username);
                return "redirect:/admin/users";
            }

            // Update user details
            user.setUsername(username);
            user.setEmail(email);
            user.setRole(role);
            user.setEnabled(enabled != null ? enabled : false);

            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("success", "User updated successfully: " + username);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findById(id);

            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found with ID: " + id);
                return "redirect:/admin/users";
            }

            // Validate passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/admin/users";
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("success", "Password reset successfully for user: " + user.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error resetting password: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);

            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found with ID: " + id);
                return "redirect:/admin/users";
            }

            // Store username for success message
            String username = user.getUsername();

            // Delete user
            userService.deleteUser(id);

            redirectAttributes.addFlashAttribute("success", "User deleted successfully: " + username);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }
}