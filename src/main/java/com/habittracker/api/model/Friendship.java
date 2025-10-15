package com.habittracker.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "friendships")
public class Friendship {

    public enum FriendshipStatus {
        PENDING,
        ACCEPTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who initiated the request

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend; // The user receiving the request

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;
}