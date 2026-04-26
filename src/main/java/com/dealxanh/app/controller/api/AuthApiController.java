package com.dealxanh.app.controller.api;

import com.dealxanh.app.service.EmailService;
import com.dealxanh.app.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import com.dealxanh.app.dto.request.RegisterBuyerRequest;
import com.dealxanh.app.dto.request.RegisterSellerRequest;
import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.repository.RoleRepository;
import com.dealxanh.app.repository.UserRepository;
import com.dealxanh.app.repository.StoreRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userRepository.findByEmail(email).isPresent()) {
                response.put("success", false);
                response.put("message", "Email đã được sử dụng. Vui lòng chọn email khác.");
                return ResponseEntity.badRequest().body(response);
            }

            // Generate OTP
            String otp = otpService.generateAndStoreOtp(email);
            
            // Send Email
            emailService.sendOtpEmail(email, otp);
            
            response.put("success", true);
            response.put("message", "Đã gửi OTP đến email: " + email);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể gửi email: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        Map<String, Object> response = new HashMap<>();
        
        boolean isValid = otpService.verifyOtp(email, otp);
        if (isValid) {
            response.put("success", true);
            response.put("message", "Xác thực OTP thành công");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Mã OTP không chính xác hoặc đã hết hạn");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register-buyer")
    public ResponseEntity<?> registerBuyer(@RequestBody RegisterBuyerRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // 1. Verify OTP
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            response.put("success", false);
            response.put("message", "Mã OTP không chính xác hoặc đã hết hạn");
            return ResponseEntity.badRequest().body(response);
        }

        // 2. Check if email exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            response.put("success", false);
            response.put("message", "Email đã được sử dụng. Vui lòng chọn email khác.");
            return ResponseEntity.badRequest().body(response);
        }

        // 3. Create User
        try {
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            // Lấy email prefix làm username tạm thời
            newUser.setUsername(request.getEmail().split("@")[0]); 
            newUser.setFullName(request.getFullName());
            newUser.setPhone(request.getPhone());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setProvider("local");
            newUser.setActive(true);
            newUser.setCreatedAt(LocalDateTime.now());
            
            // Assign Role USER
            Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("USER");
                    return roleRepository.save(r);
                });
            newUser.setRole(userRole);

            userRepository.save(newUser);
            
            response.put("success", true);
            response.put("message", "Đăng ký tài khoản thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi tạo tài khoản: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Autowired
    private StoreRepository storeRepository;

    @PostMapping("/register-seller")
    public ResponseEntity<?> registerSeller(@org.springframework.web.bind.annotation.ModelAttribute RegisterSellerRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Setup Owner (User)
            java.util.Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
            User newOwner;
            boolean isNewUser = false;
            if (existingUserOpt.isPresent()) {
                newOwner = existingUserOpt.get();
                // Chỉ update các thông tin cơ bản
                newOwner.setFullName(request.getFirstName() + " " + request.getLastName());
                newOwner.setPhone(request.getPhone());
                newOwner.setAddress(request.getAddress() + ", " + request.getDistrict() + ", " + request.getCity());
                if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                    newOwner.setPassword(passwordEncoder.encode(request.getPassword()));
                }
            } else {
                newOwner = new User();
                newOwner.setEmail(request.getEmail());
                newOwner.setUsername(request.getEmail().split("@")[0]); 
                newOwner.setFullName(request.getFirstName() + " " + request.getLastName());
                newOwner.setPhone(request.getPhone());
                newOwner.setPassword(passwordEncoder.encode(request.getPassword()));
                newOwner.setAddress(request.getAddress() + ", " + request.getDistrict() + ", " + request.getCity());
                newOwner.setProvider("local");
                newOwner.setActive(true);
                newOwner.setCreatedAt(LocalDateTime.now());
                
                Role ownerRole = roleRepository.findByName("STORE_OWNER")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("STORE_OWNER");
                        return roleRepository.save(r);
                    });
                newOwner.setRole(ownerRole);
                isNewUser = true;
            }

            userRepository.save(newOwner);

            // 2. Setup Store
            java.util.Optional<Store> existingStoreOpt = storeRepository.findByOwner(newOwner);
            Store newStore = existingStoreOpt.orElseGet(Store::new);

            newStore.setStoreName(request.getStoreName());
            newStore.setBusinessType(request.getBusinessType());
            newStore.setCategories(request.getCategories());
            newStore.setAddress(request.getAddress());
            newStore.setCity(request.getCity());
            newStore.setDistrict(request.getDistrict());
            newStore.setDescription(request.getDescription());
            newStore.setPhone(request.getPhone());
            
            newStore.setBankName(request.getBankName());
            newStore.setBankAccountNumber(request.getBankAccountNumber());
            newStore.setBankAccountOwner(request.getBankAccountOwner());
            
            newStore.setOperatingDays(request.getOperatingDays());
            newStore.setPickupSlots(request.getPickupSlots());
            newStore.setMaxSlotsPerTime(request.getMaxSlotsPerTime());
            newStore.setPickupDurationMinutes(request.getPickupDurationMinutes());
            newStore.setApprovalMode(request.getApprovalMode());
            
            // Handle Files (Chỉ cập nhật nếu có file upload lên)
            String cccdUrl = saveFile(request.getCccdFile());
            if (cccdUrl != null) newStore.setCccdUrl(cccdUrl);
            
            String licenseUrl = saveFile(request.getLicenseFile());
            if (licenseUrl != null) newStore.setBusinessLicenseUrl(licenseUrl);
            
            String vsattpUrl = saveFile(request.getVsattpFile());
            if (vsattpUrl != null) newStore.setVsattpUrl(vsattpUrl);
            
            if (!existingStoreOpt.isPresent()) {
                newStore.setCreatedAt(LocalDateTime.now());
                newStore.setOwner(newOwner);
            }
            // Always set back to PENDING when resubmitting
            newStore.setStatus("PENDING");

            storeRepository.save(newStore);
            
            response.put("success", true);
            response.put("message", isNewUser ? "Gửi hồ sơ xét duyệt thành công!" : "Cập nhật hồ sơ thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi khi lưu hồ sơ: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private String saveFile(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) return null;
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath);
        }
        java.nio.file.Files.copy(file.getInputStream(), uploadPath.resolve(fileName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + fileName;
    }

    /**
     * GET /api/auth/seller-status?email={email}
     * Trả về trạng thái hồ sơ seller từ DB (dùng cho onboarding-pending realtime)
     */
    @org.springframework.web.bind.annotation.GetMapping("/seller-status")
    public ResponseEntity<?> getSellerStatus(@RequestParam String email) {
        Map<String, Object> resp = new HashMap<>();
        try {
            java.util.Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                resp.put("success", false);
                resp.put("message", "Không tìm thấy tài khoản");
                return ResponseEntity.status(404).body(resp);
            }
            User user = userOpt.get();
            java.util.Optional<Store> storeOpt = storeRepository.findByOwner(user);
            if (!storeOpt.isPresent()) {
                resp.put("success", false);
                resp.put("message", "Chưa có hồ sơ cửa hàng");
                return ResponseEntity.status(404).body(resp);
            }
            Store store = storeOpt.get();
            resp.put("success", true);
            resp.put("status", store.getStatus());
            resp.put("storeName", store.getStoreName());
            resp.put("submittedAt", store.getCreatedAt() != null ? store.getCreatedAt().toString() : null);
            resp.put("hasCccd", store.getCccdUrl() != null && !store.getCccdUrl().isEmpty());
            resp.put("hasLicense", store.getBusinessLicenseUrl() != null && !store.getBusinessLicenseUrl().isEmpty());
            resp.put("hasVsattp", store.getVsattpUrl() != null && !store.getVsattpUrl().isEmpty());
            resp.put("hasBank", store.getBankAccountNumber() != null && !store.getBankAccountNumber().isEmpty());
            resp.put("pickupSlots", store.getPickupSlots());
            resp.put("ownerName", user.getFullName());
            resp.put("ownerEmail", user.getEmail());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
}
