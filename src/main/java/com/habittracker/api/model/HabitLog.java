package com.habittracker.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter; // <-- Ensure imported
import lombok.Setter; // <-- Ensure imported
import java.time.LocalDate;

@Getter // <-- Add annotation
@Setter // <-- Add annotation
@Entity
@Table(name = "habit_logs")
@JsonIgnoreProperties({"habit"})
public class HabitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate completionDate;

    @Column(name = "streak_count", nullable = false)
    private int streakCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;
}
