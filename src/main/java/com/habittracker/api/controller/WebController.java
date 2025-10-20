package com.habittracker.api.controller;

import com.habittracker.api.model.*;
import com.habittracker.api.repository.*;
import com.habittracker.api.service.EmailService;
import com.habittracker.api.service.HabitService;
import com.habittracker.api.service.NotificationService;
import com.habittracker.api.service.TurnstileService;
import com.habittracker.api.service.TwoFactorAuthenticationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private static final Logger log = LoggerFactory.getLogger(WebController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private HabitRepository habitRepository;
    @Autowired private HabitLogRepository habitLogRepository;
    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private VerificationTokenRepository verificationTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private HabitService habitService;
    @Autowired private NotificationService notificationService;
    @Autowired private EmailService emailService;
    @Autowired private TwoFactorAuthenticationService tfaService;
    @Autowired private TurnstileService turnstileService;

    @Value("${app.cutover-hour:0}") private int cutoverHour;
    @Value("${cloudflare.turnstile.siteKey}") private String turnstileSiteKey;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal) {
        if (principal != null) {
            User currentUser = getCurrentUser(principal);
            if (currentUser != null) {
                List<Notification> unreadNotifications = notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(currentUser, false);
                model.addAttribute("unreadNotifications", unreadNotifications);
            }
        }
        model.addAttribute("turnstileSiteKey", turnstileSiteKey);
    }

    @GetMapping("/") public String home() { return "index"; }
    @GetMapping("/login") public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               @RequestParam("confirmPassword") String confirmPassword,
                               @RequestParam("cf-turnstile-response") String turnstileToken,
                               RedirectAttributes redirectAttributes) {

        log.info("Attempting registration for email: {}", user.getEmail());

        boolean isHuman = turnstileService.verifyTurnstile(turnstileToken).blockOptional().orElse(false);
        if (!isHuman) {
            log.warn("Turnstile verification failed for email {}", user.getEmail());
            redirectAttributes.addFlashAttribute("error", "Human verification failed. Please try again.");
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/register";
        }
        log.info("Turnstile verification succeeded for email {}", user.getEmail());

        if (!user.getPassword().equals(confirmPassword)) {
            bindingResult.rejectValue("password", "error.user", "Passwords do not match.");
            log.warn("Registration failed: Passwords do not match for email {}", user.getEmail());
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.user", "Email already exists.");
            log.warn("Registration failed: Email {} already exists", user.getEmail());
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "error.user", "Username already exists.");
            log.warn("Registration failed: Username {} already exists", user.getUsername());
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            log.warn("Registration failed due to validation errors for email {}", user.getEmail());
            return "redirect:/register";
        }

        String uniqueId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 7);
        user.setUniqueUserId(uniqueId);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);

        log.info("User {} saved (disabled). Verification email sent.", user.getEmail());
        return "redirect:/registration-success";
    }

    @GetMapping("/registration-success")
    public String registrationSuccessPage() {
        return "registration-success";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        log.info("Verify-email endpoint called with token: {}", token);
        
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            log.error("Token not found in database");
            redirectAttributes.addFlashAttribute("error", "Invalid verification token.");
            return "redirect:/login";
        }

        User user = verificationToken.getUser();
        if (user == null) {
            log.error("User is null - relationship broken");
            redirectAttributes.addFlashAttribute("error", "User data is corrupted.");
            return "redirect:/login";
        }
        
        log.info("User found: {}, enabled before: {}", user.getEmail(), user.isEnabled());

        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            log.error("Token expired");
            redirectAttributes.addFlashAttribute("error", "Verification token has expired.");
            return "redirect:/login";
        }

        // THE KEY FIX: Fetch fresh from database, modify, and save
        User freshUser = userRepository.findById(user.getId()).orElse(null);
        if (freshUser == null) {
            log.error("User not found in database by ID");
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }
        
        freshUser.setEnabled(true);
        log.info("Setting enabled=true for user {}", freshUser.getEmail());
        
        userRepository.save(freshUser);
        log.info("User saved. Verifying...");
        
        // Verify the save actually worked
        User verify = userRepository.findById(freshUser.getId()).orElse(null);
        log.info("Verification: User {} is now enabled={}", verify != null ? verify.getEmail() : "null", 
                verify != null ? verify.isEnabled() : "N/A");

        verificationTokenRepository.delete(verificationToken);
        log.info("Verification token deleted");

        log.info("Email verified successfully for user {}", freshUser.getEmail());
        redirectAttributes.addFlashAttribute("success", "Email verified successfully! Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/setup-2fa")
    public String setup2faPage(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login?error";
        if (user.isTwoFactorEnabled()) {
            redirectAttributes.addFlashAttribute("info", "2FA is already enabled.");
            return "redirect:/dashboard";
        }

        if (user.getTwoFactorSecret() == null || user.getTwoFactorSecret().isEmpty()) {
            user.setTwoFactorSecret(tfaService.generateNewSecret());
            userRepository.save(user);
            log.info("Generated new 2FA secret for user {}", user.getEmail());
        }

        model.addAttribute("secret", user.getTwoFactorSecret());
        model.addAttribute("qrCodeUri", tfaService.generateQrCodeImageUri(user.getTwoFactorSecret(), user.getEmail()));
        return "setup-2fa";
    }

    @PostMapping("/setup-2fa")
    public String verifyAndEnable2fa(@RequestParam("code") String code, Principal principal, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login?error";

        if (tfaService.isOtpValid(user.getTwoFactorSecret(), code)) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            log.info("2FA enabled successfully for user {}", user.getEmail());
            redirectAttributes.addFlashAttribute("success", "2FA enabled successfully!");
            return "redirect:/dashboard";
        } else {
            log.warn("Invalid 2FA code during setup for user {}", user.getEmail());
            redirectAttributes.addFlashAttribute("error", "Invalid verification code. Please try again.");
            redirectAttributes.addFlashAttribute("secret", user.getTwoFactorSecret());
            redirectAttributes.addFlashAttribute("qrCodeUri", tfaService.generateQrCodeImageUri(user.getTwoFactorSecret(), user.getEmail()));
            return "redirect:/setup-2fa";
        }
    }

    @GetMapping("/verify-2fa")
    public String verify2faPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("TFA_USER_EMAIL");
        if (email == null) {
            log.warn("Accessed /verify-2fa without TFA_USER_EMAIL in session. Redirecting to login.");
            return "redirect:/login";
        }
        model.addAttribute("email", email);
        return "verify-2fa";
    }

    @PostMapping("/verify-2fa")
    public String verify2faCode(@RequestParam("code") String code, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("TFA_USER_EMAIL");
        if (email == null) {
            log.warn("POST to /verify-2fa without TFA_USER_EMAIL in session. Redirecting to login.");
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User from session not found: " + email));

        if (tfaService.isOtpValid(user.getTwoFactorSecret(), code)) {
            log.info("2FA verification successful for user {}", email);
            session.removeAttribute("TFA_USER_EMAIL");

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                 email, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            return "redirect:/dashboard"; // Redirect to GET /dashboard

        } else {
            log.warn("Invalid 2FA code during login verification for user {}", email);
            redirectAttributes.addFlashAttribute("error", "Invalid verification code.");
            return "redirect:/verify-2fa";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login?error";

        List<Habit> habits = habitRepository.findByUserId(user.getId());
        // --- THE FIX IS HERE: Changed getCurrentStreak to calculateCurrentStreak ---
        Map<Long, Integer> streaks = habits.stream()
                .collect(Collectors.toMap(Habit::getId, habit -> habitService.calculateCurrentStreak(habit.getId())));
        int dailyStreak = habitService.calculateDailyStreak(user.getId());
        model.addAttribute("dailyStreak", dailyStreak);
        model.addAttribute("habits", habits);
        model.addAttribute("streaks", streaks);
        model.addAttribute("newHabit", new Habit());
        return "dashboard";
    }

    @PostMapping("/habits")
    public String addHabit(@ModelAttribute Habit newHabit, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login?error";
        newHabit.setUser(user);
        habitRepository.save(newHabit);
        log.info("User {} added habit '{}'", user.getEmail(), newHabit.getName());
        return "redirect:/dashboard";
    }

    @PostMapping("/habits/{habitId}/log")
    public String logHabitCompletion(@PathVariable Long habitId, Principal principal, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login?error";

        try {
            Optional<Habit> habitOpt = habitRepository.findByIdAndUserId(habitId, user.getId());
            if (habitOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Habit not found or access denied.");
                log.warn("User {} failed to log completion for non-existent/unauthorized habit {}", user.getEmail(), habitId);
                return "redirect:/dashboard";
            }

            Habit habit = habitOpt.get();
            LocalDate today = LocalDate.now();
            LocalTime nowTime = LocalTime.now();

            if (cutoverHour > 0 && nowTime.getHour() < cutoverHour) {
                today = today.minusDays(1);
                log.debug("Applying cutoverHour={}, treating time {} as previous day {}", cutoverHour, nowTime, today);
            }

            log.info("User {} logging completion for habit {} on date {}", user.getEmail(), habit.getId(), today);

            boolean alreadyLogged = habitLogRepository.existsByHabitAndCompletionDate(habit, today);
            if (alreadyLogged) {
                log.info("Duplicate log prevented for user {} habit {} date {}", user.getEmail(), habit.getId(), today);
                redirectAttributes.addFlashAttribute("info", "Habit already logged for " + today);
            } else {
                HabitLog newLog = new HabitLog();
                newLog.setHabit(habit);
                newLog.setCompletionDate(today);
                habitLogRepository.save(newLog);
                log.info("Saved new HabitLog id={} for user {} habit={} date={}", newLog.getId(), user.getEmail(), habit.getId(), newLog.getCompletionDate());
                redirectAttributes.addFlashAttribute("success", "Logged habit completion for " + today);
            }
        } catch (Exception ex) {
            log.error("Failed to log habit completion for user {} habit {}: {}", user.getEmail(), habitId, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("error", "Error logging habit.");
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/account")
    public String accountPage(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) return "redirect:/login?error";
        model.addAttribute("user", user);
        return "account";
    }

    @GetMapping("/friends")
    public String friendsPage(Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) return "redirect:/login?error";

        List<Friendship> pendingRequests = friendshipRepository.findByFriendAndStatus(currentUser, Friendship.FriendshipStatus.PENDING);
        List<FriendDTO> friends = friendshipRepository.findAcceptedFriendships(currentUser).stream()
            .map(f -> f.getUser().equals(currentUser) ? f.getFriend() : f.getUser())
            .map(friend -> new FriendDTO(
                friend.getUsername(),
                friend.getUniqueUserId(),
                habitService.calculateDailyStreak(friend.getId())
            ))
            .collect(Collectors.toList());

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("friends", friends);
        return "friends";
    }

    @PostMapping("/friends/{friendId}/nudge")
    public String nudgeFriend(@PathVariable String friendId, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) return "redirect:/login?error";
        User friendToNudge = userRepository.findByUniqueUserId(friendId).orElse(null);

        if (friendToNudge != null) {
            boolean areFriends = friendshipRepository.findFriendshipBetweenUsers(currentUser, friendToNudge)
                                    .map(f -> f.getStatus() == Friendship.FriendshipStatus.ACCEPTED)
                                    .orElse(false);
            if(areFriends) {
                notificationService.createNotification(friendToNudge, currentUser.getUsername() + " nudged you to complete a habit!", "/dashboard");
                redirectAttributes.addFlashAttribute("success", "Nudge sent to " + friendToNudge.getUsername() + "!");
                log.info("User {} nudged user {}", currentUser.getEmail(), friendToNudge.getEmail());
            } else {
                 redirectAttributes.addFlashAttribute("error", "You can only nudge friends.");
                 log.warn("User {} failed to nudge non-friend {}", currentUser.getEmail(), friendId);
            }
        } else {
             redirectAttributes.addFlashAttribute("error", "User not found.");
             log.warn("User {} failed to nudge non-existent user {}", currentUser.getEmail(), friendId);
        }
        return "redirect:/friends";
    }

    @GetMapping("/explore")
    public String explorePage(@RequestParam(value = "query", required = false) String query, Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) return "redirect:/login?error";
        Map<String, String> friendStatus = new HashMap<>();

        if (query != null && !query.trim().isEmpty()) {
            List<User> users = userRepository.findByUsernameOrUniqueUserId(query, query)
                                    .stream()
                                    .filter(u -> !u.getId().equals(currentUser.getId()))
                                    .collect(Collectors.toList());
            log.info("Explore search for '{}' found {} users", query, users.size());

            for (User user : users) {
                Optional<Friendship> friendshipOpt = friendshipRepository.findFriendshipBetweenUsers(currentUser, user);
                if (friendshipOpt.isPresent()) {
                    Friendship friendship = friendshipOpt.get();
                    if (friendship.getStatus() == Friendship.FriendshipStatus.ACCEPTED) {
                        friendStatus.put(user.getUniqueUserId(), "FRIENDS");
                    } else {
                        if(friendship.getUser().getId().equals(currentUser.getId())){
                             friendStatus.put(user.getUniqueUserId(), "PENDING_SENT");
                        } else {
                             friendStatus.put(user.getUniqueUserId(), "PENDING_RECEIVED");
                        }
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
        if (currentUser == null) return "redirect:/login?error";
        User friendToAdd = userRepository.findByUniqueUserId(friendId).orElse(null);

        if (friendToAdd == null || currentUser.getId().equals(friendToAdd.getId()) || friendshipRepository.findFriendshipBetweenUsers(currentUser, friendToAdd).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Cannot send request: User not found, is yourself, or already friends/pending.");
            log.warn("User {} failed friend request to {}: Invalid request", currentUser.getEmail(), friendId);
            return "redirect:/explore";
        }

        Friendship friendship = new Friendship();
        friendship.setUser(currentUser);
        friendship.setFriend(friendToAdd);
        friendship.setStatus(Friendship.FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);
        log.info("User {} sent friend request to user {}", currentUser.getEmail(), friendToAdd.getEmail());

        notificationService.createNotification(friendToAdd, currentUser.getUsername() + " sent you a friend request.", "/friends");
        redirectAttributes.addFlashAttribute("success", "Friend request sent to " + friendToAdd.getUsername() + "!");

        return "redirect:/explore?query=" + friendId;
    }

    @PostMapping("/friends/accept")
    public String acceptFriendRequest(@RequestParam("requestId") Long requestId, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) return "redirect:/login?error";
        Friendship friendship = friendshipRepository.findById(requestId).orElse(null);

        if (friendship != null && friendship.getFriend().equals(currentUser) && friendship.getStatus() == Friendship.FriendshipStatus.PENDING) {
            friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
            friendshipRepository.save(friendship);
            log.info("User {} accepted friend request from user {}", currentUser.getEmail(), friendship.getUser().getEmail());
            notificationService.createNotification(friendship.getUser(), currentUser.getUsername() + " accepted your friend request.", "/friends");
            redirectAttributes.addFlashAttribute("success", "Friend request accepted!");
        } else {
             redirectAttributes.addFlashAttribute("error", "Invalid friend request.");
             log.warn("User {} failed to accept invalid/non-existent friend request ID {}", currentUser.getEmail(), requestId);
        }
        return "redirect:/friends";
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            log.warn("Attempted to get current user but Principal was null.");
            return null;
        }
        String email = principal.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
    @GetMapping("/notifications/{notificationId}/read")
    public String markNotificationAsRead(@PathVariable Long notificationId, @RequestParam(required = false) String redirectTo, Principal principal) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        
        if (notification != null) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        
        // Redirect to the original destination, or dashboard if not specified
        return "redirect:" + (redirectTo != null ? redirectTo : "/dashboard");
    }
}
