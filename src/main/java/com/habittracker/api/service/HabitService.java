package com.habittracker.api.service;

import com.habittracker.api.model.HabitLog;
import com.habittracker.api.repository.HabitLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HabitService {

    @Autowired
    private HabitLogRepository habitLogRepository;

    public int calculateCurrentStreak(Long habitId) {
        List<HabitLog> logs = habitLogRepository.findByHabitId(habitId);

        List<LocalDate> distinctDates = logs.stream()
                .map(HabitLog::getCompletionDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (distinctDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        if (!distinctDates.get(0).isEqual(today) && !distinctDates.get(0).isEqual(today.minusDays(1))) {
            return 0;
        }

        int streak = 0;
        LocalDate expectedDate = today;
        
        if(distinctDates.get(0).isEqual(today.minusDays(1))){
            expectedDate = today.minusDays(1);
        }

        for (LocalDate date : distinctDates) {
            if (date.isEqual(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    public int calculateDailyStreak(Long userId) {
        List<HabitLog> logs = habitLogRepository.findByUserId(userId);

        List<LocalDate> distinctDates = logs.stream()
                .map(HabitLog::getCompletionDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (distinctDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        if (!distinctDates.get(0).isEqual(today) && !distinctDates.get(0).isEqual(today.minusDays(1))) {
            return 0;
        }

        int streak = 0;
        LocalDate expectedDate = today;
        
        if(distinctDates.get(0).isEqual(today.minusDays(1))){
            expectedDate = today.minusDays(1);
        }

        for (LocalDate date : distinctDates) {
            if (date.isEqual(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }
}