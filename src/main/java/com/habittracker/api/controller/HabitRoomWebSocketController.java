package com.habittracker.api.controller;

import com.habittracker.api.model.*;
import com.habittracker.api.repository.UserRepository;
import com.habittracker.api.service.HabitRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class HabitRoomWebSocketController {

    @Autowired
    private HabitRoomService habitRoomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/rooms/create")
    @SendTo("/topic/rooms")
    public HabitRoom createRoom(Map<String, String> payload, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        String habitName = payload.get("habitName");
        String description = payload.get("description");
        String dailyGoal = payload.get("dailyGoal");

        return habitRoomService.createRoom(user, habitName, description, dailyGoal);
    }

    @MessageMapping("/room/join")
    public void joinRoom(Map<String, String> payload, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        String roomCode = payload.get("roomCode");
        boolean success = habitRoomService.joinRoom(roomCode, user);

        if (success) {
            messagingTemplate.convertAndSend("/topic/room-updates", 
                Map.of("type", "join", "username", user.getUsername(), "roomCode", roomCode));
        }
    }
}