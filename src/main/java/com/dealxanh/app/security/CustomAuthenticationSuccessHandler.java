package com.dealxanh.app.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @org.springframework.beans.factory.annotation.Autowired
    private com.dealxanh.app.repository.StoreRepository storeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.dealxanh.app.repository.UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String authType = authentication.getClass().getSimpleName();
        String principalName = authentication.getName();
        boolean isOAuth2 = authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

        log.info("=== AUTH SUCCESS START ===");
        log.info("Auth type: {} (OAuth2: {}), principal: {}", authType, isOAuth2, principalName);

        if (isOAuth2) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
            log.info("OAuth2 details - clientRegistrationId={}, authorizedClientRegistrationId={}",
                    oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getAuthorizedClientRegistrationId());
            // Log available attributes (key names only, not values for privacy)
            if (oauthToken.getPrincipal() instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User) {
                var attrKeys = ((org.springframework.security.oauth2.core.user.DefaultOAuth2User) oauthToken.getPrincipal()).getAttributes().keySet();
                log.info("OAuth2 attribute keys: {}", attrKeys);
            }
        }

        // Spring Security's AbstractAuthenticationProcessingFilter.successfulAuthentication()
        // already created a SecurityContext, set the authentication, and saved it to the
        // session via SecurityContextRepository.saveContext() BEFORE calling this handler.
        // We explicitly re-save the SAME context (not a new one) to ensure it's properly
        // committed to the JDBC-backed session before sendRedirect().
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            context = SecurityContextHolder.createEmptyContext();
            SecurityContextHolder.setContext(context);
            log.warn("SecurityContext was null, created new empty context");
        }
        // Ensure authentication is set on the existing context
        if (context.getAuthentication() == null) {
            context.setAuthentication(authentication);
            log.info("SecurityContext had no authentication, set it now");
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            context);
        log.info("SecurityContext persisted to session: id={}, isNew={}, authInContext={}",
                session.getId(), session.isNew(), context.getAuthentication() != null);

        // Check for saved redirect from session expiry first
        String redirectUrl = request.getParameter("redirect");
        boolean hasSavedRedirect = (redirectUrl != null && !redirectUrl.isEmpty());
        if (!hasSavedRedirect) {
            redirectUrl = "/home"; // Default for buyers
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        log.info("DEBUG: Authorities: {}", authorities);
        log.info("DEBUG: Saved redirect URL: {}", hasSavedRedirect ? redirectUrl : "none");
        log.info("DEBUG: Session ID: {}", session.getId());

        // Check which login page was used
        String requestURI = request.getRequestURI();
        String referer = request.getHeader("referer");
        log.info("DEBUG: Request URI: {}, Referer: {}", requestURI, referer);

        // If we have a saved redirect from session expiry, use it (skip role-based default)
        if (hasSavedRedirect) {
            log.info("DEBUG: Using saved redirect: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            log.info("=== AUTH SUCCESS END (saved redirect) ===");
            return;
        }

        boolean roleFound = false;
        boolean isAdmin = false;
        boolean isSeller = false;
        boolean isBuyer = false;

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            log.info("DEBUG: Checking role: {}", role);

            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_MODERATOR")) {
                isAdmin = true;
                redirectUrl = "/admin/dashboard";
                roleFound = true;
                log.info("DEBUG: Admin/Moderator role, redirecting to: {}", redirectUrl);
                break;
            } else if (role.equals("ROLE_STORE_OWNER")) {
                isSeller = true;
                // Check store status
                String username = authentication.getName();
                com.dealxanh.app.entity.User user = userRepository.findByUsername(username)
                        .orElse(userRepository.findByEmail(username).orElse(null));

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
                            log.info("DEBUG: Store status: {}, redirecting to: {}", status, redirectUrl);
                        } else {
                            redirectUrl = "/seller/dashboard";
                            log.info("DEBUG: Store approved, redirecting to: {}", redirectUrl);
                        }
                    } else {
                        redirectUrl = "/seller/register";
                        log.info("DEBUG: No store found, redirecting to: {}", redirectUrl);
                    }
                } else {
                    redirectUrl = "/seller/register";
                    log.info("DEBUG: User not found, redirecting to: {}", redirectUrl);
                }
                roleFound = true;
                break;
            } else if (role.equals("ROLE_STORE_STAFF")) {
                isSeller = true;
                redirectUrl = "/staff/dashboard";
                roleFound = true;
                log.info("DEBUG: Staff role, redirecting to: {}", redirectUrl);
                break;
            } else if (role.equals("ROLE_USER") || role.equals("ROLE_CUSTOMER")) {
                isBuyer = true;
                redirectUrl = "/home";
                roleFound = true;
                log.info("DEBUG: Buyer role, redirecting to: {}", redirectUrl);
                break;
            }
        }

        // Check if user logged in from WRONG login page - redirect to correct login page
        if ((isAdmin && referer != null && referer.contains("/login") && !referer.contains("/admin/login")) ||
            (isSeller && referer != null && referer.contains("/login") && !referer.contains("/seller/login") && !referer.contains("/staff/login"))) {
            log.info("DEBUG: Admin/Seller/Staff logged in from BUYER login page - redirecting to correct login page");

            // Logout this session
            SecurityContextHolder.clearContext();
            if (session != null) {
                session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            }

            // Redirect to correct login page with hint about which role tried to login
            String correctLoginPage = isAdmin ? "/admin/login" : "/seller/login";
            response.sendRedirect(correctLoginPage + "?error=please_use_correct_login&from=buyer");
            return;
        }

        // Buyer logged in from admin/seller login page - redirect to buyer login
        if (isBuyer && (referer != null && referer.contains("/admin/login") || referer.contains("/seller/login"))) {
            log.info("DEBUG: Buyer logged in from ADMIN/SELLER login page - redirecting to buyer login");

            // Logout this session
            SecurityContextHolder.clearContext();
            if (session != null) {
                session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            }

            // Determine which wrong login page was used
            String fromParam = referer != null && referer.contains("/admin/login") ? "admin" : "seller";

            // Redirect to correct login page with hint
            String correctLoginPage = "/login"; // Buyer login page
            response.sendRedirect(correctLoginPage + "?error=please_use_correct_login&from=" + fromParam);
            return;
        }

        if (!roleFound) {
            log.info("DEBUG: No matching role found, defaulting to /home");
            redirectUrl = "/home";
        }

        log.info("Final redirect URL: {} (isOAuth2={}, hasSavedRedirect={}, roleFound={})",
                redirectUrl, isOAuth2, hasSavedRedirect, roleFound);
        log.info("=== AUTH SUCCESS END -> redirect to {} ===", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}
