package com.example.healthflow.service;

import com.example.healthflow.model.*;
import com.example.healthflow.repository.AppointmentRepository;
import com.example.healthflow.repository.DoctorRepository;
import com.example.healthflow.repository.PatientRepository;
import com.example.healthflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public Patient getPatientByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return patientRepository.findByUser(user);
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
                AppointmentStatus.CANCELED, AppointmentStatus.COMPLETED);
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
    public List<String> getAllDepartments() {
        return doctorRepository.findAll().stream()
                .map(Doctor::getSpecialization)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public boolean cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return false;
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
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
        return patientRepository.save(patient);
    }
}