package com.example.healthflow.controller;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.AppointmentStatus;
import com.example.healthflow.model.Doctor;
import com.example.healthflow.model.Patient;
import com.example.healthflow.model.DoctorAvailability;
import com.example.healthflow.dto.AppointmentDTO;
import com.example.healthflow.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.time.DayOfWeek;

@RestController
@RequestMapping("/api/doctor")
public class DoctorRestController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/profile")
    public ResponseEntity<Doctor> getProfile(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<List<AppointmentDTO>> getTodaysAppointments(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        List<AppointmentDTO> appointmentDTOs = doctorService.getTodaysAppointments(doctor).stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/pending")
    public ResponseEntity<List<AppointmentDTO>> getPendingAppointments(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        List<AppointmentDTO> appointmentDTOs = doctorService.getPendingAppointments(doctor).stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/period")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        List<Appointment> appointments = doctorService.getAppointmentsForPeriod(doctor, startDate, endDate);

        // Convert entities to DTOs to avoid circular references
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/past")
    public ResponseEntity<List<AppointmentDTO>> getPastAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());

        // Get appointments for the period
        List<Appointment> appointments = doctorService.getAppointmentsForPeriod(doctor, startDate, endDate);

        // Filter to only completed or no-show appointments
        List<Appointment> pastAppointments = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.NO_SHOW)
                .collect(Collectors.toList());

        // Convert to DTOs
        List<AppointmentDTO> appointmentDTOs = pastAppointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/appointments/cancelled")
    public ResponseEntity<List<AppointmentDTO>> getCancelledAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());

        // Get appointments for the period
        List<Appointment> appointments = doctorService.getAppointmentsForPeriod(doctor, startDate, endDate);

        // Filter to only cancelled appointments
        List<Appointment> cancelledAppointments = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());

        // Convert to DTOs
        List<AppointmentDTO> appointmentDTOs = cancelledAppointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointmentDTOs);
    }

    @GetMapping("/calendar/weekly")
    public ResponseEntity<Map<LocalDate, List<Appointment>>> getWeeklyCalendar(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctorService.getWeeklyCalendar(doctor));
    }

    @GetMapping("/calendar/monthly")
    public ResponseEntity<Map<LocalDate, List<Appointment>>> getMonthlyCalendar(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctorService.getMonthlyCalendar(doctor));
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<Patient> getPatientDetails(@PathVariable Long patientId) {
        Patient patient = doctorService.getPatientDetails(patientId);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestParam AppointmentStatus status) {

        Appointment appointment = doctorService.updateAppointmentStatus(appointmentId, status);
        if (appointment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(appointment);
    }

    @PostMapping("/availability")
    public ResponseEntity<?> setAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam boolean available,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        doctorService.setAvailability(doctor, startTime, endTime, available);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Appointment>> getNotifications(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        return ResponseEntity.ok(doctorService.getNotifications(doctor));
    }

    @PutMapping("/update-doctor")
    public ResponseEntity<Doctor> updateProfile(
            @RequestBody Doctor updatedDoctor,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());

        // Update only allowed fields
        doctor.setFirstName(updatedDoctor.getFirstName());
        doctor.setLastName(updatedDoctor.getLastName());
        doctor.setPhoneNumber(updatedDoctor.getPhoneNumber());
        doctor.setBio(updatedDoctor.getBio());
        doctor.setSpecialization(updatedDoctor.getSpecialization());

        // Save the updated doctor using the service method
        doctor = doctorService.updateDoctorInfo(doctor);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/availability")
    public ResponseEntity<?> getAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        if (doctor == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();

        // Get working hours for each day based on the doctor_availability table
        Map<Integer, Map<String, Object>> workingHours = new HashMap<>();
        List<DoctorAvailability> availabilityList = doctorService.getDoctorAvailability(doctor);

        // Default for all days
        for (int i = 0; i < 7; i++) {
            Map<String, Object> dayHours = new HashMap<>();
            dayHours.put("enabled", false);
            dayHours.put("startTime", "09:00");
            dayHours.put("endTime", "17:00");
            workingHours.put(i, dayHours);
        }

        // Update with the doctor's actual availability
        for (DoctorAvailability availability : availabilityList) {
            int dayIndex = availability.getDayOfWeek().getValue() % 7; // Sunday is 0 in JS, but 7 in DayOfWeek
            if (dayIndex == 0) dayIndex = 7;
            dayIndex--; // Adjust to 0-6 index

            Map<String, Object> dayHours = workingHours.get(dayIndex);
            dayHours.put("enabled", true);
            dayHours.put("startTime", availability.getStartTime().toString());
            dayHours.put("endTime", availability.getEndTime().toString());
        }

        response.put("workingHours", workingHours);

        // Get appointments for the period
        List<Appointment> appointments = doctorService.getAppointmentsForPeriod(
                doctor,
                start.toLocalDate(),
                end.toLocalDate()
        );

        // Convert appointments to DTO to avoid circular references
        List<Map<String, Object>> appointmentList = appointments.stream()
                .map(apt -> {
                    Map<String, Object> aptMap = new HashMap<>();
                    aptMap.put("id", apt.getId());
                    aptMap.put("appointmentTime", apt.getAppointmentTime());
                    aptMap.put("status", apt.getStatus().name());
                    if (apt.getPatient() != null) {
                        aptMap.put("patientName", apt.getPatient().getFirstName() + " " + apt.getPatient().getLastName());
                    }
                    return aptMap;
                })
                .collect(Collectors.toList());

        response.put("appointments", appointmentList);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/working-hours")
    public ResponseEntity<?> setWorkingHours(
            @RequestBody Map<String, Map<String, Object>> workingHours,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        if (doctor == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Process each day's working hours
            processWorkingHoursForDay(doctor, workingHours, "sunday", DayOfWeek.SUNDAY);
            processWorkingHoursForDay(doctor, workingHours, "monday", DayOfWeek.MONDAY);
            processWorkingHoursForDay(doctor, workingHours, "tuesday", DayOfWeek.TUESDAY);
            processWorkingHoursForDay(doctor, workingHours, "wednesday", DayOfWeek.WEDNESDAY);
            processWorkingHoursForDay(doctor, workingHours, "thursday", DayOfWeek.THURSDAY);
            processWorkingHoursForDay(doctor, workingHours, "friday", DayOfWeek.FRIDAY);
            processWorkingHoursForDay(doctor, workingHours, "saturday", DayOfWeek.SATURDAY);

            // Update appointment duration if present
            if (workingHours.containsKey("settings") && workingHours.get("settings").containsKey("appointmentDuration")) {
                Integer duration = Integer.parseInt(workingHours.get("settings").get("appointmentDuration").toString());
                doctor.setAppointmentDuration(duration);
                doctorService.updateDoctorInfo(doctor);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating working hours: " + e.getMessage());
        }
    }

    private void processWorkingHoursForDay(
            Doctor doctor,
            Map<String, Map<String, Object>> workingHours,
            String dayKey,
            DayOfWeek dayOfWeek) {

        if (workingHours.containsKey(dayKey)) {
            Map<String, Object> dayData = workingHours.get(dayKey);
            boolean enabled = (boolean) dayData.get("enabled");

            // Get existing availability for this day
            List<DoctorAvailability> existingAvailability = doctorService.getDoctorAvailability(doctor)
                    .stream()
                    .filter(a -> a.getDayOfWeek() == dayOfWeek)
                    .collect(Collectors.toList());

            if (enabled) {
                // Set or update availability
                String startTimeStr = (String) dayData.get("start");
                String endTimeStr = (String) dayData.get("end");

                LocalTime startTime = LocalTime.parse(startTimeStr);
                LocalTime endTime = LocalTime.parse(endTimeStr);

                doctorService.setDoctorAvailability(doctor, dayOfWeek, startTime, endTime);
            } else if (!existingAvailability.isEmpty()) {
                // Delete availability if day is disabled
                doctorService.deleteDoctorAvailability(existingAvailability.get(0).getId());
            }
        }
    }

    @GetMapping("/availability/schedule")
    public ResponseEntity<List<DoctorAvailability>> getDoctorAvailabilitySchedule(Authentication authentication) {
        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        if (doctor == null) {
            return ResponseEntity.notFound().build();
        }

        List<DoctorAvailability> availabilityList = doctorService.getDoctorAvailability(doctor);
        return ResponseEntity.ok(availabilityList);
    }

    @PostMapping("/availability/schedule")
    public ResponseEntity<DoctorAvailability> setDoctorAvailabilitySchedule(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        if (doctor == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            DoctorAvailability availability = doctorService.setDoctorAvailability(doctor, dayOfWeek, startTime, endTime);
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/availability/schedule/{id}")
    public ResponseEntity<?> deleteDoctorAvailabilitySchedule(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            doctorService.deleteDoctorAvailability(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/availability/check")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentTime,
            @RequestParam(defaultValue = "30") int durationMinutes,
            Authentication authentication) {

        Doctor doctor = doctorService.getDoctorByUsername(authentication.getName());
        if (doctor == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isAvailable = doctorService.isDoctorAvailable(doctor, appointmentTime, durationMinutes);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);

        return ResponseEntity.ok(response);
    }
}