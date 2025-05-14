package com.example.healthflow.service;

import com.example.healthflow.model.*;
import com.example.healthflow.repository.PatientRepository;
import com.example.healthflow.repository.UserRepository;
import com.example.healthflow.repository.AppointmentRepository;
import com.example.healthflow.repository.DoctorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PublicBookingServiceImpl implements PublicBookingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public Appointment bookAppointmentForGuest(
            String firstName,
            String lastName,
            String email,
            String phone,
            String dateOfBirth,
            String gender,
            Long doctorId,
            String appointmentDateTime,
            String reason,
            String notes) {

        log.info("Processing guest appointment booking for {} {} with doctor ID {}", firstName, lastName, doctorId);

        try {
            // Check if user already exists with this email
            User user = userRepository.findByEmail(email);
            Patient patient;

            if (user == null) {
                // Create new user account with generated credentials
                String username = generateUsername(firstName, lastName);
                String password = generateRandomPassword();

                user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(Role.PATIENT);
                user.setEnabled(true);
                user = userRepository.save(user);

                // Create new patient profile
                patient = new Patient();
                patient.setUser(user);
                patient.setFirstName(firstName);
                patient.setLastName(lastName);
                patient.setPhoneNumber(phone);
                patient.setDateOfBirth(LocalDate.parse(dateOfBirth));
                patient.setGender(Gender.valueOf(gender));
                patient = patientRepository.save(patient);

                // Send credentials by email
                emailService.sendAccountCredentials(email, username, password);
                log.info("Created new patient account for {} {}", firstName, lastName);
            } else {
                // Use existing patient account
                patient = patientRepository.findByUser(user).orElse(null);
                if (patient == null) {
                    log.error("User exists but patient profile not found for email: {}", email);
                    throw new RuntimeException("Patient profile not found for existing user");
                }
                log.info("Using existing patient account for {} {}", firstName, lastName);
            }

            // Find doctor
            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            if (doctor == null) {
                log.error("Doctor not found with ID: {}", doctorId);
                throw new RuntimeException("Doctor not found");
            }

            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentTime(LocalDateTime.parse(appointmentDateTime));
            appointment.setDurationMinutes(30); // Default duration
            appointment.setStatus(AppointmentStatus.PENDING);
            appointment.setReason(reason);
            appointment.setNotes(notes);

            appointment = appointmentRepository.save(appointment);
            log.info("Appointment successfully booked with ID: {}", appointment.getId());
            
            return appointment;

        } catch (Exception e) {
            log.error("Error booking appointment for guest: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to book appointment: " + e.getMessage());
        }
    }

    /**
     * Generate a username based on first and last name
     */
    private String generateUsername(String firstName, String lastName) {
        String baseUsername = (firstName.toLowerCase().charAt(0) + lastName.toLowerCase()).replaceAll("[^a-z0-9]", "");
        String username = baseUsername;
        int counter = 1;

        // Check if username is already taken
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    /**
     * Generate a random password with 8 characters
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
} 