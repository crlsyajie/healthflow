package com.example.healthflow.controller;

import com.example.healthflow.model.*;
import com.example.healthflow.repository.PatientRepository;
import com.example.healthflow.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Overview statistics for the dashboard
        int totalDoctors = doctorService.getAllDoctors().size();
        int totalPatients = patientService.getAllPatients().size();
        int todayAppointments = appointmentService.getTodayAppointments().size();
        int pendingAppointments = appointmentService.getPendingAppointments().size();

        model.addAttribute("totalDoctors", totalDoctors);
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("pendingAppointments", pendingAppointments);

        // Recent system activity (latest appointments)
        model.addAttribute("recentAppointments", appointmentService.getRecentAppointments(5));

        return "admin/dashboard";
    }

    // Appointments Management
    @GetMapping("/appointments")
    public String appointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        List<Appointment> appointments;

        if (doctorId != null) {
            Doctor doctor = doctorService.getDoctorById(doctorId);
            appointments = appointmentService.getAppointmentsByDoctor(doctor);
        } else if (departmentId != null) {
            appointments = appointmentService.getAppointmentsByDepartment(departmentId);
        } else if (date != null) {
            appointments = appointmentService.getAppointmentsByDate(date);
        } else {
            appointments = appointmentService.getAllAppointments();
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("selectedDate", date);

        return "admin/appointments";
    }

    @GetMapping("/appointments/{id}")
    public String viewAppointmentDetail(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        if (appointment == null) {
            return "redirect:/admin/appointments";
        }
        
        // Get any follow-up appointments for this appointment
        List<Appointment> followUps = appointmentService.getFollowUpAppointments(id);
        
        model.addAttribute("appointment", appointment);
        model.addAttribute("followUps", followUps);
        return "admin/appointment-detail";
    }

    @GetMapping("/appointments/{id}/edit")
    public String editAppointmentForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                redirectAttributes.addFlashAttribute("error", "Appointment not found");
                return "redirect:/admin/appointments";
            }

            model.addAttribute("appointment", appointment);
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("statuses", AppointmentStatus.values());

            return "admin/edit-appointment";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error retrieving appointment: " + e.getMessage());
            return "redirect:/admin/appointments";
        }
    }

    @PostMapping("/appointments/{id}/update")
    public String updateAppointment(
            @PathVariable Long id,
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam String appointmentDateTime,
            @RequestParam Integer durationMinutes,
            @RequestParam AppointmentStatus status,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                redirectAttributes.addFlashAttribute("error", "Appointment not found");
                return "redirect:/admin/appointments";
            }

            // Update appointment details
            Patient patient = patientService.getPatientById(patientId);
            Doctor doctor = doctorService.getDoctorById(doctorId);

            if (patient == null || doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Patient or doctor not found");
                return "redirect:/admin/appointments/" + id + "/edit";
            }

            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentTime(LocalDateTime.parse(appointmentDateTime));
            appointment.setDurationMinutes(durationMinutes);
            appointment.setStatus(status);
            appointment.setReason(reason);
            appointment.setNotes(notes);

            appointmentService.saveAppointment(appointment);

            redirectAttributes.addFlashAttribute("success", "Appointment updated successfully");
            return "redirect:/admin/appointments";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating appointment: " + e.getMessage());
            return "redirect:/admin/appointments/" + id + "/edit";
        }
    }

    @PostMapping("/appointments/{id}/delete")
    @ResponseBody
    public Map<String, String> deleteAppointment(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean deleted = appointmentService.deleteAppointment(id);
            if (deleted) {
                response.put("status", "success");
                response.put("message", "Appointment deleted successfully");
            } else {
                response.put("status", "error");
                response.put("message", "Failed to delete appointment");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error deleting appointment: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/appointments/create")
    public String createAppointment(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam String appointmentDateTime,
            @RequestParam Integer durationMinutes,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            Patient patient = patientService.getPatientById(patientId);
            Doctor doctor = doctorService.getDoctorById(doctorId);

            if (patient == null || doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Patient or doctor not found");
                return "redirect:/admin/appointments";
            }

            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentTime(LocalDateTime.parse(appointmentDateTime));
            appointment.setDurationMinutes(durationMinutes);
            appointment.setStatus(status != null ? status : AppointmentStatus.PENDING);
            appointment.setReason(reason);
            appointment.setNotes(notes);

            appointmentService.saveAppointment(appointment);

            redirectAttributes.addFlashAttribute("success", "Appointment created successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating appointment: " + e.getMessage());
        }

        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointments/{id}/follow-up")
    public String createFollowUp(
            @PathVariable Long id,
            @RequestParam String scheduleOption,
            @RequestParam(required = false) String followUpDate,
            @RequestParam(required = false) String followUpTime,
            @RequestParam(required = false) String selectedIntervals,
            @RequestParam Integer durationMinutes,
            @RequestParam String reason,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                redirectAttributes.addFlashAttribute("error", "Appointment not found.");
                return "redirect:/admin/appointments";
            }
            
            if ("single".equals(scheduleOption)) {
                // Create a single follow-up appointment
                LocalDateTime followUpDateTime = LocalDateTime.parse(followUpDate + "T" + followUpTime);
                
                Appointment followUp = appointmentService.createFollowUpAppointment(
                        id, followUpDateTime, durationMinutes, reason, notes);
                
                if (followUp != null) {
                    redirectAttributes.addFlashAttribute("success", "Follow-up appointment scheduled successfully.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Failed to create follow-up appointment.");
                }
            } else {
                // Create multiple follow-up appointments at regular intervals
                if (selectedIntervals == null || selectedIntervals.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "No intervals selected for follow-up appointments.");
                    return "redirect:/admin/appointments/" + id;
                }
                
                // Parse the selected intervals
                String[] intervalStrings = selectedIntervals.split(",");
                int[] intervals = new int[intervalStrings.length];
                for (int i = 0; i < intervalStrings.length; i++) {
                    intervals[i] = Integer.parseInt(intervalStrings[i]);
                }
                
                // Create the follow-up appointments
                List<Appointment> followUps = appointmentService.createScheduledFollowUps(
                        id, intervals, reason, durationMinutes);
                
                if (followUps != null && !followUps.isEmpty()) {
                    redirectAttributes.addFlashAttribute("success", 
                            followUps.size() + " follow-up appointments scheduled successfully.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Failed to create follow-up appointments.");
                }
            }
            
            return "redirect:/admin/appointments/" + id;
        } catch (Exception e) {
            log.error("Error creating follow-up appointment: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
            return "redirect:/admin/appointments/" + id;
        }
    }

    // Patient Management
    @GetMapping("/patients")
    public String patients(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "admin/patients";
    }

    @PostMapping("/patients/add")
    public String addPatient(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam Gender gender,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes) {

        try {
            // Check if username already exists
            if (userService.findByUsername(username) != null) {
                redirectAttributes.addFlashAttribute("error", "Username already exists: " + username);
                return "redirect:/admin/patients";
            }

            // Create new user with PATIENT role
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(Role.PATIENT);
            user.setEnabled(true);

            // Save the user
            user = userService.saveUser(user);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Failed to create user account");
                return "redirect:/admin/patients";
            }

            // Create a new patient
            Patient patient = new Patient();
            patient.setUser(user);
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setDateOfBirth(dateOfBirth);
            patient.setGender(gender);
            patient.setPhoneNumber(phoneNumber);
            patient.setAddress(address);

            // Save the patient
            Patient savedPatient = patientService.updateProfile(patient);
            if (savedPatient != null) {
                redirectAttributes.addFlashAttribute("success", "Patient added successfully: " + firstName + " " + lastName);
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to add patient profile");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding patient: " + e.getMessage());
        }

        return "redirect:/admin/patients";
    }

    @GetMapping("/patients/{id}")
    public String patientDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Patient patient = patientService.getPatientById(id);
        if (patient == null) {
            redirectAttributes.addFlashAttribute("error", "Patient not found with ID: " + id);
            return "redirect:/admin/patients";
        }
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointmentService.getAppointmentsByPatient(patient));
        return "admin/patient-details";
    }

    @PostMapping("/patients/{id}/remove")
    public String removePatient(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            // First check if the patient exists and get their name for the message
            Optional<Patient> patientOpt = Optional.ofNullable(patientService.getPatientById(id));

            if (patientOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Patient not found with ID: " + id);
                return "redirect:/admin/patients";
            }

            Patient patient = patientOpt.get();
            String patientName = patient.getFirstName() + " " + patient.getLastName();

            // Delete the patient (this will also delete the user in PatientServiceImpl)
            boolean deleted = patientService.deletePatient(id);

            if (deleted) {
                redirectAttributes.addFlashAttribute("success",
                        String.format("Successfully removed patient %s and associated records", patientName));
            } else {
                redirectAttributes.addFlashAttribute("error",
                        String.format("Failed to remove patient %s", patientName));
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove patient: " + e.getMessage());
        }

        return "redirect:/admin/patients";
    }

    @PostMapping("/patients/{id}/update")
    public String updatePatient(
            @PathVariable Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam Gender gender,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes) {

        try {
            Patient patient = patientService.getPatientById(id);
            if (patient == null) {
                redirectAttributes.addFlashAttribute("error", "Patient not found");
                return "redirect:/admin/patients";
            }

            // Update patient details
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setDateOfBirth(dateOfBirth);
            patient.setGender(gender);
            patient.setPhoneNumber(phoneNumber);
            patient.setAddress(address);

            Patient updatedPatient = patientService.updateProfile(patient);
            if (updatedPatient != null) {
                redirectAttributes.addFlashAttribute("success", "Patient updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update patient");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating patient: " + e.getMessage());
        }

        return "redirect:/admin/patients/" + id;
    }

    // Doctor Management
    @GetMapping("/doctors")
    public String doctors(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/doctors";
    }

    @PostMapping("/doctors/add")
    public String addDoctor(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Long departmentId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String specialization,
            @RequestParam String licenseNumber,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String bio,
            RedirectAttributes redirectAttributes) {

        try {
            // Check if username already exists
            if (userService.findByUsername(username) != null) {
                redirectAttributes.addFlashAttribute("error", "Username already exists: " + username);
                return "redirect:/admin/doctors";
            }

            // Create new user with DOCTOR role
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(Role.DOCTOR);
            user.setEnabled(true);

            // Save the user
            user = userService.saveUser(user);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Failed to create user account");
                return "redirect:/admin/doctors";
            }

            // Get the department
            Department department = departmentService.getDepartmentById(departmentId);
            if (department == null) {
                redirectAttributes.addFlashAttribute("error", "Department not found with ID: " + departmentId);
                return "redirect:/admin/doctors";
            }

            // Create a new doctor
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setDepartment(department);
            doctor.setFirstName(firstName);
            doctor.setLastName(lastName);
            doctor.setSpecialization(specialization);
            doctor.setLicenseNumber(licenseNumber);
            doctor.setPhoneNumber(phoneNumber);
            doctor.setBio(bio);

            // Save the doctor
            Doctor savedDoctor = doctorService.updateDoctorInfo(doctor);
            if (savedDoctor != null) {
                redirectAttributes.addFlashAttribute("success", "Doctor added successfully: " + firstName + " " + lastName);
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to add doctor profile");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding doctor: " + e.getMessage());
        }

        return "redirect:/admin/doctors";
    }

    @GetMapping("/doctors/{id}")
    public String doctorDetails(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        if (doctor == null) {
            return "redirect:/admin/doctors";
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("appointments", appointmentService.getAppointmentsByDoctor(doctor));
        return "admin/doctor-details";
    }

    @PostMapping("/doctors/{id}/update-department")
    public String updateDoctorDepartment(
            @PathVariable Long id,
            @RequestParam Long departmentId,
            RedirectAttributes redirectAttributes) {

        try {
            Doctor doctor = doctorService.getDoctorById(id);
            Department department = departmentService.getDepartmentById(departmentId);

            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/admin/doctors";
            }

            if (department == null) {
                redirectAttributes.addFlashAttribute("error", "Department not found");
                return "redirect:/admin/doctors/" + id;
            }

            doctor.setDepartment(department);
            doctorService.updateDoctorInfo(doctor);

            redirectAttributes.addFlashAttribute("success", "Department updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update department: " + e.getMessage());
        }

        return "redirect:/admin/doctors/" + id;
    }

    @PostMapping("/doctors/{id}/remove")
    public String removeDoctor(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Doctor doctor = doctorService.getDoctorById(id);
            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/admin/doctors";
            }

            // Get doctor's name for success message
            String doctorName = doctor.getFirstName() + " " + doctor.getLastName();

            // Remove the doctor
            boolean removed = doctorService.removeDoctor(doctor.getId());

            if (removed) {
                redirectAttributes.addFlashAttribute("success", "Doctor " + doctorName + " removed successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to remove doctor. Doctor may have pending appointments.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove doctor: " + e.getMessage());
        }

        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/{id}/update")
    public String updateDoctor(
            @PathVariable Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String specialization,
            @RequestParam String licenseNumber,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String bio,
            RedirectAttributes redirectAttributes) {

        try {
            Doctor doctor = doctorService.getDoctorById(id);
            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/admin/doctors";
            }

            // Update doctor details
            doctor.setFirstName(firstName);
            doctor.setLastName(lastName);
            doctor.setSpecialization(specialization);
            doctor.setLicenseNumber(licenseNumber);
            doctor.setPhoneNumber(phoneNumber);
            doctor.setBio(bio);

            Doctor updatedDoctor = doctorService.updateDoctorInfo(doctor);
            if (updatedDoctor != null) {
                redirectAttributes.addFlashAttribute("success", "Doctor updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update doctor");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating doctor: " + e.getMessage());
        }

        return "redirect:/admin/doctors/" + id;
    }

    @PostMapping("/doctors/{id}/update-resident")
    public String updateDoctorResidentStatus(
            @PathVariable Long id,
            @RequestParam String isResident,
            RedirectAttributes redirectAttributes) {

        try {
            // Validate resident status value
            if (!"yes".equals(isResident) && !"no".equals(isResident)) {
                redirectAttributes.addFlashAttribute("error", "Invalid resident status value");
                return "redirect:/admin/doctors/" + id;
            }

            Doctor updatedDoctor = doctorService.updateResidentStatus(id, isResident);
            
            if (updatedDoctor != null) {
                redirectAttributes.addFlashAttribute("success", "Resident status updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update resident status");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating resident status: " + e.getMessage());
        }

        return "redirect:/admin/doctors/" + id;
    }
    
    @PostMapping("/doctors/{id}/upload-image")
    public String uploadDoctorProfileImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {
            Doctor doctor = doctorService.getDoctorById(id);
            if (doctor == null) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found");
                return "redirect:/admin/doctors";
            }
            
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/admin/doctors/" + id;
            }

            // Create directory if it doesn't exist
            String uploadDir = "uploads/doctor-profiles/";
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save the file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // Update doctor profile with the image path
            doctor = doctorService.updateDoctorProfile(doctor, "/uploads/doctor-profiles/" + filename);
            
            redirectAttributes.addFlashAttribute("success", "Profile image uploaded successfully");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error uploading profile image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }

        return "redirect:/admin/doctors/" + id;
    }

    // Department Management
    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/departments";
    }

    @GetMapping("/departments/{id}")
    public String departmentDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.getDepartmentById(id);
            if (department == null) {
                redirectAttributes.addFlashAttribute("error", "Department not found");
                return "redirect:/admin/departments";
            }

            List<Doctor> doctors = doctorService.getDoctorsByDepartment(id);

            model.addAttribute("department", department);
            model.addAttribute("doctors", doctors);
            return "admin/department-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error retrieving department details: " + e.getMessage());
            return "redirect:/admin/departments";
        }
    }

    @PostMapping("/departments/add")
    public String addDepartment(@RequestParam String name, RedirectAttributes redirectAttributes) {
        Department department = new Department();
        department.setName(name);

        Department saved = departmentService.saveDepartment(department);

        if (saved != null) {
            redirectAttributes.addFlashAttribute("success", "Department added successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to add department");
        }

        return "redirect:/admin/departments";
    }

    @PostMapping("/departments/{id}/update")
    public String updateDepartment(
            @PathVariable Long id,
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        boolean updated = departmentService.updateDepartment(id, name);

        if (updated) {
            redirectAttributes.addFlashAttribute("success", "Department updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update department");
        }

        return "redirect:/admin/departments/" + id;
    }

    @PostMapping("/departments/{id}/remove")
    public String removeDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean removed = departmentService.removeDepartment(id);

        if (removed) {
            redirectAttributes.addFlashAttribute("success", "Department removed successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to remove department or department has assigned doctors");
        }

        return "redirect:/admin/departments";
    }

    // Reports & Analytics
    @GetMapping("/reports")
    public String reports(Model model) {
        return "admin/reports";
    }

    // Patient Monitor
    @GetMapping("/patient-monitor")
    public String patientMonitor(Model model) {
        // Get all patients for the monitoring board
        List<Patient> patients = patientService.getAllPatients();
        
        // Populate last appointment for each patient
        for (Patient patient : patients) {
            if (patient != null) {
                List<Appointment> patientAppointments = appointmentService.getAppointmentsByPatient(patient);
                if (patientAppointments != null && !patientAppointments.isEmpty()) {
                    // Sort by appointment time, latest first
                    patientAppointments.sort(Comparator.comparing(Appointment::getAppointmentTime).reversed());
                    patient.setLastAppointment(patientAppointments.get(0));
                } else {
                    // Explicitly set null for patients with no appointments
                    patient.setLastAppointment(null);
                }
            }
        }
        
        model.addAttribute("patients", patients);
        
        // Count appointments by status
        long pendingCount = appointmentService.getPendingAppointments().size();
        long completedCount = appointmentService.getCompletedAppointments().size();
        long cancelledCount = appointmentService.getCancelledAppointments().size();
        
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        
        return "admin/patient-monitor";
    }

    @GetMapping("/api/reports/appointment-trends")
    @ResponseBody
    public ResponseEntity<?> appointmentTrends() {
        Map<String, Object> data = new HashMap<>();

        // Daily appointments for the last 7 days
        Map<LocalDate, Integer> dailyAppointments = appointmentService.getDailyAppointmentCountForLastDays(7);

        // Monthly appointments for the last 6 months
        Map<String, Integer> monthlyAppointments = appointmentService.getMonthlyAppointmentCount(6);

        // Appointment statuses distribution
        Map<AppointmentStatus, Integer> statusDistribution = appointmentService.getAppointmentStatusDistribution();

        data.put("dailyAppointments", dailyAppointments);
        data.put("monthlyAppointments", monthlyAppointments);
        data.put("statusDistribution", statusDistribution);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/reports/doctor-stats")
    @ResponseBody
    public ResponseEntity<?> doctorStats() {
        Map<String, Object> data = new HashMap<>();

        // Appointments per doctor
        Map<String, Integer> appointmentsPerDoctor = appointmentService.getAppointmentCountPerDoctor();

        // Average appointment duration per doctor
        Map<String, Double> avgDurationPerDoctor = appointmentService.getAverageAppointmentDurationPerDoctor();

        data.put("appointmentsPerDoctor", appointmentsPerDoctor);
        data.put("avgDurationPerDoctor", avgDurationPerDoctor);

        return ResponseEntity.ok(data);
    }

    // Notifications Management
    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("notifications", appointmentService.getAllNotifications());
        return "admin/notifications";
    }

    @PostMapping("/notifications/{id}/mark-read")
    public String markNotificationAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean marked = appointmentService.markNotificationAsRead(id);

        if (marked) {
            redirectAttributes.addFlashAttribute("success", "Notification marked as read");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to mark notification as read");
        }

        return "redirect:/admin/notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsAsRead(RedirectAttributes redirectAttributes) {
        int count = appointmentService.markAllNotificationsAsRead();
        redirectAttributes.addFlashAttribute("success", count + " notifications marked as read");
        return "redirect:/admin/notifications";
    }

    // Add these new endpoints for appointment reporting and authorization settings
    
    @GetMapping("/appointments/print")
    public String printAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        
        // Get appointments based on filters (same logic as in appointments method)
        List<Appointment> appointments;

        if (doctorId != null) {
            Doctor doctor = doctorService.getDoctorById(doctorId);
            appointments = appointmentService.getAppointmentsByDoctor(doctor);
        } else if (departmentId != null) {
            appointments = appointmentService.getAppointmentsByDepartment(departmentId);
        } else if (date != null) {
            appointments = appointmentService.getAppointmentsByDate(date);
        } else {
            appointments = appointmentService.getAllAppointments();
        }
        
        model.addAttribute("appointments", appointments);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("printTitle", "Appointments Report");
        
        // Add filter descriptions for the report heading
        if (doctorId != null) {
            Doctor doctor = doctorService.getDoctorById(doctorId);
            if (doctor != null) {
                model.addAttribute("filterDescription", "Doctor: " + doctor.getFirstName() + " " + doctor.getLastName());
            }
        } else if (departmentId != null) {
            Department department = departmentService.getDepartmentById(departmentId);
            if (department != null) {
                model.addAttribute("filterDescription", "Department: " + department.getName());
            }
        } else if (date != null) {
            model.addAttribute("filterDescription", "Date: " + date);
        } else {
            model.addAttribute("filterDescription", "All Appointments");
        }
        
        return "admin/print/appointments-report";
    }
    
    @GetMapping("/patient/{id}/history/print")
    public String printPatientHistory(@PathVariable Long id, Model model) {
        Patient patient = patientService.getPatientById(id);
        if (patient == null) {
            return "redirect:/admin/patients";
        }
        
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient);
        
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("printTitle", "Patient History Report");
        
        return "admin/print/patient-history-report";
    }
    
    @GetMapping("/export-csv")
    public void exportCsv(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletResponse response) throws IOException {
        
        // Get appointments based on filters
        List<Appointment> appointments;

        if (doctorId != null) {
            Doctor doctor = doctorService.getDoctorById(doctorId);
            appointments = appointmentService.getAppointmentsByDoctor(doctor);
        } else if (departmentId != null) {
            appointments = appointmentService.getAppointmentsByDepartment(departmentId);
        } else if (date != null) {
            appointments = appointmentService.getAppointmentsByDate(date);
        } else {
            appointments = appointmentService.getAllAppointments();
        }
        
        // Set response headers
        String filename = "appointments_export_" + LocalDate.now() + ".csv";
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        // Write CSV data
        try (PrintWriter writer = response.getWriter()) {
            // Write headers
            writer.println("ID,Patient,Doctor,Department,Date,Time,Duration,Status,Reason");
            
            // Write data rows
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            for (Appointment appointment : appointments) {
                StringBuilder sb = new StringBuilder();
                sb.append(appointment.getId()).append(",");
                sb.append(getCsvSafe(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName())).append(",");
                sb.append(getCsvSafe(appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName())).append(",");
                sb.append(getCsvSafe(appointment.getDoctor().getDepartment() != null ? appointment.getDoctor().getDepartment().getName() : "N/A")).append(",");
                
                LocalDateTime dateTime = appointment.getAppointmentTime();
                sb.append(dateTime.format(dateFormatter)).append(",");
                sb.append(dateTime.format(timeFormatter)).append(",");
                
                sb.append(appointment.getDurationMinutes()).append(",");
                sb.append(appointment.getStatus()).append(",");
                sb.append(getCsvSafe(appointment.getReason()));
                
                writer.println(sb.toString());
            }
        }
    }
    
    private String getCsvSafe(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    
    @GetMapping("/authorization-settings")
    public String authorizationSettings(Model model) {
        // Load current settings
        model.addAttribute("termsAndConditions", loadTermsAndConditions());
        model.addAttribute("ndaText", loadNdaText());
        model.addAttribute("users", userService.findAll());
        
        return "admin/authorization-settings";
    }
    
    @PostMapping("/authorization-settings/terms")
    public String updateTermsAndConditions(
            @RequestParam String termsContent,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Save terms and conditions to file
            Path filePath = Paths.get("config/terms-and-conditions.txt");
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, termsContent);
            
            redirectAttributes.addFlashAttribute("success", "Terms and conditions updated successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating terms and conditions: " + e.getMessage());
        }
        
        return "redirect:/admin/authorization-settings";
    }
    
    @PostMapping("/authorization-settings/nda")
    public String updateNda(
            @RequestParam String ndaContent,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Save NDA text to file
            Path filePath = Paths.get("config/nda.txt");
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, ndaContent);
            
            redirectAttributes.addFlashAttribute("success", "NDA template updated successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating NDA template: " + e.getMessage());
        }
        
        return "redirect:/admin/authorization-settings";
    }
    
    @PostMapping("/authorization-settings/user/{id}/toggle")
    public String toggleUserStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.findById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/admin/authorization-settings";
            }
            
            // Toggle user enabled status
            user.setEnabled(!user.isEnabled());
            userService.saveUser(user);
            
            String status = user.isEnabled() ? "enabled" : "disabled";
            redirectAttributes.addFlashAttribute("success", "User " + user.getUsername() + " " + status + " successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user status: " + e.getMessage());
        }
        
        return "redirect:/admin/authorization-settings";
    }
    
    private String loadTermsAndConditions() {
        try {
            Path filePath = Paths.get("config/terms-and-conditions.txt");
            if (Files.exists(filePath)) {
                return Files.readString(filePath);
            } else {
                return "Default Terms and Conditions. Please update this text.";
            }
        } catch (IOException e) {
            return "Error loading terms and conditions.";
        }
    }
    
    private String loadNdaText() {
        try {
            Path filePath = Paths.get("config/nda.txt");
            if (Files.exists(filePath)) {
                return Files.readString(filePath);
            } else {
                return "Default Non-Disclosure Agreement Template. Please update this text.";
            }
        } catch (IOException e) {
            return "Error loading NDA template.";
        }
    }
} 