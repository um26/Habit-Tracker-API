package com.habittracker.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FriendDTO {
    private String username;
    private String uniqueUserId;
    private int dailyStreak;
}