package com.example.healthflow.service;

import com.example.healthflow.model.Appointment;

public interface PublicBookingService {
    /**
     * Book an appointment for a guest user (without authentication)
     * This method will create a new patient account if needed
     * 
     * @param firstName Patient's first name
     * @param lastName Patient's last name
     * @param email Patient's email
     * @param phone Patient's phone number
     * @param dateOfBirth Patient's date of birth
     * @param gender Patient's gender
     * @param doctorId Doctor ID
     * @param appointmentDateTime Appointment date and time
     * @param reason Reason for visit
     * @param notes Additional notes
     * @return The created appointment
     */
    Appointment bookAppointmentForGuest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String dateOfBirth,
        String gender,
        Long doctorId,
        String appointmentDateTime,
        String reason,
        String notes
    );
} 