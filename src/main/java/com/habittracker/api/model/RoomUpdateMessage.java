package com.habittracker.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoomUpdateMessage {
    private String type; // USER_JOINED, USER_COMPLETED, ROOM_COMPLETED
    private String message;
}