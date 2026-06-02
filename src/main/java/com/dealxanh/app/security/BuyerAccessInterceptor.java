package com.dealxanh.app.security;

import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;

/**
 * Blocks non-buyer roles from accessing buyer pages.
 * Admin/Mod → /admin/dashboard, Seller/Staff → their dashboard.
 * Only ROLE_USER (buyer) can access buyer pages.
 */
@Component
public class BuyerAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        Principal principal = request.getUserPrincipal();
        if (principal == null) return true;

        String path = request.getRequestURI();
        // Allow: own role pages, APIs, static resources, auth, logout
        if (path.startsWith("/admin") || path.startsWith("/moderator")
                || path.startsWith("/api/") || path.startsWith("/seller")
                || path.startsWith("/staff") || path.startsWith("/css")
                || path.startsWith("/js") || path.startsWith("/img")
                || path.startsWith("/uploads") || path.startsWith("/favicon")
                || path.startsWith("/register") || path.startsWith("/forgot")
                || path.startsWith("/reset") || path.startsWith("/terms")
                || path.equals("/logout") || path.equals("/login")
                || path.equals("/admin/login") || path.equals("/seller/login")) {
            return true;
        }

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            user = userRepository.findByEmail(principal.getName()).orElse(null);
        }
        if (user == null) return true;

        Role role = user.getRole();
        if (role != null) {
            String roleName = role.getName();
            if ("ROLE_ADMIN".equals(roleName) || "ROLE_MODERATOR".equals(roleName)
                    || "ADMIN".equals(roleName) || "MODERATOR".equals(roleName)) {
                response.sendRedirect("/admin/dashboard");
                return false;
            }
            if ("ROLE_STORE_OWNER".equals(roleName) || "STORE_OWNER".equals(roleName)
                    || "ROLE_STORE_STAFF".equals(roleName) || "STORE_STAFF".equals(roleName)) {
                response.sendRedirect("/seller/dashboard");
                return false;
            }
        }
        return true;
    }
}
