package com.example.healthflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    private String reason;

    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Fields for follow-up appointments
    @ManyToOne
    @JoinColumn(name = "parent_appointment_id")
    private Appointment parentAppointment;
    
    @OneToMany(mappedBy = "parentAppointment", cascade = CascadeType.ALL)
    private List<Appointment> followUpAppointments = new ArrayList<>();
    
    @Column(name = "is_follow_up")
    private Boolean isFollowUp = false;
    
    @Column(name = "follow_up_interval_days")
    private Integer followUpIntervalDays;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
    
    // Convenience methods for follow-up appointments
    public void addFollowUpAppointment(Appointment followUp) {
        followUp.setParentAppointment(this);
        followUp.setIsFollowUp(true);
        followUpAppointments.add(followUp);
    }
    
    public boolean hasFollowUps() {
        return followUpAppointments != null && !followUpAppointments.isEmpty();
    }
}