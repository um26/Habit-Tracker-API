package com.habittracker.api.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import com.habittracker.api.model.Habit;
import com.habittracker.api.model.HabitLog;
import com.habittracker.api.repository.HabitLogRepository;
import com.habittracker.api.repository.HabitRepository;
import com.habittracker.api.exception.HabitNotFoundException;
import com.habittracker.api.exception.HabitAlreadyLoggedException;

@Service
public class HabitService {

    @Autowired
    private HabitLogRepository habitLogRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Transactional
    public HabitLog logHabitCompletion(Long habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException("Habit with id " + habitId + " not found"));

        LocalDate today = LocalDate.now();

        // Prevent duplicate log for same day
        if (habitLogRepository.existsByHabitIdAndCompletionDate(habitId, today)) {
            throw new HabitAlreadyLoggedException("Habit already logged for today!");
        }

        // Fetch most recent log
        HabitLog lastLog = habitLogRepository.findTopByHabitIdOrderByCompletionDateDesc(habitId)
                .orElse(null);

        int newStreak = 1;
        if (lastLog != null) {
            LocalDate lastDate = lastLog.getCompletionDate();

            if (lastDate.equals(today.minusDays(1))) {
                // Streak continues from yesterday
                newStreak = lastLog.getStreakCount() + 1;
            } else if (lastDate.isBefore(today.minusDays(1))) {
                // Streak broken - more than one day gap
                newStreak = 1;
            }
        }

        HabitLog newLog = new HabitLog();
        newLog.setHabit(habit);
        newLog.setCompletionDate(today);
        newLog.setStreakCount(newStreak);

        System.out.println("=== BEFORE SAVE ===");
        System.out.println("newStreak value: " + newStreak);
        System.out.println("newLog.getStreakCount(): " + newLog.getStreakCount());
        
        HabitLog saved = habitLogRepository.save(newLog);
        
        System.out.println("=== AFTER SAVE ===");
        System.out.println("saved.getStreakCount(): " + saved.getStreakCount());
        
        return saved;
    }

    public int getCurrentStreak(Long habitId) {
        HabitLog lastLog = habitLogRepository.findTopByHabitIdOrderByCompletionDateDesc(habitId)
                .orElse(null);

        if (lastLog == null) {
            return 0;
        }

        LocalDate lastDate = lastLog.getCompletionDate();
        LocalDate today = LocalDate.now();

        // Streak is valid only if logged today or yesterday
        if (lastDate.equals(today) || lastDate.equals(today.minusDays(1))) {
            return lastLog.getStreakCount();
        }

        // Streak expired - last log is older than yesterday
        return 0;
    }

    public int getLongestStreak(Long habitId) {
        List<HabitLog> allLogs = habitLogRepository.findByHabitId(habitId);
        
        if (allLogs.isEmpty()) {
            return 0;
        }

        return allLogs.stream()
                .mapToInt(HabitLog::getStreakCount)
                .max()
                .orElse(0);
    }

    public int getTotalCompletions(Long habitId) {
        return habitLogRepository.countByHabitId(habitId);
    }
    public List<HabitLog> getAllHabitLogs(Long habitId) {
        return habitLogRepository.findByHabitId(habitId);
    }
}