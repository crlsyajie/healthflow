package com.example.healthflow.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // The user this notification is for, null if for all users

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment; // Related appointment if applicable

    public enum NotificationType {
        APPOINTMENT_CREATED,
        APPOINTMENT_UPDATED,
        APPOINTMENT_CANCELLED,
        SYSTEM_NOTIFICATION,
        USER_MESSAGE
    }
}