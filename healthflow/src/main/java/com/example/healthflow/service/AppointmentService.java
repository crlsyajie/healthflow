package com.example.healthflow.service;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.AppointmentStatus;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Notification;
import com.example.healthflow.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    // Basic CRUD operations
    List<Appointment> getAllAppointments();
    Appointment getAppointmentById(Long id);
    Appointment saveAppointment(Appointment appointment);
    boolean deleteAppointment(Long id);

    // Filtered queries
    List<Appointment> getTodayAppointments();
    List<Appointment> getPendingAppointments();
    List<Appointment> getCompletedAppointments();
    List<Appointment> getCancelledAppointments();
    List<Appointment> getRecentAppointments(int limit);
    List<Appointment> getAppointmentsByDoctor(Doctor doctor);
    List<Appointment> getAppointmentsByPatient(Patient patient);
    List<Appointment> getAppointmentsByDate(LocalDate date);
    List<Appointment> getAppointmentsByDepartment(Long departmentId);

    // Status management
    boolean updateAppointmentStatus(Long id, AppointmentStatus status);

    // Analytics
    Map<LocalDate, Integer> getDailyAppointmentCountForLastDays(int days);
    Map<String, Integer> getMonthlyAppointmentCount(int months);
    Map<AppointmentStatus, Integer> getAppointmentStatusDistribution();
    Map<String, Integer> getAppointmentCountPerDoctor();
    Map<String, Double> getAverageAppointmentDurationPerDoctor();

    // Notifications
    List<Notification> getAllNotifications();
    boolean markNotificationAsRead(Long id);
    int markAllNotificationsAsRead();
    
    // Follow-up appointment methods
    Appointment createFollowUpAppointment(Long parentAppointmentId, LocalDateTime followUpTime, 
                                         Integer durationMinutes, String reason, String notes);
    List<Appointment> getFollowUpAppointments(Long parentAppointmentId);
    List<Appointment> createScheduledFollowUps(Long appointmentId, int[] intervalDays, 
                                              String reason, Integer durationMinutes);
}