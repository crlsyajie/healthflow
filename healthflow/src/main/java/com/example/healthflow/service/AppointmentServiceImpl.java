package com.example.healthflow.service;

import com.example.healthflow.model.*;
import com.example.healthflow.repository.AppointmentRepository;
import com.example.healthflow.repository.DoctorRepository;
import com.example.healthflow.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Appointment saveAppointment(Appointment appointment) {
        Appointment saved = appointmentRepository.save(appointment);

        // Create notification for new appointment
        if (saved != null) {
            createAppointmentNotification(saved, Notification.NotificationType.APPOINTMENT_CREATED);
        }

        return saved;
    }

    @Override
    @Transactional
    public boolean deleteAppointment(Long id) {
        try {
            Appointment appointment = appointmentRepository.findById(id).orElse(null);
            if (appointment == null) {
                return false;
            }

            // First delete existing notifications for this appointment
            notificationRepository.deleteByAppointmentId(id);

            // Create cancellation notification before deleting
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.APPOINTMENT_CANCELLED);
            notification.setTitle("Appointment Canceled");
            notification.setMessage("Appointment with Dr. " + appointment.getDoctor().getLastName() +
                    " for " + appointment.getPatient().getFirstName() + " " +
                    appointment.getPatient().getLastName() + " on " +
                    appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                    " has been canceled");
            notification.setUser(appointment.getPatient().getUser());
            notificationRepository.save(notification);

            // Finally delete the appointment
            appointmentRepository.delete(appointment);
            return true;
        } catch (Exception e) {
            log.error("Error deleting appointment {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<Appointment> getTodayAppointments() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);
        return appointmentRepository.findByAppointmentTimeBetween(startOfDay, endOfDay);
    }

    @Override
    public List<Appointment> getPendingAppointments() {
        return appointmentRepository.findByStatus(AppointmentStatus.PENDING);
    }

    @Override
    public List<Appointment> getRecentAppointments(int limit) {
        return appointmentRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(Patient patient) {
        if (patient == null) {
            return Collections.emptyList();
        }
        return appointmentRepository.findByPatient(patient);
    }

    @Override
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);
        return appointmentRepository.findByAppointmentTimeBetween(startOfDay, endOfDay);
    }

    @Override
    public List<Appointment> getAppointmentsByDepartment(Long departmentId) {
        List<Doctor> doctors = doctorRepository.findByDepartmentId(departmentId);
        if (doctors.isEmpty()) {
            return Collections.emptyList();
        }

        return appointmentRepository.findByDoctorIn(doctors);
    }

    @Override
    @Transactional
    public boolean updateAppointmentStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment == null) {
            return false;
        }

        appointment.setStatus(status);
        appointmentRepository.save(appointment);

        // Create notification for status update
        createAppointmentNotification(appointment, Notification.NotificationType.APPOINTMENT_UPDATED);

        return true;
    }

    @Override
    public Map<LocalDate, Integer> getDailyAppointmentCountForLastDays(int days) {
        Map<LocalDate, Integer> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // Initialize all days with 0 appointments
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            result.put(date, 0);
        }

        // Query appointments for the date range
        LocalDateTime start = today.minusDays(days - 1).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<Appointment> appointments = appointmentRepository.findByAppointmentTimeBetween(start, end);

        // Count appointments per day
        for (Appointment appointment : appointments) {
            LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();
            result.put(appointmentDate, result.getOrDefault(appointmentDate, 0) + 1);
        }

        return result;
    }

    @Override
    public Map<String, Integer> getMonthlyAppointmentCount(int months) {
        Map<String, Integer> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // Initialize all months with 0 appointments
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = today.minusMonths(i);
            String monthKey = date.format(formatter);
            result.put(monthKey, 0);
        }

        // Query appointments for the date range
        LocalDateTime start = today.minusMonths(months - 1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        List<Appointment> appointments = appointmentRepository.findByAppointmentTimeBetween(start, end);

        // Count appointments per month
        for (Appointment appointment : appointments) {
            String monthKey = appointment.getAppointmentTime().format(formatter);
            result.put(monthKey, result.getOrDefault(monthKey, 0) + 1);
        }

        return result;
    }

    @Override
    public Map<AppointmentStatus, Integer> getAppointmentStatusDistribution() {
        Map<AppointmentStatus, Integer> result = new HashMap<>();

        // Initialize all statuses with 0 appointments
        for (AppointmentStatus status : AppointmentStatus.values()) {
            result.put(status, 0);
        }

        // Count appointments per status
        List<Appointment> appointments = appointmentRepository.findAll();
        for (Appointment appointment : appointments) {
            AppointmentStatus status = appointment.getStatus();
            result.put(status, result.getOrDefault(status, 0) + 1);
        }

        return result;
    }

    @Override
    public Map<String, Integer> getAppointmentCountPerDoctor() {
        Map<String, Integer> result = new HashMap<>();
        List<Appointment> appointments = appointmentRepository.findAll();

        for (Appointment appointment : appointments) {
            Doctor doctor = appointment.getDoctor();
            String doctorName = doctor.getFirstName() + " " + doctor.getLastName();
            result.put(doctorName, result.getOrDefault(doctorName, 0) + 1);
        }

        return result;
    }

    @Override
    public Map<String, Double> getAverageAppointmentDurationPerDoctor() {
        Map<String, Double> result = new HashMap<>();
        Map<String, List<Integer>> durationsByDoctor = new HashMap<>();

        List<Appointment> appointments = appointmentRepository.findAll();

        for (Appointment appointment : appointments) {
            Doctor doctor = appointment.getDoctor();
            String doctorName = doctor.getFirstName() + " " + doctor.getLastName();

            int durationMinutes = appointment.getDurationMinutes();

            if (!durationsByDoctor.containsKey(doctorName)) {
                durationsByDoctor.put(doctorName, new ArrayList<>());
            }
            durationsByDoctor.get(doctorName).add(durationMinutes);
        }

        // Calculate averages
        for (Map.Entry<String, List<Integer>> entry : durationsByDoctor.entrySet()) {
            double average = entry.getValue().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            result.put(entry.getKey(), average);
        }

        return result;
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    @Transactional
    public boolean markNotificationAsRead(Long id) {
        int updated = notificationRepository.markAsRead(id);
        return updated > 0;
    }

    @Override
    @Transactional
    public int markAllNotificationsAsRead() {
        return notificationRepository.markAllAsRead();
    }

    /**
     * Helper method to create notifications for appointment events
     */
    private void createAppointmentNotification(Appointment appointment, Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setType(type);

        // Set the user to the patient's user for all appointment notifications
        notification.setUser(appointment.getPatient().getUser());

        // Different titles and messages based on notification type
        switch (type) {
            case APPOINTMENT_CREATED:
                notification.setTitle("New Appointment Created");
                notification.setMessage("A new appointment has been scheduled with Dr. " +
                        appointment.getDoctor().getLastName() + " for " +
                        appointment.getPatient().getFirstName() + " " +
                        appointment.getPatient().getLastName() + " on " +
                        appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                break;
            case APPOINTMENT_UPDATED:
                notification.setTitle("Appointment Updated");
                notification.setMessage("Appointment status updated to " + appointment.getStatus() +
                        " for " + appointment.getPatient().getFirstName() + " " +
                        appointment.getPatient().getLastName() + " with Dr. " +
                        appointment.getDoctor().getLastName());
                break;
            case APPOINTMENT_CANCELLED:
                notification.setTitle("Appointment Canceled");
                notification.setMessage("Appointment with Dr. " + appointment.getDoctor().getLastName() +
                        " for " + appointment.getPatient().getFirstName() + " " +
                        appointment.getPatient().getLastName() + " on " +
                        appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                        " has been canceled");
                break;
            default:
                notification.setTitle("Appointment Notification");
                notification.setMessage("Notification regarding appointment with Dr. " +
                        appointment.getDoctor().getLastName() + " for " +
                        appointment.getPatient().getFirstName() + " " +
                        appointment.getPatient().getLastName());
        }

        notificationRepository.save(notification);
    }
} 