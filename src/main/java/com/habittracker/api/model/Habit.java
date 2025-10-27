package com.habittracker.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "habits")
@JsonIgnoreProperties({"user", "challengeRooms"})
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // This is from your friend's 'ChallengeRoom' (which uses ManyToMany)
    @ManyToMany(mappedBy = "habits")
    private List<ChallengeRoom> challengeRooms;

    // --- MANUAL GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<ChallengeRoom> getChallengeRooms() { return challengeRooms; }
    public void setChallengeRooms(List<ChallengeRoom> challengeRooms) { this.challengeRooms = challengeRooms; }
}