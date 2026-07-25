package com.dealxanh.app.security;

import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.RoleRepository;
import com.dealxanh.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== OAUTH2 LOGIN START ===");
        log.info("OAuth2 client registration: {}", userRequest.getClientRegistration().getRegistrationId());

        OAuth2User oauth2User = super.loadUser(userRequest);

        // Extract all relevant Google profile attributes
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String providerId = oauth2User.getAttribute("sub");
        String picture = oauth2User.getAttribute("picture"); // Google profile photo URL

        log.info("OAuth2 Google login - email={}, name={}, providerId={}, hasPicture={}",
                email, name,
                providerId != null ? providerId.substring(0, Math.min(providerId.length(), 8)) + "..." : "null",
                picture != null && !picture.isEmpty());

        if (email == null || email.isEmpty()) {
            log.error("OAuth2 Google login FAILED: Google did not return an email address");
            throw new OAuth2AuthenticationException("Google không trả về email. Vui lòng cấp quyền truy cập email.");
        }

        // Log all available attribute keys for debugging
        log.debug("OAuth2 Google attributes keys: {}", oauth2User.getAttributes().keySet());

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            log.info("OAuth2 Google login - EXISTING USER: userId={}, username={}, email={}, role={}, provider={}, active={}",
                    user.getUserId(), user.getUsername(), user.getEmail(),
                    user.getRole() != null ? user.getRole().getName() : "null",
                    user.getProvider(), user.getActive());

            // Update provider info if this is a Google login for an existing user
            boolean needsUpdate = false;
            if (!"google".equals(user.getProvider())) {
                user.setProvider("google");
                user.setProviderId(providerId);
                needsUpdate = true;
                log.info("OAuth2 Google login - updated provider to google for userId={}", user.getUserId());
            }
            // Update avatar if user doesn't have one yet
            if ((user.getAvatarUrl() == null || user.getAvatarUrl().trim().isEmpty()) && picture != null && !picture.isEmpty()) {
                user.setAvatarUrl(picture);
                needsUpdate = true;
                log.info("OAuth2 Google login - updated avatar from Google for userId={}", user.getUserId());
            }
            // Update name if empty
            if ((user.getFullName() == null || user.getFullName().trim().isEmpty()) && name != null && !name.isEmpty()) {
                user.setFullName(name);
                needsUpdate = true;
                log.info("OAuth2 Google login - updated fullName from Google for userId={}", user.getUserId());
            }
            if (needsUpdate) {
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        } else {
            log.info("OAuth2 Google login - no existing user for email={}, creating new user", email);

            // Generate unique username from email prefix, with fallback for collisions
            String baseUsername = email.split("@")[0];
            String username = generateUniqueUsername(baseUsername);

            log.info("OAuth2 Google login - generated username={} for email={}", username, email);

            user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setFullName(name);
            user.setProvider("google");
            user.setProviderId(providerId);
            user.setActive(true);
            user.setEmailVerified(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // Set Google profile photo as avatar
            if (picture != null && !picture.isEmpty()) {
                // Use the high-resolution version: replace s96-c with s400-c
                String avatarUrl = picture;
                // Google's default picture URL: https://lh3.googleusercontent.com/a/...=s96-c
                // Request larger size for better quality
                if (avatarUrl.contains("=s96-c")) {
                    avatarUrl = avatarUrl.replace("=s96-c", "=s400-c");
                }
                user.setAvatarUrl(avatarUrl);
                log.info("OAuth2 Google login - set avatar URL for new user: {}", avatarUrl);
            }

            // Try both prefixed and non-prefixed role names (DB may have ROLE_USER or USER)
            Role defaultRole = findRole("ROLE_USER", "USER");
            if (defaultRole == null) {
                // Last resort: create the role
                log.warn("OAuth2 Google login - ROLE_USER not found in DB, creating it");
                defaultRole = new Role();
                defaultRole.setName("ROLE_USER");
                defaultRole = roleRepository.save(defaultRole);
            }
            user.setRole(defaultRole);

            user = userRepository.save(user);
            log.info("OAuth2 Google login - NEW USER CREATED: userId={}, username={}, email={}, fullName={}, role={}, active={}, emailVerified={}, provider={}",
                    user.getUserId(), user.getUsername(), user.getEmail(), user.getFullName(),
                    user.getRole().getName(), user.getActive(), user.getEmailVerified(), user.getProvider());
        }

        // Build authority — role name may or may not have ROLE_ prefix already
        String roleName = user.getRole().getName();
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        var authorities = Collections.singleton(new SimpleGrantedAuthority(authority));

        log.info("OAuth2 Google login SUCCESS - userId={}, email={}, authority={}, principalName will be email",
                user.getUserId(), user.getEmail(), authority);
        log.info("=== OAUTH2 LOGIN END (user service complete, returning to Spring Security filter chain) ===");

        // Use "email" as the name attribute key so principal.getName() returns the email
        // This is intentional: getUserFromPrincipal() in both GlobalModelAdvice and
        // controllers always tries findByUsername() first, then findByEmail() as fallback
        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
    }

    /**
     * Find a role by trying multiple name variants (DB may store with or without ROLE_ prefix).
     */
    private Role findRole(String... names) {
        for (String name : names) {
            Optional<Role> found = roleRepository.findByName(name);
            if (found.isPresent()) return found.get();
        }
        return null;
    }

    /**
     * Generate a unique username, appending a random suffix if the base name is taken.
     */
    private String generateUniqueUsername(String base) {
        if (!userRepository.findByUsername(base).isPresent()) {
            return base;
        }
        // Try with random 4-digit suffix
        Random rng = new Random();
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = base + rng.nextInt(9000) + 1000;
            if (!userRepository.findByUsername(candidate).isPresent()) {
                return candidate;
            }
        }
        // Ultimate fallback
        return base + System.currentTimeMillis() % 100000;
    }
}
