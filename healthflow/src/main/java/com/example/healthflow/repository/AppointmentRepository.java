package com.example.healthflow.repository;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.AppointmentStatus;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // Time-based queries
    List<Appointment> findByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Appointment> findByAppointmentTimeAfter(LocalDateTime dateTime);
    List<Appointment> findByAppointmentTimeBefore(LocalDateTime dateTime);

    // Status-based queries
    List<Appointment> findByStatus(AppointmentStatus status);

    // Entity-based queries
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByDoctorAndStatus(Doctor doctor, AppointmentStatus status);
    List<Appointment> findByDoctorAndAppointmentTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByPatientAndStatus(Patient patient, AppointmentStatus status);
    List<Appointment> findByDoctorIn(List<Doctor> doctors);
    
    // Follow-up related queries
    List<Appointment> findByParentAppointment(Appointment parentAppointment);
    List<Appointment> findByIsFollowUpTrue();

    // Recent appointments
    List<Appointment> findTop5ByOrderByCreatedAtDesc();

    // Doctor's appointments for a specific day
    @Query("SELECT a FROM Appointment a WHERE a.doctor = ?1 AND a.appointmentTime >= ?2 AND a.appointmentTime < ?3 ORDER BY a.appointmentTime ASC")
    List<Appointment> findDoctorAppointmentsForDay(Doctor doctor, LocalDateTime dayStart, LocalDateTime dayEnd);

    // Find first appointment by doctor and exact appointment time
    Appointment findFirstByDoctorAndAppointmentTime(Doctor doctor, LocalDateTime appointmentTime);

    // Methods needed by PatientServiceImpl
    List<Appointment> findByPatientAndAppointmentTimeAfterAndStatusNotInOrderByAppointmentTimeAsc(
            Patient patient, LocalDateTime dateTime, List<AppointmentStatus> statuses);

    List<Appointment> findByPatientOrderByAppointmentTimeDesc(Patient patient);
}