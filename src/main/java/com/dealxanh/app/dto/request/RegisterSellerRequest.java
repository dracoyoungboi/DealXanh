package com.dealxanh.app.dto.request;

public class RegisterSellerRequest {
    // User info
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;

    // Store info
    private String storeName;
    private String businessType;
    private String categories;
    private String address;
    private String city;
    private String district;
    private String description;

    // Bank details
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountOwner;

    // Pickup config
    private String operatingDays;
    private String pickupSlots;
    private Integer maxSlotsPerTime;
    private Integer pickupDurationMinutes;
    private String approvalMode;

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getCategories() { return categories; }
    public void setCategories(String categories) { this.categories = categories; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public String getBankAccountOwner() { return bankAccountOwner; }
    public void setBankAccountOwner(String bankAccountOwner) { this.bankAccountOwner = bankAccountOwner; }

    public String getOperatingDays() { return operatingDays; }
    public void setOperatingDays(String operatingDays) { this.operatingDays = operatingDays; }

    public String getPickupSlots() { return pickupSlots; }
    public void setPickupSlots(String pickupSlots) { this.pickupSlots = pickupSlots; }

    public Integer getMaxSlotsPerTime() { return maxSlotsPerTime; }
    public void setMaxSlotsPerTime(Integer maxSlotsPerTime) { this.maxSlotsPerTime = maxSlotsPerTime; }

    public Integer getPickupDurationMinutes() { return pickupDurationMinutes; }
    public void setPickupDurationMinutes(Integer pickupDurationMinutes) { this.pickupDurationMinutes = pickupDurationMinutes; }

    public String getApprovalMode() { return approvalMode; }
    public void setApprovalMode(String approvalMode) { this.approvalMode = approvalMode; }

    private org.springframework.web.multipart.MultipartFile cccdFile;
    private org.springframework.web.multipart.MultipartFile licenseFile;
    private org.springframework.web.multipart.MultipartFile vsattpFile;
    private org.springframework.web.multipart.MultipartFile logoFile;

    public org.springframework.web.multipart.MultipartFile getCccdFile() { return cccdFile; }
    public void setCccdFile(org.springframework.web.multipart.MultipartFile cccdFile) { this.cccdFile = cccdFile; }

    public org.springframework.web.multipart.MultipartFile getLicenseFile() { return licenseFile; }
    public void setLicenseFile(org.springframework.web.multipart.MultipartFile licenseFile) { this.licenseFile = licenseFile; }

    public org.springframework.web.multipart.MultipartFile getVsattpFile() { return vsattpFile; }
    public void setVsattpFile(org.springframework.web.multipart.MultipartFile vsattpFile) { this.vsattpFile = vsattpFile; }

    public org.springframework.web.multipart.MultipartFile getLogoFile() { return logoFile; }
    public void setLogoFile(org.springframework.web.multipart.MultipartFile logoFile) { this.logoFile = logoFile; }
}
