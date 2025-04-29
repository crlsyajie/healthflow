package com.example.healthflow.service;

import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Role;
import com.example.healthflow.model.User;
import com.example.healthflow.repository.DoctorRepository;
import com.example.healthflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorProfileFixService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Finds all users with DOCTOR role that don't have a doctor profile
     * @return List of usernames
     */
    public List<String> findDoctorUsersWithoutProfiles() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.DOCTOR)
                .filter(user -> doctorRepository.findByUser(user) == null)
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

    /**
     * Fixes all doctor users without profiles
     * @return Number of profiles created
     */
    @Transactional
    public int fixAllDoctorProfiles() {
        List<User> doctorUsersWithoutProfiles = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.DOCTOR)
                .filter(user -> doctorRepository.findByUser(user) == null)
                .collect(Collectors.toList());

        int count = 0;
        for (User doctorUser : doctorUsersWithoutProfiles) {
            if (createDoctorProfile(doctorUser)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Fixes a specific doctor user's profile
     * @param username Username of the doctor
     * @return true if fixed, false if not needed or failed
     */
    @Transactional
    public boolean fixDoctorProfileByUsername(String username) {
        User doctorUser = userRepository.findByUsername(username);
        if (doctorUser == null || doctorUser.getRole() != Role.DOCTOR) {
            return false;
        }

        Doctor existingDoctor = doctorRepository.findByUser(doctorUser);
        if (existingDoctor != null) {
            return false; // Already has a profile
        }

        return createDoctorProfile(doctorUser);
    }

    /**
     * Creates a doctor profile for a user that has the DOCTOR role but no profile
     * @param doctorUser The user with DOCTOR role
     * @return true if profile was created, false otherwise
     */
    private boolean createDoctorProfile(User doctorUser) {
        if (doctorUser == null || doctorUser.getRole() != Role.DOCTOR) {
            return false;
        }

        Doctor doctor = new Doctor();
        doctor.setUser(doctorUser);

        // Extract first/last name from username if possible, or use defaults
        String username = doctorUser.getUsername();
        if (username.contains(".")) {
            String[] parts = username.split("\\.", 2);
            doctor.setFirstName(capitalizeFirstLetter(parts[0]));
            doctor.setLastName(capitalizeFirstLetter(parts[1]));
        } else {
            doctor.setFirstName("Doctor");
            doctor.setLastName(username);
        }

        // Generate a unique license number
        doctor.setLicenseNumber("DR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Set default values for other fields
        doctor.setSpecialization("General Medicine");
        doctor.setPhoneNumber("000-000-0000");
        doctor.setBio("Doctor profile automatically created by system fix.");

        // Save the new doctor profile
        doctorRepository.save(doctor);
        return true;
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}