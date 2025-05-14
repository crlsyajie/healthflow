package com.example.healthflow.service;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.AppointmentStatus;
import com.example.healthflow.model.Department;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.DoctorAvailability;
import com.example.healthflow.model.Patient;
import com.example.healthflow.model.User;
import com.example.healthflow.repository.AppointmentRepository;
import com.example.healthflow.repository.DepartmentRepository;
import com.example.healthflow.repository.DoctorAvailabilityRepository;
import com.example.healthflow.repository.DoctorRepository;
import com.example.healthflow.repository.NotificationRepository;
import com.example.healthflow.repository.PatientRepository;
import com.example.healthflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Override
    public Doctor getDoctorByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        return doctorRepository.findByUser(user);
    }

    @Override
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentId(departmentId);
    }

    @Override
    @Transactional
    public boolean updateDoctorDepartment(Long doctorId, Long departmentId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        Department department = departmentRepository.findById(departmentId).orElse(null);

        if (doctor == null || department == null) {
            return false;
        }

        doctor.setDepartment(department);
        doctorRepository.save(doctor);
        return true;
    }

    @Override
    @Transactional
    public boolean removeDoctor(Long id) {
        try {
            Doctor doctor = doctorRepository.findById(id).orElse(null);
            if (doctor == null) {
                return false;
            }

            // Get all appointments for this doctor
            List<Appointment> allAppointments = appointmentRepository.findByDoctor(doctor);

            // Check if doctor has any pending appointments
            boolean hasPendingAppointments = allAppointments.stream()
                    .anyMatch(appointment -> appointment.getStatus() == AppointmentStatus.PENDING);

            if (hasPendingAppointments) {
                return false; // Can't remove doctor with pending appointments
            }

            // Delete all notifications for appointments first
            for (Appointment appointment : allAppointments) {
                notificationRepository.deleteByAppointmentId(appointment.getId());
            }

            // Delete all appointments
            appointmentRepository.deleteAll(allAppointments);

            // Get the user associated with the doctor before deleting the doctor
            User user = doctor.getUser();

            // Delete the doctor record
            doctorRepository.delete(doctor);

            // Finally delete the associated user account
            if (user != null) {
                userRepository.deleteById(user.getId());
            }

            return true;
        } catch (Exception e) {
            log.error("Error removing doctor {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<Appointment> getTodaysAppointments(Doctor doctor) {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);
        return appointmentRepository.findDoctorAppointmentsForDay(doctor, today, tomorrow);
    }

    @Override
    public List<Appointment> getAppointmentsForPeriod(Doctor doctor, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        return appointmentRepository.findByDoctorAndAppointmentTimeBetween(doctor, startDateTime, endDateTime);
    }

    @Override
    public List<Appointment> getPendingAppointments(Doctor doctor) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByDoctorAndAppointmentTimeBetween(doctor, now, now.plusMonths(1)).stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.PENDING)
                .collect(Collectors.toList());
    }

    @Override
    public Patient getPatientDetails(Long patientId) {
        return patientRepository.findById(patientId).orElse(null);
    }

    @Override
    @Transactional
    public Appointment updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment != null) {
            appointment.setStatus(status);
            return appointmentRepository.save(appointment);
        }
        return null;
    }

    @Override
    public Map<LocalDate, List<Appointment>> getWeeklyCalendar(Doctor doctor) {
        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = today.plusDays(6); // Get a week from today

        return getCalendarMap(doctor, today, endOfWeek);
    }

    @Override
    public Map<LocalDate, List<Appointment>> getMonthlyCalendar(Doctor doctor) {
        LocalDate today = LocalDate.now();
        LocalDate endOfMonth = today.plusMonths(1);

        return getCalendarMap(doctor, today, endOfMonth);
    }

    private Map<LocalDate, List<Appointment>> getCalendarMap(Doctor doctor, LocalDate start, LocalDate end) {
        List<Appointment> appointments = getAppointmentsForPeriod(doctor, start, end);

        Map<LocalDate, List<Appointment>> calendar = new TreeMap<>();

        // Initialize all dates in the range with empty lists
        LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            calendar.put(currentDate, new ArrayList<>());
            currentDate = currentDate.plusDays(1);
        }

        // Fill the calendar with appointments
        for (Appointment appointment : appointments) {
            LocalDate appointmentDate = appointment.getAppointmentTime().toLocalDate();
            calendar.get(appointmentDate).add(appointment);
        }

        return calendar;
    }

    @Override
    @Transactional
    public void setAvailability(Doctor doctor, LocalDateTime startTime, LocalDateTime endTime, boolean available) {
        if (available) {
            // Check for any existing appointments in this time slot
            List<Appointment> existingAppointments = appointmentRepository.findByDoctorAndAppointmentTimeBetween(
                    doctor, startTime, endTime);

            if (!existingAppointments.isEmpty()) {
                throw new IllegalStateException("Cannot set availability: There are existing appointments in this time slot");
            }

            // Update doctor's working hours
            doctor.setWorkingHoursStart(startTime);
            doctor.setWorkingHoursEnd(endTime);
            doctorRepository.save(doctor);
        } else {
            // If setting as unavailable, we don't need to store the time range
            // Just ensure no new appointments can be made during this time
            List<Appointment> existingAppointments = appointmentRepository.findByDoctorAndAppointmentTimeBetween(
                    doctor, startTime, endTime);

            if (!existingAppointments.isEmpty()) {
                throw new IllegalStateException("Cannot set unavailability: There are existing appointments in this time slot");
            }
        }
    }

    @Override
    public List<Appointment> getNotifications(Doctor doctor) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        // Get recent appointments
        return appointmentRepository.findByDoctorAndAppointmentTimeBetween(doctor, yesterday, LocalDateTime.now().plusMonths(1))
                .stream()
                .filter(appointment -> {
                    // New bookings created within the last 24 hours
                    if (appointment.getCreatedAt() != null &&
                            appointment.getCreatedAt().isAfter(yesterday) &&
                            appointment.getStatus() == AppointmentStatus.PENDING) {
                        return true;
                    }

                    // Recently canceled appointments
                    if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                        return true;
                    }

                    return false;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Doctor updateDoctorInfo(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public Doctor updateDoctorProfile(Doctor doctor, String profileImage) {
        doctor.setProfileImage(profileImage);
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public Doctor updateResidentStatus(Long doctorId, String isResident) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return null;
        }
        
        doctor.setIsResident(isResident);
        return doctorRepository.save(doctor);
    }
    
    @Override
    public List<Doctor> getResidentDoctors() {
        return doctorRepository.findAll().stream()
                .filter(doctor -> "yes".equalsIgnoreCase(doctor.getIsResident()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Doctor> getNonResidentDoctors() {
        return doctorRepository.findAll().stream()
                .filter(doctor -> !"yes".equalsIgnoreCase(doctor.getIsResident()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Doctor> getPotentialSupervisors() {
        // Return doctors who are not residents and can supervise
        return doctorRepository.findAll().stream()
                .filter(doctor -> !"yes".equalsIgnoreCase(doctor.getIsResident()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorAvailability> getDoctorAvailability(Doctor doctor) {
        return doctorAvailabilityRepository.findByDoctor(doctor);
    }

    @Override
    @Transactional
    public DoctorAvailability setDoctorAvailability(Doctor doctor, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        // Check if there's already an availability for this day
        List<DoctorAvailability> existingAvailability = doctorAvailabilityRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek);

        DoctorAvailability availability;
        if (!existingAvailability.isEmpty()) {
            // Update existing availability
            availability = existingAvailability.get(0);
            availability.setStartTime(startTime);
            availability.setEndTime(endTime);
        } else {
            // Create new availability
            availability = new DoctorAvailability();
            availability.setDoctor(doctor);
            availability.setDayOfWeek(dayOfWeek);
            availability.setStartTime(startTime);
            availability.setEndTime(endTime);
        }

        return doctorAvailabilityRepository.save(availability);
    }

    @Override
    @Transactional
    public void deleteDoctorAvailability(Long availabilityId) {
        doctorAvailabilityRepository.deleteById(availabilityId);
    }

    @Override
    public boolean isDoctorAvailable(Doctor doctor, LocalDateTime appointmentTime, int durationMinutes) {
        // Get day of week for the appointment
        DayOfWeek dayOfWeek = appointmentTime.getDayOfWeek();

        // Get time for the appointment
        LocalTime timeOfDay = appointmentTime.toLocalTime();

        // Get availability for this day
        List<DoctorAvailability> dayAvailability = doctorAvailabilityRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek);

        // If no availability is set for this day, doctor is not available
        if (dayAvailability.isEmpty()) {
            return false;
        }

        // Check if appointment time falls within doctor's availability
        DoctorAvailability availability = dayAvailability.get(0);
        LocalTime startTime = availability.getStartTime();
        LocalTime endTime = availability.getEndTime();

        // Appointment end time
        LocalTime appointmentEndTime = timeOfDay.plusMinutes(durationMinutes);

        // Check if appointment fits within availability window
        if (timeOfDay.isBefore(startTime) || appointmentEndTime.isAfter(endTime)) {
            return false;
        }

        // Check if there are existing appointments that would conflict
        LocalDateTime appointmentEndDateTime = appointmentTime.plusMinutes(durationMinutes);
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorAndAppointmentTimeBetween(
                doctor, appointmentTime, appointmentEndDateTime);

        // If there are existing appointments in this time slot, doctor is not available
        return existingAppointments.isEmpty();
    }
}