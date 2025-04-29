package com.example.healthflow.service;

import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Patient;
import com.example.healthflow.model.Appointment;

import java.util.List;
import java.util.Set;

public interface PatientService {
    List<Patient> getAllPatients();
    Patient getPatientById(Long id);
    Patient getPatientByUsername(String username);
    List<Appointment> getUpcomingAppointments(Patient patient);
    List<Appointment> getAppointmentHistory(Patient patient);
    List<Doctor> getAllDoctors();
    List<String> getAllDepartments();
    Appointment bookAppointment(Appointment appointment);
    boolean cancelAppointment(Long appointmentId);
    boolean rescheduleAppointment(Long appointmentId, Appointment updatedAppointment);
    Patient updateProfile(Patient patient);
}