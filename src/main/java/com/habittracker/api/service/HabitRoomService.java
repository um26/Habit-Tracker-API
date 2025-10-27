package com.habittracker.api.service;

import com.habittracker.api.model.*;
import com.habittracker.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional; // <-- ADD THIS
import java.util.stream.Collectors; // <-- ADD THIS

@Service
public class HabitRoomService {

    @Autowired
    private HabitRoomRepository habitRoomRepository;
    
    @Autowired
    private HabitRoomMemberRepository habitRoomMemberRepository;
    
    @Autowired
    private HabitRoomLogRepository habitRoomLogRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired // <-- ADD THIS
    private NotificationService notificationService; // <-- ADD THIS

    @Transactional
    public HabitRoom createRoom(User creator, String habitName, String description, String dailyGoal) {
        HabitRoom room = new HabitRoom();
        room.setRoomCode(generateUniqueRoomCode());
        room.setHabitName(habitName);
        room.setDescription(description);
        room.setDailyGoal(dailyGoal);
        room.setCreatedBy(creator);
        room.setCreatedAt(LocalDateTime.now());
        room.setActive(true);
        room.setCurrentStreak(0);
        
        habitRoomRepository.save(room);
        
        // Automatically add creator as first member
        HabitRoomMember member = new HabitRoomMember();
        member.setHabitRoom(room);
        member.setUser(creator);
        member.setJoinedAt(LocalDateTime.now());
        member.setHasCompletedToday(false);
        member.setStatus(HabitRoomMember.MemberStatus.ACTIVE);
        
        habitRoomMemberRepository.save(member);
        
        return room;
    }

    @Transactional
    public boolean joinRoom(String roomCode, User user) {
        HabitRoom room = habitRoomRepository.findByRoomCode(roomCode).orElse(null);
        
        if (room == null || !room.isActive()) {
            return false;
        }
        
        // Check if already member
        if (habitRoomMemberRepository.existsByHabitRoomAndUser(room, user)) {
            return false;
        }
        
        HabitRoomMember member = new HabitRoomMember();
        member.setHabitRoom(room);
        member.setUser(user);
        member.setJoinedAt(LocalDateTime.now());
        member.setHasCompletedToday(false);
        member.setStatus(HabitRoomMember.MemberStatus.ACTIVE);
        
        habitRoomMemberRepository.save(member);
        
        // Notify all room members via WebSocket
        messagingTemplate.convertAndSend("/topic/room/" + roomCode, 
            new RoomUpdateMessage("USER_JOINED", user.getUsername() + " joined the room!")); // <-- This needs RoomUpdateMessage.java
        
        // Notify creator
        notificationService.createNotification(room.getCreatedBy(), user.getUsername() + " joined your room: " + room.getHabitName(), "/rooms/" + room.getRoomCode());

        return true;
    }

    @Transactional
    public boolean markComplete(String roomCode, User user) {
        HabitRoom room = habitRoomRepository.findByRoomCode(roomCode).orElse(null);
        if (room == null) return false;
        
        HabitRoomMember member = habitRoomMemberRepository.findByHabitRoomAndUser(room, user).orElse(null);
        if (member == null || member.getStatus() != HabitRoomMember.MemberStatus.ACTIVE) {
            return false;
        }
        
        // Mark as completed
        member.setHasCompletedToday(true);
        member.setLastCompletedAt(LocalDateTime.now());
        habitRoomMemberRepository.save(member);
        
        // Notify room via WebSocket
        Map<String, Object> userCompletedPayload = new HashMap<>();
        userCompletedPayload.put("type", "USER_COMPLETED");
        userCompletedPayload.put("roomCode", roomCode);
        userCompletedPayload.put("userId", user.getId());
        userCompletedPayload.put("username", user.getUsername());
        userCompletedPayload.put("message", user.getUsername() + " completed their task!");
        messagingTemplate.convertAndSend("/topic/room/" + roomCode, userCompletedPayload);

        // Check if all members completed
        int totalMembers = habitRoomMemberRepository.countTotalActiveMembers(room);
        int completedMembers = habitRoomMemberRepository.countCompletedMembers(room);
        
        if (completedMembers == totalMembers && totalMembers > 0) {
            // All members completed! Log it and update streak
            completeRoomForDay(room);
            
            // Notify room of completion
            Map<String, Object> roomCompletedPayload = new HashMap<>();
            roomCompletedPayload.put("type", "ROOM_COMPLETED");
            roomCompletedPayload.put("roomCode", roomCode);
            roomCompletedPayload.put("currentStreak", room.getCurrentStreak()); // Use the updated value
            roomCompletedPayload.put("message", "All members completed! Streak: " + room.getCurrentStreak());
            messagingTemplate.convertAndSend("/topic/room/" + roomCode, roomCompletedPayload);
        }
        
        return true;
    }

    @Transactional
    private void completeRoomForDay(HabitRoom room) {
        LocalDate today = LocalDate.now();
        
        if (habitRoomLogRepository.existsByHabitRoomAndCompletionDate(room, today)) {
            return;
        }
        
        HabitRoomLog lastLog = habitRoomLogRepository.findTopByHabitRoomOrderByCompletionDateDesc(room).orElse(null);
        
        int newStreak = 1;
        if (lastLog != null && lastLog.getCompletionDate().equals(today.minusDays(1))) {
            newStreak = lastLog.getStreakCount() + 1;
        }
        
        room.setCurrentStreak(newStreak);
        habitRoomRepository.save(room);
        
        HabitRoomLog log = new HabitRoomLog();
        log.setHabitRoom(room);
        log.setCompletionDate(today);
        log.setCompletedAt(LocalDateTime.now());
        log.setStreakCount(newStreak);
        log.setAllMembersCompleted(true);
        
        List<Long> memberIds = habitRoomMemberRepository.findActiveMembers(room)
            .stream()
            .map(m -> m.getUser().getId())
            .toList();
        log.setCompletedByUserIds(memberIds);
        
        habitRoomLogRepository.save(log);
        
        // Reset daily completion flags for this room
        habitRoomMemberRepository.resetDailyCompletionForRoom(room.getId());
    }

    public HabitRoom getRoomByCode(String roomCode) {
        return habitRoomRepository.findByRoomCode(roomCode).orElse(null);
    }

    public List<HabitRoom> getUserRooms(User user) {
        return habitRoomRepository.findActiveRoomsByUser(user);
    }

    public List<HabitRoomMember> getRoomMembers(HabitRoom room) {
        return habitRoomMemberRepository.findActiveMembers(room);
    }
    
    public boolean isMember(HabitRoom room, User user) {
        return habitRoomMemberRepository.existsByHabitRoomAndUser(room, user);
    }

    private String generateUniqueRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (habitRoomRepository.existsByRoomCode(code));
        
        return code;
    }
}