package com.dealxanh.app.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    // Trang đăng nhập của User (Buyer)
    @GetMapping("/login")
    public String buyerLogin(jakarta.servlet.http.HttpServletRequest request) {
        // Clear buyer-related session data when accessing login page
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            // Remove specific attributes instead of invalidating entire session
            // This prevents Spring Security issues
            session.removeAttribute("user");
            session.removeAttribute("cart");
            session.removeAttribute("orderData");
            // IMPORTANT: Also clear security context to ensure clean state
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        return "buyer/login";
    }

    // Trang đăng nhập của Admin / Moderator
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    // Trang đăng nhập của Store Owner / Staff
    @GetMapping("/seller/login")
    public String sellerLogin() {
        return "seller/login";
    }

    // Logout endpoint for buyer (trả về JSON cho AJAX)
    @GetMapping("/api/auth/logout")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, String>> buyerLogoutApi(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();

        try {
            System.out.println("=== BUYER LOGOUT API START ===");

            // Invalidate session FIRST
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session != null) {
                System.out.println("DEBUG: Invalidating session: " + session.getId());
                session.invalidate();
            }

            // Clear security context IMMEDIATELY
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(null);
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            System.out.println("DEBUG: Security context cleared");

            // Delete ALL cookies including JSESSIONID and remember-me
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                System.out.println("DEBUG: Deleting cookie: " + cookie.getName() + "=" + cookie.getValue());
                cookie.setMaxAge(0);
                cookie.setPath("/");
                cookie.setValue("");
                response.addCookie(cookie);
            }

            result.put("success", "true");
            result.put("message", "Logged out successfully");
            System.out.println("=== BUYER LOGOUT API END ===");
            return org.springframework.http.ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("ERROR in logout: " + e.getMessage());
            e.printStackTrace();
            result.put("success", "false");
            result.put("error", e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(result);
        }
    }

    // Trang chọn vai trò Đăng ký (Người mua / Cửa hàng)
    @GetMapping("/register")
    public String roleSelect() {
        return "auth/role-select";
    }

    // Trang đăng ký Người mua (User)
    @GetMapping("/register/buyer")
    public String registerBuyer() {
        return "auth/register-buyer";
    }

    // Trang đăng ký Chủ Cửa hàng (Owner)
    @GetMapping("/seller/register")
    public String registerSeller(java.security.Principal principal, org.springframework.ui.Model model) {
        if (principal != null) {
            String username = principal.getName();
            com.dealxanh.app.entity.User user = userRepository.findByUsername(username)
                    .orElse(userRepository.findByEmail(username).orElse(null));
            if (user != null) {
                model.addAttribute("userObj", user);
                java.util.Optional<com.dealxanh.app.entity.Store> storeOpt = storeRepository.findByOwner(user);
                storeOpt.ifPresent(store -> {
                    model.addAttribute("storeObj", store);
                    // Add document URLs for file pre-loading
                    model.addAttribute("cccdUrl", store.getCccdUrl());
                    model.addAttribute("licenseUrl", store.getBusinessLicenseUrl());
                    model.addAttribute("vsattpUrl", store.getVsattpUrl());
                    model.addAttribute("logoUrl", store.getLogoUrl());
                });
            }
        }
        return "auth/register-seller";
    }

    // Trang Quên mật khẩu chung (User)
    @GetMapping("/terms")
    public String termsPage() {
        return "auth/terms";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordUser() {
        return "auth/forgot-password";
    }

    // Trang Quên mật khẩu của Chủ Cửa hàng / Nhân viên
    @GetMapping("/seller/forgot-password")
    public String forgotPasswordSeller() {
        return "auth/forgot-password";
    }

    // Trang Quên mật khẩu của Admin / Moderator
    @GetMapping("/admin/forgot-password")
    public String forgotPasswordAdmin() {
        return "auth/forgot-password";
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.dealxanh.app.repository.UserRepository userRepository;
    
    @org.springframework.beans.factory.annotation.Autowired
    private com.dealxanh.app.repository.StoreRepository storeRepository;

    @GetMapping({"/seller/onboarding-pending", "/auth/onboarding-pending"})
    public String onboardingPending(java.security.Principal principal, org.springframework.ui.Model model) {
        if (principal == null) {
            return "redirect:/seller/login";
        }

        // Set default values to avoid null issues in template
        model.addAttribute("storeStatus", "PENDING");
        model.addAttribute("storeName", "");
        model.addAttribute("submittedAt", null);
        model.addAttribute("hasCccd", false);
        model.addAttribute("hasLicense", false);
        model.addAttribute("hasVsattp", false);
        model.addAttribute("hasBank", false);
        model.addAttribute("pickupSlots", "");
        model.addAttribute("ownerEmail", "");
        model.addAttribute("ownerName", "");

        String username = principal.getName();
        com.dealxanh.app.entity.User user = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));

        if (user != null) {
            model.addAttribute("ownerEmail", user.getEmail() != null ? user.getEmail() : "");
            model.addAttribute("ownerName", user.getFullName() != null ? user.getFullName() : "");

            java.util.Optional<com.dealxanh.app.entity.Store> storeOpt = storeRepository.findByOwner(user);
            if (storeOpt.isPresent()) {
                com.dealxanh.app.entity.Store store = storeOpt.get();
                model.addAttribute("storeStatus", store.getStatus() != null ? store.getStatus() : "PENDING");
                model.addAttribute("storeName", store.getStoreName() != null ? store.getStoreName() : "");
                model.addAttribute("submittedAt", store.getCreatedAt());
                model.addAttribute("hasCccd", store.getCccdUrl() != null && !store.getCccdUrl().isEmpty());
                model.addAttribute("hasLicense", store.getBusinessLicenseUrl() != null && !store.getBusinessLicenseUrl().isEmpty());
                model.addAttribute("hasVsattp", store.getVsattpUrl() != null && !store.getVsattpUrl().isEmpty());
                model.addAttribute("hasBank", store.getBankAccountNumber() != null && !store.getBankAccountNumber().isEmpty());
                model.addAttribute("pickupSlots", store.getPickupSlots() != null ? store.getPickupSlots() : "");
            }
        }
        return "auth/onboarding-pending";
    }

    @GetMapping("/seller/logout")
    public String logoutSeller(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/seller/login";
    }

    // Trang thông tin tài khoản (Profile)
    @GetMapping("/profile")
    public String profilePage(java.security.Principal principal, org.springframework.ui.Model model,
                              jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        try {
            System.out.println("=== PROFILE PAGE ACCESS ===");
            System.out.println("DEBUG: Request URI: " + request.getRequestURI());
            System.out.println("DEBUG: Principal: " + (principal != null ? principal.getName() : "null"));
            System.out.println("DEBUG: Auth Context: " + (org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()));
            System.out.println("DEBUG: Session ID: " + (request.getSession(false) != null ? request.getSession().getId() : "no session"));

            // IMPORTANT: Force check if we're in a fresh application start
            // If app was restarted, old sessions are invalid
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            boolean isFreshStart = false;

            if (session != null) {
                long creationTime = session.getCreationTime();
                long currentTime = System.currentTimeMillis();
                long sessionAge = (currentTime - creationTime) / 1000; // in seconds

                System.out.println("DEBUG: Session age: " + sessionAge + " seconds");

                // If session is older than 1 second, this might be a stale session from before restart
                // Force logout to be safe
                if (sessionAge > 60) { // More than 1 minute = likely stale session
                    System.out.println("DEBUG: Session is old (" + sessionAge + "s), forcing logout");
                    isFreshStart = true;
                }
            } else {
                System.out.println("DEBUG: No session found");
                isFreshStart = true; // No session = fresh start
            }

            if (principal == null) {
                System.out.println("DEBUG: No principal, redirecting to /login");
                return "redirect:/login";
            }

            String principalName = principal.getName();
            System.out.println("DEBUG: Looking up user with: " + principalName);

            // Try findByUsername first, then findByEmail
            com.dealxanh.app.entity.User user = userRepository.findByUsername(principalName)
                    .orElse(null);

            if (user == null) {
                System.out.println("DEBUG: Not found by username, trying email...");
                user = userRepository.findByEmail(principalName).orElse(null);
            }

            if (user == null) {
                System.out.println("=== USER NOT FOUND ===");
                System.out.println("DEBUG: Principal exists but user NOT in DB: " + principalName);
                System.out.println("DEBUG: This is a STALE/OLD session. Forcing logout...");

                // Force logout: invalidate session and clear authentication
                if (session != null) {
                    System.out.println("DEBUG: Invalidating session...");
                    session.invalidate();
                }

                // Clear security context
                org.springframework.security.core.context.SecurityContextHolder.clearContext();

                // Delete JSESSIONID cookie
                jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if (cookie.getName().equals("JSESSIONID") || cookie.getName().equals("remember-me")) {
                        System.out.println("DEBUG: Deleting cookie: " + cookie.getName());
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        cookie.setValue("");
                        response.addCookie(cookie);
                    }
                }

                System.out.println("=== FORCE LOGOUT COMPLETE ===");
                return "redirect:/login?session_expired=true";
            }

            // User found - verify user is active
            if (user.getActive() == null || !user.getActive()) {
                System.out.println("DEBUG: User is inactive, redirecting to /login");
                return "redirect:/login";
            }

            // Double-check: if session is too old, force logout even if user exists
            if (isFreshStart) {
                System.out.println("=== FRESH APP START DETECTED ===");
                System.out.println("DEBUG: App was restarted, forcing re-login for security");

                if (session != null) {
                    session.invalidate();
                }
                org.springframework.security.core.context.SecurityContextHolder.clearContext();

                // Clear cookies
                jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if (cookie.getName().equals("JSESSIONID") || cookie.getName().equals("remember-me")) {
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        cookie.setValue("");
                        response.addCookie(cookie);
                    }
                }

                return "redirect:/login?app_restarted=true";
            }

            model.addAttribute("user", user);
            System.out.println("DEBUG: User found: " + user.getUsername() + " / " + user.getEmail());
            System.out.println("DEBUG: User full name: " + user.getFullName());

            System.out.println("=== PROFILE PAGE SUCCESS ===");
            return "buyer/profile";
        } catch (Exception e) {
            System.err.println("ERROR in profile page: " + e.getMessage());
            e.printStackTrace();

            // On error, force logout and redirect
            try {
                jakarta.servlet.http.HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
            } catch (Exception ex) {
                System.err.println("ERROR during force logout: " + ex.getMessage());
            }

            return "redirect:/login?error=profile_error";
        }
    }
}
