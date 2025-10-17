package com.habittracker.api.exception;

public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException(String message) {
        super(message);
    }
}