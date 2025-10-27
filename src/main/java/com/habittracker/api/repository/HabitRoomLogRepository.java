package com.habittracker.api.repository;

import com.habittracker.api.model.HabitRoom;
import com.habittracker.api.model.HabitRoomLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRoomLogRepository extends JpaRepository<HabitRoomLog, Long> {
    
    Optional<HabitRoomLog> findByHabitRoomAndCompletionDate(HabitRoom habitRoom, LocalDate completionDate);
    
    List<HabitRoomLog> findByHabitRoomOrderByCompletionDateDesc(HabitRoom habitRoom);
    
    List<HabitRoomLog> findByHabitRoom(HabitRoom habitRoom);
    
    boolean existsByHabitRoomAndCompletionDate(HabitRoom habitRoom, LocalDate completionDate);
    
    @Query("SELECT COUNT(l) FROM HabitRoomLog l WHERE l.habitRoom = :room AND l.allMembersCompleted = true")
    int countSuccessfulCompletions(@Param("room") HabitRoom room);
    
    @Query("SELECT l FROM HabitRoomLog l WHERE l.habitRoom = :room ORDER BY l.completionDate DESC")
    List<HabitRoomLog> findRecentLogs(@Param("room") HabitRoom room);
    
    Optional<HabitRoomLog> findTopByHabitRoomOrderByCompletionDateDesc(HabitRoom habitRoom);
    
    @Query("SELECT l FROM HabitRoomLog l WHERE l.habitRoom.id = :roomId AND l.completionDate BETWEEN :startDate AND :endDate ORDER BY l.completionDate")
    List<HabitRoomLog> findLogsBetweenDates(@Param("roomId") Long roomId, 
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);
}