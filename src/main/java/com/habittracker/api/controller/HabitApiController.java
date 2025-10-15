package com.habittracker.api.controller;

import com.habittracker.api.model.HabitLog;
import com.habittracker.api.repository.HabitLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/habits")
public class HabitApiController {

    @Autowired
    private HabitLogRepository habitLogRepository;

    @GetMapping("/{habitId}/logs")
    public List<String> getHabitLogs(@PathVariable Long habitId) {
        // Find all logs for a habit and return just a list of the dates as strings
        return habitLogRepository.findByHabitId(habitId).stream()
                .map(HabitLog::getCompletionDate)
                .map(LocalDate::toString) // Convert LocalDate to "YYYY-MM-DD" string
                .collect(Collectors.toList());
    }
}