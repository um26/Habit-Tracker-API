package com.habittracker.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@Entity
@Table(name = "habit_room_members")
public class HabitRoomMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_room_id", nullable = false)
    @JsonIgnoreProperties({"members", "logs"})
    private HabitRoom habitRoom;
    
    @ManyToOne(fetch = FetchType.LAZY)
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
}