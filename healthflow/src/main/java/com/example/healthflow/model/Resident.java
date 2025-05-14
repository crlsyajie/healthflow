package com.example.healthflow.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "residents")
@Data
public class Resident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "program_start_date")
    private LocalDate programStartDate;

    @Column(name = "program_end_date")
    private LocalDate programEndDate;

    @Column(name = "program_name", nullable = false)
    private String programName;

    @Column(name = "residency_year")
    private Integer residencyYear;

    @Column(name = "supervisor_name")
    private String supervisorName;

    @Column(name = "supervisor_id")
    private Long supervisorId;

    @Column(name = "notes", length = 1000)
    private String notes;

    // Explicit getter and setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getProgramStartDate() {
        return programStartDate;
    }

    public void setProgramStartDate(LocalDate programStartDate) {
        this.programStartDate = programStartDate;
    }

    public LocalDate getProgramEndDate() {
        return programEndDate;
    }

    public void setProgramEndDate(LocalDate programEndDate) {
        this.programEndDate = programEndDate;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public Integer getResidencyYear() {
        return residencyYear;
    }

    public void setResidencyYear(Integer residencyYear) {
        this.residencyYear = residencyYear;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 