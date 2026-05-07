package com.dealxanh.app.controller.util;

import com.dealxanh.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/util")
public class PasswordUpdateController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/update-admin-password")
    public Map<String, Object> updateAdminPassword(@RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find admin user
            userRepository.findByEmail("admin@dealxanh.com").ifPresent(user -> {
                String encodedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encodedPassword);
                userRepository.save(user);

                response.put("success", true);
                response.put("message", "Password updated successfully for admin user");
                response.put("newPassword", newPassword);
                response.put("encodedPassword", encodedPassword);
            });

            if (!response.containsKey("success")) {
                response.put("success", false);
                response.put("message", "Admin user not found");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }
}
