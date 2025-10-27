package com.habittracker.api.model;

public class RoomUpdateMessage {
    private String type;
    private String message;

    // Default constructor
    public RoomUpdateMessage() {}
    
    // All-args constructor
    public RoomUpdateMessage(String type, String message) {
        this.type = type;
        this.message = message;
    }

    // --- MANUAL GETTERS AND SETTERS ---
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}