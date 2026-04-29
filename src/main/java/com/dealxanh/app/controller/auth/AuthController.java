package com.dealxanh.app.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // Trang đăng nhập của User (Buyer)
    @GetMapping("/login")
    public String buyerLogin() {
        return "buyer/login";
    }

    // Trang đăng nhập của Admin / Moderator
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    // Trang đăng nhập của Store Owner / Staff
    @GetMapping("/seller/login")
    public String sellerLogin() {
        return "seller/login";
    }

    // Trang chọn vai trò Đăng ký (Người mua / Cửa hàng)
    @GetMapping("/register")
    public String roleSelect() {
        return "auth/role-select";
    }

    // Trang đăng ký Người mua (User)
    @GetMapping("/register/buyer")
    public String registerBuyer() {
        return "auth/register-buyer";
    }

    // Trang đăng ký Chủ Cửa hàng (Owner)
    @GetMapping("/seller/register")
    public String registerSeller(java.security.Principal principal, org.springframework.ui.Model model) {
        if (principal != null) {
            String username = principal.getName();
            com.dealxanh.app.entity.User user = userRepository.findByUsername(username)
                    .orElse(userRepository.findByEmail(username).orElse(null));
            if (user != null) {
                model.addAttribute("userObj", user);
                java.util.Optional<com.dealxanh.app.entity.Store> storeOpt = storeRepository.findByOwner(user);
                storeOpt.ifPresent(store -> {
                    model.addAttribute("storeObj", store);
                    // Add document URLs for file pre-loading
                    model.addAttribute("cccdUrl", store.getCccdUrl());
                    model.addAttribute("licenseUrl", store.getBusinessLicenseUrl());
                    model.addAttribute("vsattpUrl", store.getVsattpUrl());
                    model.addAttribute("logoUrl", store.getLogoUrl());
                });
            }
        }
        return "auth/register-seller";
    }

    // Trang Quên mật khẩu chung (User)
    @GetMapping("/terms")
    public String termsPage() {
        return "auth/terms";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordUser() {
        return "auth/forgot-password";
    }

    // Trang Quên mật khẩu của Chủ Cửa hàng / Nhân viên
    @GetMapping("/seller/forgot-password")
    public String forgotPasswordSeller() {
        return "auth/forgot-password";
    }

    // Trang Quên mật khẩu của Admin / Moderator
    @GetMapping("/admin/forgot-password")
    public String forgotPasswordAdmin() {
        return "auth/forgot-password";
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.dealxanh.app.repository.UserRepository userRepository;
    
    @org.springframework.beans.factory.annotation.Autowired
    private com.dealxanh.app.repository.StoreRepository storeRepository;

    @GetMapping("/seller/onboarding-pending")
    public String onboardingPending(java.security.Principal principal, org.springframework.ui.Model model) {
        if (principal == null) {
            return "redirect:/seller/login";
        }
        
        String username = principal.getName();
        com.dealxanh.app.entity.User user = userRepository.findByUsername(username)
                .orElse(userRepository.findByEmail(username).orElse(null));
                
        if (user != null) {
            java.util.Optional<com.dealxanh.app.entity.Store> storeOpt = storeRepository.findByOwner(user);
            if (storeOpt.isPresent()) {
                com.dealxanh.app.entity.Store store = storeOpt.get();
                model.addAttribute("storeStatus", store.getStatus());
                model.addAttribute("storeName", store.getStoreName());
                model.addAttribute("submittedAt", store.getCreatedAt());
                model.addAttribute("hasCccd", store.getCccdUrl() != null && !store.getCccdUrl().isEmpty());
                model.addAttribute("hasLicense", store.getBusinessLicenseUrl() != null && !store.getBusinessLicenseUrl().isEmpty());
                model.addAttribute("hasVsattp", store.getVsattpUrl() != null && !store.getVsattpUrl().isEmpty());
                model.addAttribute("hasBank", store.getBankAccountNumber() != null && !store.getBankAccountNumber().isEmpty());
                model.addAttribute("pickupSlots", store.getPickupSlots());
            }
            model.addAttribute("ownerEmail", user.getEmail());
            model.addAttribute("ownerName", user.getFullName());
        }
        return "auth/onboarding-pending";
    }

    @GetMapping("/seller/logout")
    public String logoutSeller(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/seller/login";
    }
}
