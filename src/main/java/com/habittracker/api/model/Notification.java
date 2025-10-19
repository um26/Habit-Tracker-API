package com.habittracker.api.model;

import jakarta.persistence.*;
import lombok.Getter; // <-- Ensure imported
import lombok.Setter; // <-- Ensure imported
import java.time.LocalDateTime;

@Getter // <-- Add annotation
@Setter // <-- Add annotation
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false)
    private String message;

    private String link;

    private boolean isRead = false;

    private LocalDateTime createdAt;
}