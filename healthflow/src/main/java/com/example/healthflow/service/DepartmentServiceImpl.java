package com.example.healthflow.service;

import com.example.healthflow.model.Department;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.repository.DepartmentRepository;
import com.example.healthflow.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        // Initialize the doctors collection for each department
        departments.forEach(department -> {
            department.setDoctors(doctorRepository.findByDepartmentId(department.getId()));
        });
        return departments;
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id).orElse(null);
        if (department != null) {
            // Initialize the doctors collection
            department.setDoctors(doctorRepository.findByDepartmentId(id));
        }
        return department;
    }

    @Override
    @Transactional
    public Department saveDepartment(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            return null; // Department with this name already exists
        }
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public boolean updateDepartment(Long id, String name) {
        Department department = departmentRepository.findById(id).orElse(null);
        if (department == null) {
            return false;
        }

        // Check if another department already has this name
        Department existingDept = departmentRepository.findByName(name);
        if (existingDept != null && !existingDept.getId().equals(id)) {
            return false;
        }

        department.setName(name);
        departmentRepository.save(department);
        return true;
    }

    @Override
    @Transactional
    public boolean removeDepartment(Long id) {
        Department department = departmentRepository.findById(id).orElse(null);
        if (department == null) {
            return false;
        }

        // Check if any doctors are assigned to this department
        List<Doctor> doctors = doctorRepository.findByDepartmentId(id);
        if (!doctors.isEmpty()) {
            return false; // Cannot remove department with assigned doctors
        }

        departmentRepository.delete(department);
        return true;
    }
}