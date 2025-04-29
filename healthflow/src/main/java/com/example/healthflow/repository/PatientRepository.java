package com.example.healthflow.repository;

import com.example.healthflow.model.Patient;
import com.example.healthflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByUser(User user);
}