package com.dealxanh.app.controller.api;

import com.dealxanh.app.service.EmailService;
import com.dealxanh.app.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public ResponseEntity<?> registerBuyer(@org.springframework.web.bind.annotation.ModelAttribute RegisterBuyerRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // 1. Validate all required fields
        String errorMsg = null;
        
        // Validate full name
        if ((errorMsg = getValidationErrorMessage("fullName", request.getFullName())) != null) {
            response.put("success", false);
            response.put("message", errorMsg);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate email
        if ((errorMsg = getValidationErrorMessage("email", request.getEmail())) != null) {
            response.put("success", false);
            response.put("message", errorMsg);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate phone
        if ((errorMsg = getValidationErrorMessage("phone", request.getPhone())) != null) {
            response.put("success", false);
            response.put("message", errorMsg);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate password
        if ((errorMsg = getValidationErrorMessage("password", request.getPassword())) != null) {
            response.put("success", false);
            response.put("message", errorMsg);
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validate avatar file if provided
        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            // Check file size (5MB max)
            if (!isValidFileSize(request.getAvatarFile(), 5 * 1024 * 1024)) {
                response.put("success", false);
                response.put("message", "Kích thước ảnh avatar không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if it's an image
            if (!isImageFile(request.getAvatarFile())) {
                response.put("success", false);
                response.put("message", "Avatar phải là file ảnh (JPG, PNG, GIF)");
                return ResponseEntity.badRequest().body(response);
            }
        }
        
        // 2. Verify OTP
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            response.put("success", false);
            response.put("message", "Mã OTP không chính xác hoặc đã hết hạn");
            return ResponseEntity.badRequest().body(response);
        }

        // 3. Check if email exists
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
            
            // Handle avatar upload
            try {
                String avatarUrl = saveFile(request.getAvatarFile());
                if (avatarUrl != null) {
                    newUser.setAvatarUrl(avatarUrl);
                }
            } catch (Exception e) {
                // Log error but don't fail registration
                System.err.println("Error saving avatar: " + e.getMessage());
            }
            
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
            // 1. Validate all required fields
            String errorMsg = null;
            
            // Validate first name
            if ((errorMsg = getValidationErrorMessage("firstName", request.getFirstName())) != null) {
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate last name
            if ((errorMsg = getValidationErrorMessage("lastName", request.getLastName())) != null) {
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate email
            if ((errorMsg = getValidationErrorMessage("email", request.getEmail())) != null) {
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate phone
            if ((errorMsg = getValidationErrorMessage("phone", request.getPhone())) != null) {
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate password (only for new users)
            if ((errorMsg = getValidationErrorMessage("password", request.getPassword())) != null) {
                java.util.Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
                if (!existingUserOpt.isPresent()) {
                    response.put("success", false);
                    response.put("message", errorMsg);
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Validate store name
            if ((errorMsg = getValidationErrorMessage("storeName", request.getStoreName())) != null) {
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate address
            if ((errorMsg = getValidationErrorMessage("address", request.getAddress())) != null) {
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate city
            if ((errorMsg = getValidationErrorMessage("city", request.getCity())) != null) {
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate district
            if ((errorMsg = getValidationErrorMessage("district", request.getDistrict())) != null) {
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate required document files
            org.springframework.web.multipart.MultipartFile[] cccdFiles = request.getCccdFiles();
            if (cccdFiles == null || cccdFiles.length == 0) {
                response.put("success", false);
                response.put("message", "Vui lòng tải lên ít nhất 1 file CCCD/CMND (mặt trước)");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (cccdFiles.length > 2) {
                response.put("success", false);
                response.put("message", "Chỉ được tải lên tối đa 2 file CCCD/CMND (mặt trước & mặt sau)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate each CCCD file
            for (int i = 0; i < cccdFiles.length; i++) {
                org.springframework.web.multipart.MultipartFile file = cccdFiles[i];
                if (!isValidFileSize(file, 5 * 1024 * 1024)) {
                    response.put("success", false);
                    response.put("message", "File CCCD thứ " + (i + 1) + " vượt quá kích thước 5MB");
                    return ResponseEntity.badRequest().body(response);
                }
                
                if (!isImageFile(file) && !file.getContentType().equals("application/pdf")) {
                    response.put("success", false);
                    response.put("message", "File CCCD thứ " + (i + 1) + " phải là ảnh (JPG, PNG) hoặc PDF");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            if (request.getLicenseFile() == null || request.getLicenseFile().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng tải lên Giấy phép kinh doanh");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate license file
            if (!isValidFileSize(request.getLicenseFile(), 5 * 1024 * 1024)) {
                response.put("success", false);
                response.put("message", "Kích thước file Giấy phép kinh doanh không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!isImageFile(request.getLicenseFile()) && !request.getLicenseFile().getContentType().equals("application/pdf")) {
                response.put("success", false);
                response.put("message", "Giấy phép kinh doanh phải là ảnh (JPG, PNG) hoặc PDF");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate optional VSATTP file
            if (request.getVsattpFile() != null && !request.getVsattpFile().isEmpty()) {
                if (!isValidFileSize(request.getVsattpFile(), 5 * 1024 * 1024)) {
                    response.put("success", false);
                    response.put("message", "Kích thước file VSATTP không được vượt quá 5MB");
                    return ResponseEntity.badRequest().body(response);
                }
                
                if (!isImageFile(request.getVsattpFile()) && !request.getVsattpFile().getContentType().equals("application/pdf")) {
                    response.put("success", false);
                    response.put("message", "VSATTP phải là ảnh (JPG, PNG) hoặc PDF");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // 3. Setup Owner (User)
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

            // 3. Setup Store
            java.util.Optional<Store> existingStoreOpt = storeRepository.findByOwner(newOwner);
            Store newStore = existingStoreOpt.orElseGet(Store::new);

            // 4. Check for duplicates
            // Check store name duplicate
            java.util.Optional<Store> existingStoreByName = storeRepository.findByStoreName(request.getStoreName());
            if (existingStoreByName.isPresent()) {
                // Allow same store name for same store (when updating)
                Store existingStore = existingStoreByName.get();
                if (existingStoreOpt.isEmpty() || existingStoreOpt.get().getStoreId() != existingStore.getStoreId()) {
                    response.put("success", false);
                    response.put("message", "Tên cửa hàng đã tồn tại. Vui lòng chọn tên khác.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Check phone duplicate (excluding current user)
            java.util.List<User> usersWithSamePhone = userRepository.findByPhone(request.getPhone());
            if (!usersWithSamePhone.isEmpty()) {
                boolean isCurrentUser = false;
                for (User user : usersWithSamePhone) {
                    if (user.getEmail().equals(request.getEmail())) {
                        isCurrentUser = true;
                        break;
                    }
                }
                if (!isCurrentUser) {
                    response.put("success", false);
                    response.put("message", "Số điện thoại đã được sử dụng. Vui lòng chọn số điện thoại khác.");
                    return ResponseEntity.badRequest().body(response);
                }
            }

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
            if (cccdFiles != null && cccdFiles.length > 0) {
                // Merge existing URLs with new files
                String existingCccdUrl = existingStoreOpt.isPresent() ? 
                    existingStoreOpt.get().getCccdUrl() : null;
                
                List<String> urlList = new ArrayList<>();
                
                // Parse existing URLs
                if (existingCccdUrl != null && !existingCccdUrl.trim().isEmpty()) {
                    String[] existingUrls = existingCccdUrl.split(";");
                    for (String url : existingUrls) {
                        if (url != null && !url.trim().isEmpty() && !url.startsWith("new_file_")) {
                            urlList.add(url.trim());
                        }
                    }
                }
                
                // Save new files and add to list
                for (int i = 0; i < cccdFiles.length; i++) {
                    String fileUrl = saveFile(cccdFiles[i]);
                    if (fileUrl != null) {
                        urlList.add(fileUrl);
                    }
                }
                
                // Create combined URL string (max 2 files)
                StringBuilder cccdUrls = new StringBuilder();
                for (int i = 0; i < Math.min(urlList.size(), 2); i++) {
                    if (cccdUrls.length() > 0) {
                        cccdUrls.append(";");
                    }
                    cccdUrls.append(urlList.get(i));
                }
                
                if (cccdUrls.length() > 0) {
                    String cccdUrlString = cccdUrls.toString();
                    // Truncate if too long (temporary fix until DB schema is updated)
                    if (cccdUrlString.length() > 255) {
                        System.out.println("WARNING: CCCD URL too long (" + cccdUrlString.length() + " chars), truncating to 255");
                        cccdUrlString = cccdUrlString.substring(0, 252) + "...";
                    }
                    newStore.setCccdUrl(cccdUrlString);
                }
            }
            
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
            response.put("redirectUrl", "/auth/onboarding-pending");
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

    // ========== VALIDATION HELPER METHODS ==========
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        // Remove spaces and check Vietnam phone format
        String cleanPhone = phone.replaceAll("\\s", "");
        String phoneRegex = "^(0[3|5|7|8|9])[0-9]{8}$";
        return cleanPhone.matches(phoneRegex);
    }
    
    private boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) return false;
        // Strong password: min 8 chars, uppercase, lowercase, digit, special char
        String pwdRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(pwdRegex);
    }
    
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        // Name should be at least 2 characters and contain only letters and spaces
        return name.trim().length() >= 2 && name.matches("^[\\p{L}\\s]+$");
    }
    
    private boolean isValidFileSize(org.springframework.web.multipart.MultipartFile file, long maxSizeInBytes) {
        return file != null && !file.isEmpty() && file.getSize() <= maxSizeInBytes;
    }
    
    private boolean isImageFile(org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    private String getValidationErrorMessage(String field, String value) {
        switch (field) {
            case "email":
                if (!isValidEmail(value)) return "Email không hợp lệ";
                break;
            case "phone":
                if (!isValidPhone(value)) return "Số điện thoại không hợp lệ (10 số, bắt đầu 03/05/07/08/09)";
                break;
            case "password":
                if (!isValidPassword(value)) return "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt";
                break;
            case "fullName":
            case "firstName":
            case "lastName":
                if (!isValidName(value)) return "Tên không hợp lệ (ít nhất 2 ký tự, chỉ chứa chữ cái)";
                break;
            case "storeName":
                if (value == null || value.trim().isEmpty()) return "Vui lòng nhập tên cửa hàng";
                if (value.trim().length() < 2) return "Tên cửa hàng phải có ít nhất 2 ký tự";
                break;
            case "address":
                if (value == null || value.trim().isEmpty()) return "Vui lòng nhập địa chỉ";
                break;
            case "city":
                if (value == null || value.trim().isEmpty()) return "Vui lòng chọn tỉnh/thành phố";
                break;
            case "district":
                if (value == null || value.trim().isEmpty()) return "Vui lòng chọn quận/huyện";
                break;
            default:
                return "Dữ liệu không hợp lệ";
        }
        return null;
    }
}
