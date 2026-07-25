package com.dealxanh.app.security;

import com.dealxanh.app.entity.AppConfig;
import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.AppConfigRepository;
import com.dealxanh.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;
import java.util.Optional;

/**
 * Maintenance mode interceptor.
 * When maintenance mode is ON, redirects non-staff users (buyers, guests)
 * to /maintenance page. Admin/seller/staff can bypass to test the site.
 * <p>
 * Cache: DB is checked at most every 5 seconds to avoid hitting DB on every request.
 */
@Component
public class MaintenanceInterceptor implements HandlerInterceptor {

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Autowired
    private UserRepository userRepository;

    /** Cached maintenance mode state */
    private volatile boolean cachedMode = false;
    private volatile long lastCheckTime = 0;
    private static final long CACHE_TTL_MS = 5_000; // 5 seconds

    /** Allowed path prefixes that always bypass maintenance mode */
    private static final String[] BYPASS_PREFIXES = {
        "/admin", "/moderator", "/seller", "/staff",
        "/login", "/error", "/maintenance",
        "/css/", "/js/", "/img/", "/images/", "/uploads/", "/static/",
        "/favicon.ico", "/perform_login", "/logout",
        "/api/", "/oauth2/"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        String path = request.getRequestURI();

        // Always allow bypass prefixes
        for (String prefix : BYPASS_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        // Check if maintenance mode is ON
        if (!isMaintenanceMode()) {
            return true;
        }

        // Maintenance is ON — check if user has staff+ role
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            User user = lookupUser(principal);
            if (user != null && isStaffOrAbove(user)) {
                return true; // Allow staff/seller/admin through
            }
        }

        // Redirect to maintenance page
        response.sendRedirect("/maintenance");
        return false;
    }

    /**
     * Check maintenance mode with 5-second cache.
     */
    private boolean isMaintenanceMode() {
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < CACHE_TTL_MS) {
            return cachedMode;
        }
        synchronized (this) {
            if (now - lastCheckTime < CACHE_TTL_MS) {
                return cachedMode;
            }
            try {
                Optional<AppConfig> config = appConfigRepository.findByConfigKey("maintenance_mode");
                cachedMode = config.isPresent() && "true".equalsIgnoreCase(config.get().getConfigValue());
            } catch (Exception e) {
                // DB error — default to OFF (don't block the site)
                cachedMode = false;
            }
            lastCheckTime = System.currentTimeMillis();
            return cachedMode;
        }
    }

    /**
     * Force refresh the cache on next check. Called after admin toggles the setting.
     */
    public void refreshCache() {
        lastCheckTime = 0;
    }

    /**
     * Look up user from Principal, trying username first then email.
     */
    private User lookupUser(Principal principal) {
        if (principal == null || principal.getName() == null) return null;
        String name = principal.getName();
        User user = userRepository.findByUsername(name).orElse(null);
        if (user == null) user = userRepository.findByEmail(name).orElse(null);
        return user;
    }

    /**
     * Check if user has admin, moderator, store owner, or store staff role.
     */
    private boolean isStaffOrAbove(User user) {
        Role role = user.getRole();
        if (role == null) return false;
        String roleName = role.getName();
        if (roleName == null) return false;
        return roleName.contains("ADMIN") || roleName.contains("MODERATOR")
            || roleName.contains("STORE_OWNER") || roleName.contains("STORE_STAFF");
    }
}
