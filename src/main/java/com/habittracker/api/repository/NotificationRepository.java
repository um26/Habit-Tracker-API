package com.habittracker.api.repository;

import com.habittracker.api.model.Notification;
import com.habittracker.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientAndIsReadOrderByCreatedAtDesc(User recipient, boolean isRead);
}