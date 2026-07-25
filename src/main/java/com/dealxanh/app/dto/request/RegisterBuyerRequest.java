package com.dealxanh.app.dto.request;

import org.springframework.web.multipart.MultipartFile;

public class RegisterBuyerRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String otp;
    private MultipartFile avatarFile;

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public MultipartFile getAvatarFile() { return avatarFile; }
    public void setAvatarFile(MultipartFile avatarFile) { this.avatarFile = avatarFile; }
}
