package com.dealxanh.app.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @org.springframework.beans.factory.annotation.Autowired
    private com.dealxanh.app.repository.StoreRepository storeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.dealxanh.app.repository.UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        System.out.println("=== AUTH SUCCESS START ===");
        System.out.println("DEBUG: Authentication success for user: " + authentication.getName());

        // Explicitly set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String redirectUrl = "/home"; // Default for buyers
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        System.out.println("DEBUG: Authorities: " + authorities);
        System.out.println("DEBUG: Session ID before redirect: " + request.getSession(false) != null ? request.getSession().getId() : "null");

        boolean roleFound = false;

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            System.out.println("DEBUG: Checking role: " + role);

            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_MODERATOR")) {
                redirectUrl = "/admin/dashboard";
                roleFound = true;
                System.out.println("DEBUG: Admin/Moderator role, redirecting to: " + redirectUrl);
                break;
            } else if (role.equals("ROLE_STORE_OWNER")) {
                // Check store status
                String username = authentication.getName();
                com.dealxanh.app.entity.User user = userRepository.findByUsername(username)
                        .orElse(userRepository.findByEmail(username).orElse(null));

                // If still not found, try searching by email using the list method
                if (user == null) {
                    java.util.List<com.dealxanh.app.entity.User> users = userRepository.findAllByEmailWithRole(username);
                    if (!users.isEmpty()) {
                        user = users.get(0);
                    }
                }

                if (user != null) {
                    java.util.Optional<com.dealxanh.app.entity.Store> storeOpt = storeRepository.findByOwner(user);
                    if (storeOpt.isPresent()) {
                        String status = storeOpt.get().getStatus();
                        if ("PENDING".equals(status) || "REJECTED".equals(status)) {
                            redirectUrl = "/seller/onboarding-pending";
                            System.out.println("DEBUG: Store status: " + status + ", redirecting to: " + redirectUrl);
                        } else {
                            redirectUrl = "/seller/dashboard";
                            System.out.println("DEBUG: Store approved, redirecting to: " + redirectUrl);
                        }
                    } else {
                        // No store yet, go to registration
                        redirectUrl = "/seller/register";
                        System.out.println("DEBUG: No store found, redirecting to: " + redirectUrl);
                    }
                } else {
                    redirectUrl = "/seller/register";
                    System.out.println("DEBUG: User not found, redirecting to: " + redirectUrl);
                }
                roleFound = true;
                break;
            } else if (role.equals("ROLE_STORE_STAFF")) {
                redirectUrl = "/seller/dashboard";
                roleFound = true;
                System.out.println("DEBUG: Staff role, redirecting to: " + redirectUrl);
                break;
            } else if (role.equals("ROLE_USER") || role.equals("ROLE_CUSTOMER")) {
                redirectUrl = "/home";
                roleFound = true;
                System.out.println("DEBUG: Buyer role, redirecting to: " + redirectUrl);
                break;
            }
        }

        if (!roleFound) {
            System.out.println("DEBUG: No matching role found, defaulting to /home");
            redirectUrl = "/home";
        }

        System.out.println("DEBUG: Final redirect URL: " + redirectUrl);
        System.out.println("DEBUG: Redirecting...");

        response.sendRedirect(redirectUrl);
        System.out.println("=== AUTH SUCCESS END ===");
    }
}
