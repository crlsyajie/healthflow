package com.example.healthflow.repository;

import com.example.healthflow.model.AuditLog;
import com.example.healthflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
} 