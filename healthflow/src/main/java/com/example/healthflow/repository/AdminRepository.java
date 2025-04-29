package com.example.healthflow.repository;

import com.example.healthflow.model.Admin;
import com.example.healthflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUser(User user);
    Admin findByEmployeeId(String employeeId);
}