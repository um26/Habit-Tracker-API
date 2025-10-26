// 2. HabitRoomMemberRepository.java
package com.habittracker.api.repository;

import com.habittracker.api.model.HabitRoom;
import com.habittracker.api.model.HabitRoomMember;
import com.habittracker.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRoomMemberRepository extends JpaRepository<HabitRoomMember, Long> {
    
    Optional<HabitRoomMember> findByHabitRoomAndUser(HabitRoom habitRoom, User user);
    
    List<HabitRoomMember> findByHabitRoomAndStatus(HabitRoom habitRoom, HabitRoomMember.MemberStatus status);
    
    List<HabitRoomMember> findByUserAndStatus(User user, HabitRoomMember.MemberStatus status);
    
    boolean existsByHabitRoomAndUser(HabitRoom habitRoom, User user);
    
    @Query("SELECT m FROM HabitRoomMember m WHERE m.habitRoom = :room AND m.status = 'ACTIVE'")
    List<HabitRoomMember> findActiveMembers(@Param("room") HabitRoom room);
    
    @Query("SELECT COUNT(m) FROM HabitRoomMember m WHERE m.habitRoom = :room AND m.status = 'ACTIVE' AND m.hasCompletedToday = true")
    int countCompletedMembers(@Param("room") HabitRoom room);
    
    @Query("SELECT COUNT(m) FROM HabitRoomMember m WHERE m.habitRoom = :room AND m.status = 'ACTIVE'")
    int countTotalActiveMembers(@Param("room") HabitRoom room);
    
    @Modifying
    @Query("UPDATE HabitRoomMember m SET m.hasCompletedToday = false WHERE m.habitRoom.id = :roomId")
    void resetDailyCompletionForRoom(@Param("roomId") Long roomId);
    
    @Modifying
    @Query("UPDATE HabitRoomMember m SET m.hasCompletedToday = false WHERE m.habitRoom.isActive = true")
    void resetAllDailyCompletions();
}