package com.example.healthflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "doctors")
@Data
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String bio;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @Column(name = "working_hours_start")
    private LocalDateTime workingHoursStart;

    @Column(name = "working_hours_end")
    private LocalDateTime workingHoursEnd;

    @Column(name = "appointment_duration")
    private Integer appointmentDuration = 30; // Default 30 minutes

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    // Explicit getter and setter methods for workingHoursStart
    public LocalDateTime getWorkingHoursStart() {
        return workingHoursStart;
    }

    public void setWorkingHoursStart(LocalDateTime workingHoursStart) {
        this.workingHoursStart = workingHoursStart;
    }

    // Explicit getter and setter methods for workingHoursEnd
    public LocalDateTime getWorkingHoursEnd() {
        return workingHoursEnd;
    }

    public void setWorkingHoursEnd(LocalDateTime workingHoursEnd) {
        this.workingHoursEnd = workingHoursEnd;
    }

    // Explicit getter and setter methods for appointmentDuration
    public Integer getAppointmentDuration() {
        return appointmentDuration;
    }

    public void setAppointmentDuration(Integer appointmentDuration) {
        this.appointmentDuration = appointmentDuration;
    }
}