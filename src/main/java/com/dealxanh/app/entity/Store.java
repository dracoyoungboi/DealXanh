package com.dealxanh.app.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    private String storeName;
    private String description;
    private String address;
    private String phone;
    private String logoUrl;
    private String coverImageUrl;

    // Geolocation
    private Double latitude;
    private Double longitude;

    // Operating hours
    private LocalTime openTime;
    private LocalTime closeTime;

    // Rating
    private Double averageRating = 0.0;
    private Integer totalReviews = 0;

    // Status: PENDING, ACTIVE, SUSPENDED
    private String status = "PENDING";

    // --- New Fields for Registration Flow ---
    private String businessType;
    private String categories; // e.g. "Đồ ăn mặn, Đồ uống"
    private String city;
    private String district;
    
    // Documents
    private String cccdUrl;
    private String businessLicenseUrl;
    private String vsattpUrl;
    
    // Bank details
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountOwner;
    
    // Pickup config
    private String operatingDays; // e.g. "T2,T3,T4,T5,T6,T7"
    private String pickupSlots; // e.g. "16:00-18:00, 17:00-19:00"
    private Integer maxSlotsPerTime;
    private Integer pickupDurationMinutes;
    private String approvalMode; // "manual" or "auto"
    // ----------------------------------------

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Product> products;

    @OneToMany(mappedBy = "store")
    private List<Order> orders;

    // Danh sách nhân viên trực thuộc cửa hàng
    @OneToMany(mappedBy = "workStore")
    private List<User> staffList;

    public Store() {}

    // Getters & Setters
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }

    public Double getAverageRating() { return averageRating != null ? averageRating : 0.0; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews != null ? totalReviews : 0; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }

    public List<User> getStaffList() { return staffList; }
    public void setStaffList(List<User> staffList) { this.staffList = staffList; }

    // New Fields Getters and Setters
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getCategories() { return categories; }
    public void setCategories(String categories) { this.categories = categories; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getCccdUrl() { return cccdUrl; }
    public void setCccdUrl(String cccdUrl) { this.cccdUrl = cccdUrl; }

    public String getBusinessLicenseUrl() { return businessLicenseUrl; }
    public void setBusinessLicenseUrl(String businessLicenseUrl) { this.businessLicenseUrl = businessLicenseUrl; }

    public String getVsattpUrl() { return vsattpUrl; }
    public void setVsattpUrl(String vsattpUrl) { this.vsattpUrl = vsattpUrl; }

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
}
