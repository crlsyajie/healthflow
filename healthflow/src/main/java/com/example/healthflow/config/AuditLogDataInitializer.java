package com.example.healthflow.config;

import com.example.healthflow.model.AuditLog;
import com.example.healthflow.model.User;
import com.example.healthflow.repository.AuditLogRepository;
import com.example.healthflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Configuration
public class AuditLogDataInitializer {

    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Bean
    @Profile("!prod") // Don't run in production
    public CommandLineRunner initializeAuditLogData() {
        return args -> {
            // Check if we already have audit logs
            if (auditLogRepository.count() > 0) {
                return; // Data already exists, skip initialization
            }
            
            // Find an admin user or use null for system actions
            User adminUser = userRepository.findByUsername("admin");
            User systemUser = adminUser; // If adminUser is null, systemUser will also be null
            
            LocalDateTime now = LocalDateTime.now();
            Random random = new Random();
            
            // Sample audit log entries - use a mutable ArrayList instead of Arrays.asList
            List<AuditLog> sampleLogs = new ArrayList<>();
            
            // Add initial sample logs
            sampleLogs.add(createLog("LOGIN", "USER", 1L, "User logged in", systemUser, now.minusDays(5)));
            sampleLogs.add(createLog("CREATE", "PATIENT", 1L, "Patient record created", systemUser, now.minusDays(5).plusHours(1)));
            sampleLogs.add(createLog("UPDATE", "PATIENT", 1L, "Patient information updated", systemUser, now.minusDays(5).plusHours(2)));
            sampleLogs.add(createLog("CREATE", "APPOINTMENT", 1L, "Appointment scheduled", systemUser, now.minusDays(4)));
            sampleLogs.add(createLog("UPDATE", "APPOINTMENT", 1L, "Appointment rescheduled", systemUser, now.minusDays(3)));
            sampleLogs.add(createLog("CREATE", "DOCTOR", 1L, "Doctor profile created", systemUser, now.minusDays(3).plusHours(2)));
            sampleLogs.add(createLog("DELETE", "APPOINTMENT", 2L, "Appointment cancelled", systemUser, now.minusDays(2)));
            sampleLogs.add(createLog("UPDATE", "DEPARTMENT", 1L, "Department information updated", systemUser, now.minusDays(2).plusHours(3)));
            sampleLogs.add(createLog("CREATE", "USER", 5L, "User account created", systemUser, now.minusDays(1)));
            sampleLogs.add(createLog("LOGIN", "USER", 2L, "User logged in", systemUser, now.minusHours(12)));
            sampleLogs.add(createLog("ACCESS", "PATIENT", 3L, "Patient record accessed", systemUser, now.minusHours(10)));
            sampleLogs.add(createLog("UPDATE", "DOCTOR", 2L, "Doctor schedule updated", systemUser, now.minusHours(8)));
            sampleLogs.add(createLog("CREATE", "APPOINTMENT", 5L, "Appointment scheduled", systemUser, now.minusHours(6)));
            sampleLogs.add(createLog("UPDATE", "USER", 3L, "User password reset", systemUser, now.minusHours(4)));
            sampleLogs.add(createLog("LOGOUT", "USER", 2L, "User logged out", systemUser, now.minusHours(2)));
            
            // Add some more random logs for pagination testing
            for (int i = 0; i < 30; i++) {
                String[] actions = {"CREATE", "UPDATE", "DELETE", "ACCESS", "LOGIN", "LOGOUT"};
                String[] entityTypes = {"USER", "PATIENT", "DOCTOR", "APPOINTMENT", "DEPARTMENT"};
                
                String action = actions[random.nextInt(actions.length)];
                String entityType = entityTypes[random.nextInt(entityTypes.length)];
                Long entityId = (long) (random.nextInt(10) + 1);
                String description = action + " operation on " + entityType + " #" + entityId;
                
                LocalDateTime timestamp = now.minusDays(random.nextInt(30))
                    .minusHours(random.nextInt(24))
                    .minusMinutes(random.nextInt(60));
                
                sampleLogs.add(createLog(action, entityType, entityId, description, systemUser, timestamp));
            }
            
            // Save all sample logs
            auditLogRepository.saveAll(sampleLogs);
            
            System.out.println("✅ Initialized " + sampleLogs.size() + " sample audit log entries");
        };
    }
    
    private AuditLog createLog(String action, String entityType, Long entityId, String description, User user, LocalDateTime timestamp) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setUser(user);
        log.setCreatedAt(timestamp);
        log.setDetails("Sample audit log entry for demonstration purposes");
        return log;
    }
} 