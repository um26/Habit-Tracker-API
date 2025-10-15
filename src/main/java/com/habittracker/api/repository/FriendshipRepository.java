package com.habittracker.api.repository;

import com.habittracker.api.model.Friendship;
import com.habittracker.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Find pending requests sent TO a user
    List<Friendship> findByFriendAndStatus(User friend, Friendship.FriendshipStatus status);

    // Find all accepted friendships for a user (where they are either the user or the friend)
    @Query("SELECT f FROM Friendship f WHERE (f.user = :user OR f.friend = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(User user);

    // Check if any friendship (pending or accepted) exists between two users
    @Query("SELECT f FROM Friendship f WHERE (f.user = :user1 AND f.friend = :user2) OR (f.user = :user2 AND f.friend = :user1)")
    Optional<Friendship> findFriendshipBetweenUsers(User user1, User user2);
}