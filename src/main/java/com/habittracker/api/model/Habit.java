package com.habittracker.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "habits")
@JsonIgnoreProperties({"user"})
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(mappedBy = "habits")
    private List<ChallengeRoom> challengeRooms;

    public List<ChallengeRoom> getChallengeRooms() {
        return challengeRooms;
    }

    public void setChallengeRooms(List<ChallengeRoom> challengeRooms) {
        this.challengeRooms = challengeRooms;
    }
}