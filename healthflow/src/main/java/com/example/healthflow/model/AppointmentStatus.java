package com.example.healthflow.model;

public enum AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELED,
    CANCELLED,  // Alternative spelling that might be in the database
    RESCHEDULED,
    NO_SHOW,
    PENDING,
    CONFIRMED,
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
            if (status.equalsIgnoreCase("CANCELLED")) {
                return CANCELED;
            }
            return UNKNOWN;
        }
    }
}