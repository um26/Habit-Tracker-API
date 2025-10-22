package com.habittracker.api.controller;

import com.habittracker.api.model.Notification;
import com.habittracker.api.model.User;
import com.habittracker.api.repository.NotificationRepository;
import com.habittracker.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private static final int PAGE_SIZE = 10;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/notifications")
    public String showNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String filter,
            Model model,
            Principal principal) {
        
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Adjust page number to be 0-based for Spring Pagination
        int pageNumber = Math.max(0, page - 1);
        PageRequest pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Notification> notifications;
        if ("unread".equals(filter)) {
            notifications = notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(currentUser, false, pageRequest);
            model.addAttribute("filter", "unread");
        } else if ("read".equals(filter)) {
            notifications = notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(currentUser, true, pageRequest);
            model.addAttribute("filter", "read");
        } else {
            notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser, pageRequest);
        }

        model.addAttribute("pageId", "notifications");
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifications.getTotalPages());
        
        return "notifications";
    }

    @GetMapping("/notifications/{id}/read")
    public String markAsReadAndRedirect(
            @PathVariable Long id,
            @RequestParam(defaultValue = "/dashboard") String redirectTo,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
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
            
            // If redirecting to notifications page, add success message
            if (redirectTo.startsWith("/notifications")) {
                redirectAttributes.addFlashAttribute("success", "Notification marked as read.");
            }
            
            // Redirect to the link specified in the notification (or dashboard if none)
            return "redirect:" + (notification.getLink() != null && !notification.getLink().isBlank() ? notification.getLink() : redirectTo);
        } else {
            log.warn("Attempt to mark invalid or unauthorized notification {} as read by user {}", id, email);
            redirectAttributes.addFlashAttribute("error", "Invalid notification or access denied.");
            return "redirect:/notifications";
        }
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}