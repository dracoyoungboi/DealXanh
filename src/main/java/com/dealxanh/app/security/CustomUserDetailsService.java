package com.dealxanh.app.security;

import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("DEBUG: Loading user: " + usernameOrEmail);

        // Tìm theo username trước
        Optional<User> userOpt = userRepository.findByUsernameWithRole(usernameOrEmail);

        // Nếu không tìm được, thử tìm theo email
        if (userOpt.isEmpty()) {
            System.out.println("DEBUG: Not found by username, trying email...");
            List<User> users = userRepository.findAllByEmailWithRole(usernameOrEmail);
            if (!users.isEmpty()) {
                userOpt = Optional.of(users.get(0));
                System.out.println("DEBUG: Found by email: " + users.get(0).getEmail());
            }
        }

        if (userOpt.isEmpty()) {
            System.out.println("DEBUG: User not found: " + usernameOrEmail);
            throw new UsernameNotFoundException("Không tìm thấy tài khoản với thông tin: " + usernameOrEmail);
        }

        User user = userOpt.get();
        System.out.println("DEBUG: User found: " + user.getUsername() + ", active: " + user.getActive());

        // Kiểm tra tài khoản có bị khóa không
        if (user.getActive() != null && !user.getActive()) {
            System.out.println("DEBUG: Account is disabled: " + usernameOrEmail);
            throw new UsernameNotFoundException("Tài khoản đã bị khóa: " + usernameOrEmail);
        }

        // Debug role info
        if (user.getRole() != null) {
            System.out.println("DEBUG: User role object: " + user.getRole());
            System.out.println("DEBUG: User role getName(): " + user.getRole().getName());
            System.out.println("DEBUG: User role toString(): " + user.getRole().toString());
        } else {
            System.out.println("DEBUG: User has no role, will use ROLE_USER");
        }

        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        System.out.println("DEBUG: Authorities: " + authorities);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getRole() != null && user.getRole().getName() != null) {
            String roleName = user.getRole().getName().trim();
            System.out.println("DEBUG: Raw role name: '" + roleName + "'");

            // Don't add ROLE_ prefix if it already has one
            String finalRoleName = roleName;
            if (!roleName.startsWith("ROLE_")) {
                finalRoleName = "ROLE_" + roleName.toUpperCase();
            }

            System.out.println("DEBUG: Final role name: " + finalRoleName);
            authorities.add(new SimpleGrantedAuthority(finalRoleName));
        } else {
            // Default role if no role assigned
            System.out.println("DEBUG: No role found, using default ROLE_USER");
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }
}
