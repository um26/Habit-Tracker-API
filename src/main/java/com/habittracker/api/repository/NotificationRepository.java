package com.habittracker.api.repository;

import com.habittracker.api.model.Notification;
import com.habittracker.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientAndIsReadOrderByCreatedAtDesc(User recipient, boolean isRead);
    
    // Get all notifications for a user, optionally filtered by read status
    Page<Notification> findByRecipientAndIsReadOrderByCreatedAtDesc(User recipient, boolean isRead, Pageable pageable);
    
    // Get all notifications for a user regardless of read status
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);
}