package com.dealxanh.app.controller.advice;

import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.UserRepository;
import com.dealxanh.app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/**
 * Global model attributes injected into every controller's model.
 * Ensures all pages using the shared buyer header/top-nav have:
 * - currentUserFullName / currentUserInitials → desktop avatar pill vs login/register buttons
 * - notifCount → notification badge count
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal) {
        int notifCount = 0;
        String currentUserFullName = null;
        String currentUserInitials = null;
        String currentUserAvatar = null;
        String currentUserEmail = null;

        if (principal != null) {
            try {
                User user = getUserFromPrincipal(principal);
                if (user != null) {
                    notifCount = (int) notificationService.getUnreadCount(user);
                    currentUserFullName = user.getFullName();
                    if (currentUserFullName != null && !currentUserFullName.trim().isEmpty()) {
                        String[] parts = currentUserFullName.trim().split("\\s+");
                        if (parts.length == 1) {
                            currentUserInitials = parts[0].substring(0, 1).toUpperCase();
                        } else {
                            currentUserInitials = (parts[0].substring(0, 1)
                                    + parts[parts.length - 1].substring(0, 1)).toUpperCase();
                        }
                    }
                    // Pass avatar URL to model for desktop avatar pill
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
                        currentUserAvatar = user.getAvatarUrl().trim();
                    }
                    // Pass email for profile popup
                    if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                        currentUserEmail = user.getEmail().trim();
                    }
                } else {
                    System.err.println("GlobalModelAdvice: Principal '" + principal.getName()
                            + "' exists but user NOT FOUND in DB (type=" + principal.getClass().getSimpleName() + ")");
                }
            } catch (Exception e) {
                // DB or service error — fail gracefully, don't block page rendering
                // currentUserFullName remains null → header shows login/register
                System.err.println("GlobalModelAdvice: Failed to load user attributes — " + e.getMessage());
                e.printStackTrace();
            }
        }

        model.addAttribute("notifCount", notifCount);
        model.addAttribute("currentUserFullName", currentUserFullName);
        model.addAttribute("currentUserInitials", currentUserInitials);
        model.addAttribute("currentUserAvatar", currentUserAvatar);
        model.addAttribute("currentUserEmail", currentUserEmail);
    }

    /**
     * Look up user from Principal, trying username first then email.
     * Matches the pattern used across all controllers.
     */
    private User getUserFromPrincipal(Principal principal) {
        if (principal == null || principal.getName() == null) return null;
        String name = principal.getName();
        User user = userRepository.findByUsername(name).orElse(null);
        if (user == null) user = userRepository.findByEmail(name).orElse(null);
        return user;
    }
}
