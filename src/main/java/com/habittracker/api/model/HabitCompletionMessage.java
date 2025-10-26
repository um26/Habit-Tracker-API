package com.habittracker.api.model;

public class HabitCompletionMessage {
    private Long userId;
    private String username;
    private int newStreak;
    private Long habitId;
    private String habitName;

    public HabitCompletionMessage() {
    }

    public HabitCompletionMessage(Long userId, String username, int newStreak, Long habitId, String habitName) {
        this.userId = userId;
        this.username = username;
        this.newStreak = newStreak;
        this.habitId = habitId;
        this.habitName = habitName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getNewStreak() {
        return newStreak;
    }

    public void setNewStreak(int newStreak) {
        this.newStreak = newStreak;
    }

    public Long getHabitId() {
        return habitId;
    }

    public void setHabitId(Long habitId) {
        this.habitId = habitId;
    }

    public String getHabitName() {
        return habitName;
    }

    public void setHabitName(String habitName) {
        this.habitName = habitName;
    }
}