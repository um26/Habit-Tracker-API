package com.habittracker.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@Entity
@Table(name = "habit_room_logs")
public class HabitRoomLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "habit_room_id", nullable = false)
    @JsonIgnoreProperties({"members", "logs"}) 
    private HabitRoom habitRoom;
    
    @Column(nullable = false)
    private LocalDate completionDate;
    
    @Column(nullable = false)
    private LocalDateTime completedAt;
    
    @ElementCollection
    @CollectionTable(
        name = "habit_room_log_completers",
        joinColumns = @JoinColumn(name = "habit_room_log_id")
    )
    @Column(name = "user_id")
    private List<Long> completedByUserIds;
    
    @Column(nullable = false)
    private int streakCount;
    
    @Column(nullable = false)
    private boolean allMembersCompleted;
}
