package com.dealxanh.app.security;

import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.RoleRepository;
import com.dealxanh.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update provider logic if needed
            if (!"google".equals(user.getProvider())) {
                user.setProvider("google");
                user.setProviderId(providerId);
                userRepository.save(user);
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setUsername(email != null ? email.split("@")[0] : name.replaceAll("\\s+", "").toLowerCase());
            user.setFullName(name);
            user.setProvider("google");
            user.setProviderId(providerId);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("USER");
                        return roleRepository.save(r);
                    });
            user.setRole(defaultRole);

            user = userRepository.save(user);
        }

        Collection<? extends GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())
        );

        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
    }
}
