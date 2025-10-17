package com.habittracker.api.controller;

import com.habittracker.api.model.*;
import com.habittracker.api.repository.*;
import com.habittracker.api.service.HabitService;
import com.habittracker.api.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.habittracker.api.exception.HabitNotFoundException;
import com.habittracker.api.exception.HabitAlreadyLoggedException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HabitRepository habitRepository;
    @Autowired
    private HabitLogRepository habitLogRepository;
    @Autowired
    private FriendshipRepository friendshipRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private HabitService habitService;
    @Autowired
    private NotificationService notificationService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal) {
        if (principal != null) {
            User currentUser = getCurrentUser(principal);
            List<Notification> unreadNotifications = notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(currentUser, false);
            model.addAttribute("unreadNotifications", unreadNotifications);
        }
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username already exists. Please choose another.");
            return "redirect:/register";
        }
        String uniqueId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 7);
        user.setUniqueUserId(uniqueId);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        List<Habit> habits = habitRepository.findByUserId(user.getId());
       Map<Long, Integer> currentStreaks = habits.stream()
        .collect(Collectors.toMap(Habit::getId, habit -> habitService.getCurrentStreak(habit.getId())));

        // If you want total daily streak for user, you need to implement a method in HabitService.
        // For now, you can call getCurrentStreak for a specific habit or just set to 0
        int dailyStreak = (int) habits.stream()
        .filter(habit -> habitService.getCurrentStreak(habit.getId()) > 0)
        .count();
        model.addAttribute("dailyStreak", dailyStreak);

        model.addAttribute("habits", habits);
        model.addAttribute("currentStreaks", currentStreaks);
        model.addAttribute("newHabit", new Habit());
        return "dashboard";
    }

    @PostMapping("/habits")
    public String addHabit(@ModelAttribute Habit newHabit, Principal principal) {
        newHabit.setUser(getCurrentUser(principal));
        habitRepository.save(newHabit);
        return "redirect:/dashboard";
    }

    @PostMapping("/habits/{habitId}/log")
    public String logHabitCompletion(@PathVariable Long habitId, Principal principal, RedirectAttributes redirectAttributes) {
        System.out.println("=== CONTROLLER: logHabitCompletion called with habitId: " + habitId);
        
        try {
            User user = getCurrentUser(principal);
            System.out.println("=== CONTROLLER: user found: " + user.getId());
            
            habitRepository.findByIdAndUserId(habitId, user.getId()).ifPresentOrElse(
                habit -> {
                    System.out.println("=== CONTROLLER: habit found, calling service");
                    habitService.logHabitCompletion(habitId);
                },
                () -> {
                    System.out.println("=== CONTROLLER: habit NOT found");
                    redirectAttributes.addFlashAttribute("error", "Habit not found!");
                }
            );
            redirectAttributes.addFlashAttribute("success", "Habit logged successfully!");
        } catch (HabitAlreadyLoggedException e) {
            System.out.println("=== CONTROLLER: HabitAlreadyLoggedException: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Habit already logged for today!");
        } catch (HabitNotFoundException e) {
            System.out.println("=== CONTROLLER: HabitNotFoundException: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Habit not found!");
        } catch (Exception e) {
            System.out.println("=== CONTROLLER: Unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/dashboard";
    }
    
    @GetMapping("/account")
    public String accountPage(Model model, Principal principal) {
        model.addAttribute("user", getCurrentUser(principal));
        return "account";
    }

    @GetMapping("/friends")
    public String friendsPage(Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);

        List<Friendship> pendingRequests = friendshipRepository.findByFriendAndStatus(currentUser, Friendship.FriendshipStatus.PENDING);

        List<FriendDTO> friends = friendshipRepository.findAcceptedFriendships(currentUser).stream()
            .map(f -> f.getUser().equals(currentUser) ? f.getFriend() : f.getUser())
            .map(friend -> new FriendDTO(
                friend.getUsername(),
                friend.getUniqueUserId(),
                habitService.getCurrentStreak(friend.getId())
            ))
            .collect(Collectors.toList());

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("friends", friends);
        return "friends";
    }

    @PostMapping("/friends/{friendId}/nudge")
    public String nudgeFriend(@PathVariable String friendId, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        User friendToNudge = userRepository.findByUniqueUserId(friendId).orElse(null);

        if (friendToNudge != null) {
            notificationService.createNotification(friendToNudge, currentUser.getUsername() + " nudged you to complete a habit!", "/dashboard");
            redirectAttributes.addFlashAttribute("success", "Nudge sent!");
        }

        return "redirect:/friends";
    }

    @GetMapping("/explore")
    public String explorePage(@RequestParam(value = "query", required = false) String query, Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        Map<String, String> friendStatus = new HashMap<>();

        if (query != null && !query.trim().isEmpty()) {
            List<User> users = userRepository.findByUsernameOrUniqueUserId(query, query);

            for (User user : users) {
                if (user.getId().equals(currentUser.getId())) continue;

                Optional<Friendship> friendshipOpt = friendshipRepository.findFriendshipBetweenUsers(currentUser, user);
                if (friendshipOpt.isPresent()) {
                    Friendship friendship = friendshipOpt.get();
                    if (friendship.getStatus() == Friendship.FriendshipStatus.ACCEPTED) {
                        friendStatus.put(user.getUniqueUserId(), "FRIENDS");
                    } else {
                        friendStatus.put(user.getUniqueUserId(), "PENDING");
                    }
                } else {
                    friendStatus.put(user.getUniqueUserId(), "NOT_FRIENDS");
                }
            }
            model.addAttribute("users", users);
        }

        model.addAttribute("friendStatus", friendStatus);
        return "explore";
    }

    @PostMapping("/friends/request")
    public String sendFriendRequest(@RequestParam("friendId") String friendId, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        User friendToNudge = userRepository.findByUniqueUserId(friendId).orElse(null);

        if (friendToNudge == null || currentUser.getId().equals(friendToNudge.getId()) || friendshipRepository.findFriendshipBetweenUsers(currentUser, friendToNudge).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Invalid request or already friends/pending.");
            return "redirect:/explore";
        }

        Friendship friendship = new Friendship();
        friendship.setUser(currentUser);
        friendship.setFriend(friendToNudge);
        friendship.setStatus(Friendship.FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);

        notificationService.createNotification(friendToNudge, currentUser.getUsername() + " sent you a friend request.", "/friends");

        return "redirect:/explore?query=" + friendId;
    }

    @PostMapping("/friends/accept")
    public String acceptFriendRequest(@RequestParam("requestId") Long requestId, Principal principal) {
        User currentUser = getCurrentUser(principal);
        Friendship friendship = friendshipRepository.findById(requestId).orElse(null);

        if (friendship != null && friendship.getFriend().equals(currentUser) && friendship.getStatus() == Friendship.FriendshipStatus.PENDING) {
            friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
            friendshipRepository.save(friendship);
        }
        return "redirect:/friends";
    }

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Cannot find logged in user"));
    }
}