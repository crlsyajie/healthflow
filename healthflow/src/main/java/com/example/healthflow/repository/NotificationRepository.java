package com.example.healthflow.repository;

import com.example.healthflow.model.Notification;
import com.example.healthflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserIsNullOrderByCreatedAtDesc(); // Global notifications

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = ?1")
    int markAsRead(Long id);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = ?1 OR n.user IS NULL")
    int markAllAsRead(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true")
    int markAllAsRead();
}