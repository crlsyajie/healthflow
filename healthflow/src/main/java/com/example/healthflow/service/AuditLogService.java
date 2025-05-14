package com.example.healthflow.service;

import com.example.healthflow.model.AuditLog;
import com.example.healthflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {
    AuditLog logActivity(String action, String entityType, Long entityId, String description, User user, String ipAddress, String details);
    List<AuditLog> findByUser(User user);
    List<AuditLog> findByEntityTypeAndId(String entityType, Long entityId);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end);
    Page<AuditLog> findAllPaginated(Pageable pageable);
    AuditLog findById(Long id);
    byte[] exportLogsToPdf(List<AuditLog> logs);
} 