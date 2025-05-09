package com.example.healthflow.dto;

import com.example.healthflow.model.Appointment;
import java.time.LocalDateTime;

/**
 * Simple DTO to avoid circular references in JSON serialization
 */
public class AppointmentDTO {
    private Long id;
    private String patientFirstName;
    private String patientLastName;
    private Long patientId;
    private String doctorFirstName;
    private String doctorLastName;
    private Long doctorId;
    private LocalDateTime appointmentTime;
    private String status;
    private String reason;
    private String notes;

    public AppointmentDTO(Appointment appointment) {
        this.id = appointment.getId();
        if (appointment.getPatient() != null) {
            this.patientFirstName = appointment.getPatient().getFirstName();
            this.patientLastName = appointment.getPatient().getLastName();
            this.patientId = appointment.getPatient().getId();
        }
        if (appointment.getDoctor() != null) {
            this.doctorFirstName = appointment.getDoctor().getFirstName();
            this.doctorLastName = appointment.getDoctor().getLastName();
            this.doctorId = appointment.getDoctor().getId();
        }
        this.appointmentTime = appointment.getAppointmentTime();
        this.status = appointment.getStatus().name();
        this.reason = appointment.getReason();
        this.notes = appointment.getNotes();
    }

    // Getters
    public Long getId() { return id; }
    public String getPatientFirstName() { return patientFirstName; }
    public String getPatientLastName() { return patientLastName; }
    public Long getPatientId() { return patientId; }
    public String getDoctorFirstName() { return doctorFirstName; }
    public String getDoctorLastName() { return doctorLastName; }
    public Long getDoctorId() { return doctorId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public String getNotes() { return notes; }
}