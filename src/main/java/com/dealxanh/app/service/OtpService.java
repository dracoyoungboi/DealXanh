package com.dealxanh.app.service;

import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // Store OTP in memory: Email -> OTP string
    // In production, should use Redis for distributed caching.
    private final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();
    
    public String generateAndStoreOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        otpStore.put(email, otp);
        
        // Remove after 5 mins (Optional logic, simple clear or scheduled task can be used)
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000); // 5 minutes
                otpStore.remove(email, otp); // only remove if it's the same OTP
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return otp;
    }

    public boolean verifyOtp(String email, String inputOtp) {
        String storedOtp = otpStore.get(email);
        if (storedOtp != null && storedOtp.equals(inputOtp)) {
            otpStore.remove(email); // Clear OTP after success
            return true;
        }
        return false;
    }
}
