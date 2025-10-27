package com.habittracker.api.controller;

import com.habittracker.api.model.HabitRoom;
import com.habittracker.api.model.User;
import com.habittracker.api.repository.UserRepository;
import com.habittracker.api.service.HabitRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class HabitRoomWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(HabitRoomWebSocketController.class);

    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private HabitRoomService habitRoomService;
    @Autowired private UserRepository userRepository;
    
    @MessageMapping("/challenge/create")
    public void createChallenge(@Payload Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.error("Cannot create challenge: User is not authenticated.");
            messagingTemplate.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/reply", Map.of("success", false, "error", "Authentication required."), headerAccessor.getMessageHeaders());
            return;
        }

        try {
            // Data from your friend's form
            String habitName = payload.get("dailyGoals");
            String description = payload.get("habitDescription");
            String dailyGoal = payload.get("dailyGoals"); 

            log.info("Received create challenge request from {}: name={}", principal.getName(), habitName);

            User creator = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            HabitRoom room = habitRoomService.createRoom(creator, habitName, description, dailyGoal);

            log.info("Successfully created room with ID: {} and Code: {}", room.getId(), room.getRoomCode());
            
            messagingTemplate.convertAndSendToUser(
                principal.getName(), 
                "/queue/reply", 
                Map.of("success", true, "roomId", room.getId(), "roomCode", room.getRoomCode()) // Send back code
            );

        } catch (Exception e) {
            log.error("Failed to create challenge: {}", e.getMessage(), e);
            messagingTemplate.convertAndSendToUser(
                principal.getName(), 
                "/queue/reply", 
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }
}