package com.habittracker.api.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import com.habittracker.api.model.Habit;
import com.habittracker.api.model.HabitLog;
import com.habittracker.api.repository.HabitLogRepository;
<<<<<<< HEAD
import com.habittracker.api.repository.HabitRepository;
import com.habittracker.api.exception.HabitNotFoundException;
import com.habittracker.api.exception.HabitAlreadyLoggedException;
=======
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects; // Import Objects for null check
import java.util.stream.Collectors;
>>>>>>> 4cb3601 (Describe your changes here)

@Service
public class HabitService {

    private static final Logger logger = LoggerFactory.getLogger(HabitService.class);

    @Autowired
    private HabitLogRepository habitLogRepository;

<<<<<<< HEAD
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
=======
    // --- Calculate Streak for a Specific Habit ---
    public int calculateCurrentStreak(Long habitId) {
        logger.debug("Calculating current streak for habit ID: {}", habitId);
        List<HabitLog> logs = habitLogRepository.findByHabitId(habitId);
        return calculateStreakLogic(logs, "Habit " + habitId);
    }

    // --- Calculate Overall Daily Streak for a User ---
    public int calculateDailyStreak(Long userId) {
        logger.debug("Calculating daily streak for user ID: {}", userId);
        List<HabitLog> logs = habitLogRepository.findByUserId(userId);
        return calculateStreakLogic(logs, "User " + userId);
    }

    // --- Shared Streak Calculation Logic ---
    private int calculateStreakLogic(List<HabitLog> logs, String context) {
        if (logs == null || logs.isEmpty()) {
            logger.debug("[{}] No logs found. Streak: 0", context);
            return 0;
        }

        // Get unique dates sorted from newest to oldest
        List<LocalDate> distinctDates = logs.stream()
                .map(HabitLog::getCompletionDate)
                .filter(Objects::nonNull) // Ensure no null dates
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        logger.debug("[{}] Distinct completion dates (newest first): {}", context, distinctDates);

        if (distinctDates.isEmpty()) {
            logger.debug("[{}] No valid dates found after filtering. Streak: 0", context);
>>>>>>> 4cb3601 (Describe your changes here)
            return 0;
        }

        LocalDate lastDate = lastLog.getCompletionDate();
        LocalDate today = LocalDate.now();
<<<<<<< HEAD

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
=======
        LocalDate mostRecentLogDate = distinctDates.get(0);
        logger.debug("[{}] Today's date: {}, Most recent log date: {}", context, today, mostRecentLogDate);

        // Check if the streak is current (most recent log is today or yesterday)
        if (!mostRecentLogDate.isEqual(today) && !mostRecentLogDate.isEqual(today.minusDays(1))) {
            logger.debug("[{}] Streak broken. Most recent log is neither today nor yesterday. Streak: 0", context);
            return 0;
        }

        int streak = 0;
        // Start checking from the most recent log date
        LocalDate expectedDate = mostRecentLogDate;

        logger.debug("[{}] Starting streak check. Expecting date: {}", context, expectedDate);

        for (LocalDate date : distinctDates) {
            logger.debug("[{}] Checking date: {}. Expecting: {}", context, date, expectedDate);
            if (date.isEqual(expectedDate)) {
                streak++;
                logger.debug("[{}] Match found! Streak is now: {}", context, streak);
                expectedDate = expectedDate.minusDays(1); // Set expectation for the previous day
            } else {
                logger.debug("[{}] Streak broken at date: {}. Expected: {}", context, date, expectedDate);
                break; // Streak is broken if a day is missed
            }
        }
        logger.info("[{}] Final calculated streak: {}", context, streak); // Use INFO for final result
        return streak;
>>>>>>> 4cb3601 (Describe your changes here)
    }
}