package com.habittracker.api.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@Entity
@Table(name = "habit_rooms")
public class HabitRoom{
        
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 10)
    private String roomCode;
    
    @Column(nullable = false)
    private String habitName;
    
    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String dailyGoal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    @Column(nullable = false)
    private int currentStreak = 0;
    
    @OneToMany(mappedBy = "habitRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("habitRoom")
    private List<HabitRoomMember> members;
    
    @OneToMany(mappedBy = "habitRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("habitRoom")
    private List<HabitRoomLog> logs;
}