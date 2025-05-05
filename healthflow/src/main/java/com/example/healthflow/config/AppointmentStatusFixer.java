package com.example.healthflow.config;

import com.example.healthflow.model.Appointment;
import com.example.healthflow.model.AppointmentStatus;
import com.example.healthflow.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;

@Component
public class AppointmentStatusFixer implements CommandLineRunner {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("============ Starting Appointment Status Fixer ============");

        try {
            // Get list of appointment status values that need fixing
            String sql = "SELECT id, status FROM appointments WHERE status IS NULL OR status NOT IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'RESCHEDULED', 'NO_SHOW', 'UNKNOWN')";
            Query query = entityManager.createNativeQuery(sql);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                System.out.println("No appointment status issues found.");
            } else {
                System.out.println("Found " + results.size() + " appointments with invalid status values.");

                // Fix each appointment with an invalid status
                for (Object[] row : results) {
                    Long id = ((Number) row[0]).longValue();
                    String statusValue = row[1] != null ? row[1].toString() : null;

                    Appointment appointment = appointmentRepository.findById(id).orElse(null);
                    if (appointment != null) {
                        System.out.println("Fixing appointment #" + id + " with status: " + statusValue);

                        // Set a default status if current is invalid
                        appointment.setStatus(AppointmentStatus.PENDING);
                        appointmentRepository.save(appointment);

                        System.out.println("Updated appointment #" + id + " status to: PENDING");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fixing appointment statuses: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("============ Appointment Status Fixer Complete ============");
    }
}