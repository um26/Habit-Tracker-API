// 1. HabitRoomRepository.java
package com.habittracker.api.repository;

import com.habittracker.api.model.HabitRoom;
import com.habittracker.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRoomRepository extends JpaRepository<HabitRoom, Long> {
    
    Optional<HabitRoom> findByRoomCode(String roomCode);
    
    List<HabitRoom> findByCreatedByAndIsActive(User createdBy, boolean isActive);
    
    List<HabitRoom> findByIsActive(boolean isActive);
    
    boolean existsByRoomCode(String roomCode);
    
    @Query("SELECT m.habitRoom FROM HabitRoomMember m WHERE m.user = :user AND m.status = 'ACTIVE' AND m.habitRoom.isActive = true")
    List<HabitRoom> findActiveRoomsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(m) FROM HabitRoomMember m WHERE m.habitRoom.id = :roomId AND m.status = 'ACTIVE'")
    int countActiveMembers(@Param("roomId") Long roomId);
}