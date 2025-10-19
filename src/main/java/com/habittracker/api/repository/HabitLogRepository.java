package com.habittracker.api.repository;

import com.habittracker.api.model.Habit;
import com.habittracker.api.model.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Optional;

import java.time.LocalDate;
import java.util.List;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {

    boolean existsByHabitIdAndCompletionDate(Long habitId, LocalDate date);
    Optional<HabitLog> findTopByHabitIdOrderByCompletionDateDesc(Long habitId);
    List<HabitLog> findByHabitId(Long habitId);

    @Query("SELECT hl FROM HabitLog hl WHERE hl.habit.user.id = :userId")
    List<HabitLog> findByUserId(Long userId);
<<<<<<< HEAD
    int countByHabitId(Long habitId);
=======

    boolean existsByHabitAndCompletionDate(Habit habit, LocalDate date);
>>>>>>> 4cb3601 (Describe your changes here)
}