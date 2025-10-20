package com.habittracker.api.service;

import com.habittracker.api.model.HabitLog;
import com.habittracker.api.repository.HabitLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HabitService {

    private static final Logger logger = LoggerFactory.getLogger(HabitService.class);

    @Autowired
    private HabitLogRepository habitLogRepository;

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
            return 0;
        }

        LocalDate today = LocalDate.now();
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
    }
} // <-- Ensure this closing brace is present