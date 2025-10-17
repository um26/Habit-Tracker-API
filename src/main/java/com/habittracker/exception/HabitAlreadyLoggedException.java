package com.habittracker.api.exception;

public class HabitAlreadyLoggedException extends RuntimeException {
    public HabitAlreadyLoggedException(String message) {
        super(message);
    }
}