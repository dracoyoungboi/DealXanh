package com.dealxanh.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deal Entity - Quản lý các chương trình giảm giá/khuyến mãi
 * Tương tự Shopee Deals: Flash Sale, Voucher, Freeship, Combo
 *
 * 2 KIỂU ÁP DỤNG DEAL:
 * 1. CODE_REQUIRED (Voucher/Code) - User cần nhập mã giảm giá khi checkout
 * 2. AUTO_APPLY (Direct Discount) - Giảm giá tự động áp dụng, sản phẩm hiển thị giá sale ngay
 */
@Entity
@Table(name = "deals")
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dealId;

    // Thông tin cơ bản
    private String dealName;
    private String dealCode; // Mã deal (ví dụ: FLASH50, FREESHIP10)
    private String description;

    // Loại deal
    // FLASH_SALE - Flash Sale (giờ vàng)
    // VOUCHER - Phiếu giảm giá
    // FREESHIP - Miễn phí vận chuyển
    // COMBO - Combo giảm giá
    // SEASONAL - Sale theo mùa (9.9, 11.11, 12.12)
    private String dealType;

    // Giá trị giảm
    // Discount_type: PERCENT (%) hoặc FIXED (số tiền cố định)
    private String discountType;
    private Double discountValue; // % hoặc số tiền (VND)
    private Double maxDiscountAmount; // Số tiền giảm tối đa (cho % discount)

    // Điều kiện áp dụng
    private Double minOrderAmount; // Giá trị đơn tối thiểu để áp dụng
    private Long maxUsageCount; // Số lượt sử dụng tối đa (null = vô hạn)
    private Long usageCount; // Số lượt đã sử dụng
    private Long usagePerUser; // Số lượt sử dụng tối đa per user (null = vô hạn)

    // Thời gian
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Trạng thái: SCHEDULED, ACTIVE, PAUSED, ENDED, CANCELLED
    private String status;

    // Phạm vi áp dụng
    // ALL_STORES - Tất cả cửa hàng
    // SPECIFIC_STORES - Cửa hàng cụ thể
    // SPECIFIC_PRODUCTS - Sản phẩm cụ thể
    private String scope;

    // Cách áp dụng
    // CODE_REQUIRED - Cần nhập mã (Voucher/Code)
    // AUTO_APPLY - Tự động áp dụng (Giảm giá trực tiếp trên sản phẩm)
    private String applyMethod;

    // Ảnh deal
    private String imageUrl;
    private String bannerUrl;

    // Sắp xếp (ưu tiên hiển thị)
    private Integer priority;

    // Thông tin tạo/cập nhật
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    // Constructor
    public Deal() {
        this.status = "SCHEDULED";
        this.usageCount = 0L;
        this.discountType = "PERCENT";
        this.scope = "SPECIFIC_STORES";
        this.applyMethod = "CODE_REQUIRED"; // Default: need to enter code
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getDealId() { return dealId; }
    public void setDealId(Long dealId) { this.dealId = dealId; }

    public String getDealName() { return dealName; }
    public void setDealName(String dealName) { this.dealName = dealName; }

    public String getDealCode() { return dealCode; }
    public void setDealCode(String dealCode) { this.dealCode = dealCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDealType() { return dealType; }
    public void setDealType(String dealType) { this.dealType = dealType; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public Double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public Long getMaxUsageCount() { return maxUsageCount; }
    public void setMaxUsageCount(Long maxUsageCount) { this.maxUsageCount = maxUsageCount; }

    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }

    public Long getUsagePerUser() { return usagePerUser; }
    public void setUsagePerUser(Long usagePerUser) { this.usagePerUser = usagePerUser; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getApplyMethod() { return applyMethod; }
    public void setApplyMethod(String applyMethod) { this.applyMethod = applyMethod; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    // Helper methods
    public boolean isActive() {
        return "ACTIVE".equals(status) &&
               startTime != null && startTime.isBefore(LocalDateTime.now()) &&
               endTime != null && endTime.isAfter(LocalDateTime.now());
    }

    public boolean isScheduled() {
        return "SCHEDULED".equals(status) && startTime != null && startTime.isAfter(LocalDateTime.now());
    }

    public boolean isExpired() {
        return endTime != null && endTime.isBefore(LocalDateTime.now());
    }

    public boolean hasUsageRemaining() {
        return maxUsageCount == null || usageCount < maxUsageCount;
    }

    public Long getRemainingUsage() {
        if (maxUsageCount == null) return -1L; // Unlimited
        return Math.max(0, maxUsageCount - usageCount);
    }

    public Double getUsagePercentage() {
        if (maxUsageCount == null) return null;
        return (usageCount * 100.0) / maxUsageCount;
    }

    // Apply Method helpers
    public boolean requiresCode() {
        return "CODE_REQUIRED".equals(applyMethod);
    }

    public boolean isAutoApplied() {
        return "AUTO_APPLY".equals(applyMethod);
    }

    public String getApplyMethodText() {
        if ("AUTO_APPLY".equals(applyMethod)) {
            return "Tự động áp dụng";
        }
        return "Cần nhập mã";
    }

    // Calculate discount amount for a given order amount
    public Double calculateDiscount(Double orderAmount) {
        if (orderAmount == null || discountValue == null) return 0.0;

        Double discount = 0.0;
        if ("PERCENT".equals(discountType)) {
            discount = orderAmount * (discountValue / 100.0);
            if (maxDiscountAmount != null && discount > maxDiscountAmount) {
                discount = maxDiscountAmount;
            }
        } else if ("FIXED".equals(discountType)) {
            discount = discountValue;
        }

        return Math.max(0, discount);
    }
}
