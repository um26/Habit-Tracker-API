package com.habittracker.api.controller;

import com.habittracker.api.model.ChallengeRoom;
import com.habittracker.api.model.User;
import com.habittracker.api.repository.ChallengeRoomRepository;
import com.habittracker.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/challenge")
public class ChallengeRoomController {

    @Autowired
    private ChallengeRoomRepository challengeRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/rooms/{id}")
    public String getChallengeRoom(@PathVariable Long id, Model model, Principal principal) {
        Optional<ChallengeRoom> room = challengeRoomRepository.findById(id);
        if (room.isPresent()) {
            User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
            if (currentUser != null && room.get().getParticipants().contains(currentUser)) {
                model.addAttribute("challenge", room.get());
                model.addAttribute("currentUser", currentUser);
                return "challenge-details";
            }
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/create")
    public String showCreateChallengePage(Model model) {
        model.addAttribute("challenge", new ChallengeRoom());
        return "create-challenge";
    }

    @PostMapping("/create")
    public String createChallengeRoom(@ModelAttribute ChallengeRoom challenge, Principal principal) {
        User creator = userRepository.findByEmail(principal.getName()).orElse(null);
        if (creator != null) {
            challenge.getParticipants().add(creator);
            ChallengeRoom savedChallenge = challengeRoomRepository.save(challenge);
            return "redirect:/challenge/rooms/" + savedChallenge.getId();
        }
        return "redirect:/dashboard";
    }

    @MessageMapping("/challenge/join")
    @SendTo("/topic/challenge")
    public String handleJoinRequest(String message) {
        // Handle WebSocket messages for joining challenges
        return message;
    }
}