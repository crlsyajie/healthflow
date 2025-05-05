package com.example.healthflow.service;

import com.example.healthflow.model.User;
import com.example.healthflow.model.Role;

import java.util.List;

public interface UserService {
    User findByUsername(String username);
    User registerNewUser(User user, Role role);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);

    // New methods for admin user management
    List<User> findAll();
    List<User> findAllByRole(Role role);
    User findById(Long id);
    User saveUser(User user);
    boolean deleteUser(Long id);
}