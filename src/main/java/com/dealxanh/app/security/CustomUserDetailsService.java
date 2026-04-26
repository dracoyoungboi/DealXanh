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
        // Tìm theo username trước
        Optional<User> userOpt = userRepository.findByUsernameWithRole(usernameOrEmail);

        // Nếu không tìm được, thử tìm theo email
        if (userOpt.isEmpty()) {
            List<User> users = userRepository.findAllByEmailWithRole(usernameOrEmail);
            if (!users.isEmpty()) {
                userOpt = Optional.of(users.get(0));
            }
        }

        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("Không tìm thấy user: " + usernameOrEmail);
        }

        User user = userOpt.get();

        // Kiểm tra tài khoản có bị khóa không
        if (user.getActive() != null && !user.getActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa: " + usernameOrEmail);
        }

        // Force load role
        if (user.getRole() != null) {
            user.getRole().getName();
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(getAuthorities(user))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null && user.getRole().getName() != null) {
            String roleName = user.getRole().getName().trim().toUpperCase();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            authorities.add(new SimpleGrantedAuthority(roleName));
        }
        return authorities;
    }
}
