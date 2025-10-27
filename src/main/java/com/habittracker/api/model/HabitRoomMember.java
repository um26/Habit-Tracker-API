package com.habittracker.api.model;

import jakarta.persistence.*;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "habit_room_members")
public class HabitRoomMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "habit_room_id", nullable = false)
    @JsonIgnoreProperties({"members", "logs"})
    private HabitRoom habitRoom;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"habits", "password"}) 
    private User user;
    
    @Column(nullable = false)
    private boolean hasCompletedToday = false;
    
    @Column(nullable = false)
    private LocalDateTime joinedAt;
    
    @Column
    private LocalDateTime lastCompletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;
    
    public enum MemberStatus {
        ACTIVE,
        LEFT,
        KICKED
    }
    
    // --- MANUAL GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public HabitRoom getHabitRoom() { return habitRoom; }
    public void setHabitRoom(HabitRoom habitRoom) { this.habitRoom = habitRoom; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public boolean isHasCompletedToday() { return hasCompletedToday; }
    public void setHasCompletedToday(boolean hasCompletedToday) { this.hasCompletedToday = hasCompletedToday; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public LocalDateTime getLastCompletedAt() { return lastCompletedAt; }
    public void setLastCompletedAt(LocalDateTime lastCompletedAt) { this.lastCompletedAt = lastCompletedAt; }
    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
}