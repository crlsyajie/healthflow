package com.example.healthflow.repository;

import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByUser(User user);
    Doctor findByLicenseNumber(String licenseNumber);
    List<Doctor> findByDepartmentId(Long departmentId);
}