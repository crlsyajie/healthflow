package com.example.healthflow.service;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.AppointmentStatus;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Patient;
import com.example.healthflow.model.DoctorAvailability;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface DoctorService {
    // Doctor retrieval methods
    Doctor getDoctorByUsername(String username);
    Doctor getDoctorById(Long id);
    List<Doctor> getAllDoctors();
    List<Doctor> getDoctorsByDepartment(Long departmentId);

    // Department management
    boolean updateDoctorDepartment(Long doctorId, Long departmentId);
    boolean removeDoctor(Long id);

    // Doctor's schedule management
    List<Appointment> getTodaysAppointments(Doctor doctor);
    List<Appointment> getAppointmentsForPeriod(Doctor doctor, LocalDate startDate, LocalDate endDate);
    List<Appointment> getPendingAppointments(Doctor doctor);
    Map<LocalDate, List<Appointment>> getWeeklyCalendar(Doctor doctor);
    Map<LocalDate, List<Appointment>> getMonthlyCalendar(Doctor doctor);
    void setAvailability(Doctor doctor, LocalDateTime startTime, LocalDateTime endTime, boolean available);

    // Doctor availability management
    List<DoctorAvailability> getDoctorAvailability(Doctor doctor);
    DoctorAvailability setDoctorAvailability(Doctor doctor, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);
    void deleteDoctorAvailability(Long availabilityId);
    boolean isDoctorAvailable(Doctor doctor, LocalDateTime appointmentTime, int durationMinutes);

    // Patient and appointment management
    Patient getPatientDetails(Long patientId);
    Appointment updateAppointmentStatus(Long appointmentId, AppointmentStatus status);
    List<Appointment> getNotifications(Doctor doctor);

    // Doctor profile management
    Doctor updateDoctorInfo(Doctor doctor);
} 