package com.habittracker.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "habit_rooms")
public class HabitRoom {
        
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
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"habits", "password"})
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

    // --- MANUAL GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getHabitName() { return habitName; }
    public void setHabitName(String habitName) { this.habitName = habitName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDailyGoal() { return dailyGoal; }
    public void setDailyGoal(String dailyGoal) { this.dailyGoal = dailyGoal; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public List<HabitRoomMember> getMembers() { return members; }
    public void setMembers(List<HabitRoomMember> members) { this.members = members; }
    public List<HabitRoomLog> getLogs() { return logs; }
    public void setLogs(List<HabitRoomLog> logs) { this.logs = logs; }
}