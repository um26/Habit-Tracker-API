package com.habittracker.api.repository;

import com.habittracker.api.model.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {

    List<HabitLog> findByHabitId(Long habitId);

    @Query("SELECT hl FROM HabitLog hl WHERE hl.habit.user.id = :userId")
    List<HabitLog> findByUserId(Long userId);
}