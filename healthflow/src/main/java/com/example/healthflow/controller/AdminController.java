package com.example.healthflow.controller;

import com.example.healthflow.model.*;
import com.example.healthflow.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

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

    // Patient Management
    @GetMapping("/patients")
    public String patients(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "admin/patients";
    }

    @GetMapping("/patients/{id}")
    public String patientDetails(@PathVariable Long id, Model model) {
        Patient patient = patientService.getPatientById(id);
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointmentService.getAppointmentsByPatient(patient));
        return "admin/patient-details";
    }

    // Doctor Management
    @GetMapping("/doctors")
    public String doctors(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/doctors";
    }

    @GetMapping("/doctors/{id}")
    public String doctorDetails(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointmentService.getAppointmentsByDoctor(doctor));
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/doctor-details";
    }

    @PostMapping("/doctors/{id}/update-department")
    public String updateDoctorDepartment(
            @PathVariable Long id,
            @RequestParam Long departmentId,
            RedirectAttributes redirectAttributes) {

        boolean updated = doctorService.updateDoctorDepartment(id, departmentId);

        if (updated) {
            redirectAttributes.addFlashAttribute("success", "Doctor department updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update doctor department");
        }

        return "redirect:/admin/doctors/" + id;
    }

    @PostMapping("/doctors/{id}/remove")
    public String removeDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean removed = doctorService.removeDoctor(id);

        if (removed) {
            redirectAttributes.addFlashAttribute("success", "Doctor removed successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to remove doctor");
        }

        return "redirect:/admin/doctors";
    }

    // Department Management
    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "admin/departments";
    }

    @GetMapping("/departments/{id}")
    public String departmentDetails(@PathVariable Long id, Model model) {
        Department department = departmentService.getDepartmentById(id);
        model.addAttribute("department", department);
        model.addAttribute("doctors", doctorService.getDoctorsByDepartment(id));
        return "admin/department-details";
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
}