package com.dealxanh.app.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
                                            
        String redirectUrl = "/home";
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            
            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_MODERATOR")) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (role.equals("ROLE_STORE_OWNER")) {
                // Check store status
                String username = authentication.getName();
                com.dealxanh.app.entity.User user = userRepository.findByUsername(username)
                        .orElse(userRepository.findByEmail(username).orElse(null));
                if (user != null) {
                    java.util.Optional<com.dealxanh.app.entity.Store> storeOpt = storeRepository.findByOwner(user);
                    if (storeOpt.isPresent()) {
                        String status = storeOpt.get().getStatus();
                        if ("PENDING".equals(status) || "REJECTED".equals(status)) {
                            redirectUrl = "/seller/onboarding-pending";
                            break;
                        }
                    }
                }
                redirectUrl = "/seller/dashboard";
                break;
            } else if (role.equals("ROLE_STORE_STAFF")) {
                redirectUrl = "/seller/dashboard";
                break;
            } else if (role.equals("ROLE_USER") || role.equals("ROLE_CUSTOMER")) {
                redirectUrl = "/home";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
