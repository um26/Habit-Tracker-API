package com.habittracker.api.controller;

import com.habittracker.api.model.Notification;
import com.habittracker.api.model.User;
import com.habittracker.api.repository.NotificationRepository;
import com.habittracker.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/notifications/{id}/read")
    public String markAsReadAndRedirect(@PathVariable Long id,
                                        @RequestParam(defaultValue = "/dashboard") String redirectTo,
                                        Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String email = principal.getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        Notification notification = notificationRepository.findById(id).orElse(null);

        // Check if notification exists and belongs to the current user
        if (currentUser != null && notification != null && notification.getRecipient().equals(currentUser)) {
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Marked notification {} as read for user {}", id, email);
            // Redirect to the link specified in the notification (or dashboard if none)
            return "redirect:" + (notification.getLink() != null && !notification.getLink().isBlank() ? notification.getLink() : redirectTo);
        } else {
            log.warn("Attempt to mark invalid or unauthorized notification {} as read by user {}", id, email);
            // Redirect to dashboard if something is wrong
            return "redirect:/dashboard";
        }
    }

    // Helper (Optional - can be moved to WebController if preferred)
    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}