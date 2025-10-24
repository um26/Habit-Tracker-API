package com.habittracker.api.repository;

import com.habittracker.api.model.ChallengeRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRoomRepository extends JpaRepository<ChallengeRoom, Long> {
    // Add custom queries here if needed
}