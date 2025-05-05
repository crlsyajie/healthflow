package com.example.healthflow.model;

public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    RESCHEDULED,
    NO_SHOW,
    UNKNOWN;  // Fallback value

    // Handle conversion from string (including null)
    public static AppointmentStatus fromString(String status) {
        if (status == null) {
            return UNKNOWN;
        }

        try {
            return AppointmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle any status values that don't match our enum constants
            return UNKNOWN;
        }
    }
}