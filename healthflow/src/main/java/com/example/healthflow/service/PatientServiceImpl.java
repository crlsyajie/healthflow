package com.example.healthflow.service;

import com.example.healthflow.model.*;
import com.example.healthflow.repository.AppointmentRepository;
import com.example.healthflow.repository.DoctorRepository;
import com.example.healthflow.repository.PatientRepository;
import com.example.healthflow.repository.UserRepository;
import com.example.healthflow.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Patient getPatientByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        return patientRepository.findByUser(user).orElse(null);
    }

    @Override
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElse(null);
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public List<Appointment> getUpcomingAppointments(Patient patient) {
        LocalDateTime now = LocalDateTime.now();
        List<AppointmentStatus> excludedStatuses = Arrays.asList(
                AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED);
        return appointmentRepository.findByPatientAndAppointmentTimeAfterAndStatusNotInOrderByAppointmentTimeAsc(
                patient, now, excludedStatuses);
    }

    @Override
    public List<Appointment> getAppointmentHistory(Patient patient) {
        return appointmentRepository.findByPatientOrderByAppointmentTimeDesc(patient);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    @Override
    public List<String> getAllDepartments() {
        return doctorRepository.findAll().stream()
                .map(Doctor::getSpecialization)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.PENDING);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return false;
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return true;
    }

    @Override
    @Transactional
    public boolean rescheduleAppointment(Long appointmentId, Appointment updatedAppointment) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return false;
        }

        appointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
        appointment.setDurationMinutes(updatedAppointment.getDurationMinutes());
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        appointmentRepository.save(appointment);
        return true;
    }

    @Override
    @Transactional
    public Patient updateProfile(Patient patient) {
        try {
            log.info("Updating profile for patient ID: {}, name: {} {}",
                    patient.getId(), patient.getFirstName(), patient.getLastName());

            // Validate required fields
            if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
                log.error("First name is required");
                throw new IllegalArgumentException("First name is required");
            }

            if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
                log.error("Last name is required");
                throw new IllegalArgumentException("Last name is required");
            }

            if (patient.getDateOfBirth() == null) {
                log.error("Date of birth is required");
                throw new IllegalArgumentException("Date of birth is required");
            }

            // Save the patient
            Patient savedPatient = patientRepository.save(patient);
            log.info("Profile successfully updated for patient ID: {}", savedPatient.getId());
            return savedPatient;
        } catch (Exception e) {
            log.error("Error updating patient profile: {}", e.getMessage(), e);
            throw e; // Rethrow to be handled by the controller
        }
    }

    @Override
    @Transactional
    public boolean deletePatient(Long patientId) {
        // Use Optional pattern for safer null handling
        return patientRepository.findById(patientId)
                .map(patient -> {
                    try {
                        // Get the user reference before deletion
                        User user = patient.getUser();

                        // Delete all appointments for this patient
                        List<Appointment> appointments = appointmentRepository.findByPatient(patient);
                        for (Appointment appointment : appointments) {
                            // Delete notifications first
                            notificationRepository.deleteByAppointmentId(appointment.getId());
                            // Then delete the appointment
                            appointmentRepository.delete(appointment);
                        }

                        // Delete the patient record
                        patientRepository.delete(patient);

                        // Finally delete the user if it exists
                        if (user != null) {
                            userRepository.deleteById(user.getId());
                        }

                        return true;
                    } catch (Exception e) {
                        log.error("Error while deleting patient {}: {}", patientId, e.getMessage());
                        return false;
                    }
                })
                .orElseGet(() -> {
                    log.warn("Patient not found with ID: {}", patientId);
                    return false;
                });
    }
}