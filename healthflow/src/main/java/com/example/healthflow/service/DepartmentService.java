package com.example.healthflow.service;

import com.example.healthflow.model.Department;
import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Department getDepartmentById(Long id);
    Department saveDepartment(Department department);
    boolean updateDepartment(Long id, String name);
    boolean removeDepartment(Long id);
}