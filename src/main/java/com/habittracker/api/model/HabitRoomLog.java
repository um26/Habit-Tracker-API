package com.habittracker.api.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "habit_room_logs")
public class HabitRoomLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "habit_room_id", nullable = false)
    private HabitRoom habitRoom;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate completionDate;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private int streakCount;

    @Column(nullable = false)
    private boolean allMembersCompleted;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "habit_room_log_completers", joinColumns = @JoinColumn(name = "log_id"))
    @Column(name = "user_id")
    private List<Long> completedByUserIds;
    
    // --- MANUAL GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public HabitRoom getHabitRoom() { return habitRoom; }
    public void setHabitRoom(HabitRoom habitRoom) { this.habitRoom = habitRoom; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }
    public boolean isAllMembersCompleted() { return allMembersCompleted; }
    public void setAllMembersCompleted(boolean allMembersCompleted) { this.allMembersCompleted = allMembersCompleted; }
    public List<Long> getCompletedByUserIds() { return completedByUserIds; }
    public void setCompletedByUserIds(List<Long> completedByUserIds) { this.completedByUserIds = completedByUserIds; }
}