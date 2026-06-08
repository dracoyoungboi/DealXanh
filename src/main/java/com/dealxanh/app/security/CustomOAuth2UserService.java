package com.dealxanh.app.security;

import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.RoleRepository;
import com.dealxanh.app.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String providerId = oauth2User.getAttribute("sub");

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Google không trả về email. Vui lòng cấp quyền truy cập email.");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update provider info if this is a Google login for an existing user
            if (!"google".equals(user.getProvider())) {
                user.setProvider("google");
                user.setProviderId(providerId);
                userRepository.save(user);
            }
        } else {
            user = new User();
            user.setEmail(email);
            // Generate unique username from email prefix, with fallback for collisions
            String baseUsername = email.split("@")[0];
            user.setUsername(generateUniqueUsername(baseUsername));
            user.setFullName(name);
            user.setProvider("google");
            user.setProviderId(providerId);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());

            // Try both prefixed and non-prefixed role names (DB may have ROLE_USER or USER)
            Role defaultRole = findRole("ROLE_USER", "USER");
            if (defaultRole == null) {
                // Last resort: create the role
                defaultRole = new Role();
                defaultRole.setName("ROLE_USER");
                defaultRole = roleRepository.save(defaultRole);
            }
            user.setRole(defaultRole);

            user = userRepository.save(user);
        }

        // Build authority — role name may or may not have ROLE_ prefix already
        String roleName = user.getRole().getName();
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        var authorities = Collections.singleton(new SimpleGrantedAuthority(authority));

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
