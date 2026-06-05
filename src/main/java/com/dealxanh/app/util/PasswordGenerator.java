package com.dealxanh.app.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Generate hash for "admin"
        String password = "123456789";
        String hash = encoder.encode(password);

        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);

        // Test the hash
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);

    }
}
