package com.habittracker.api.repository;

import com.habittracker.api.model.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Optional;

import java.util.List;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {

    boolean existsByHabitIdAndCompletionDate(Long habitId, LocalDate date);
    Optional<HabitLog> findTopByHabitIdOrderByCompletionDateDesc(Long habitId);
    List<HabitLog> findByHabitId(Long habitId);

    @Query("SELECT hl FROM HabitLog hl WHERE hl.habit.user.id = :userId")
    List<HabitLog> findByUserId(Long userId);
    int countByHabitId(Long habitId);
}