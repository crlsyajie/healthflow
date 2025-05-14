package com.example.healthflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String address;

    // This is a transient field that will be populated by the service layer
    @Transient
    private Appointment lastAppointment;

    public Appointment getLastAppointment() {
        return lastAppointment;
    }

    public void setLastAppointment(Appointment lastAppointment) {
        this.lastAppointment = lastAppointment;
    }
}