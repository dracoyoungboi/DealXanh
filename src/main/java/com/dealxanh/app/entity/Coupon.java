package com.dealxanh.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(unique = true)
    private String code;

    private String description;

    // PERCENT hoặc FIXED
    private String discountType;

    private Double discountValue;

    // Giá trị đơn hàng tối thiểu để áp dụng coupon
    private Double minimumOrderAmount = 0.0;

    // Giới hạn giảm tối đa (cho loại PERCENT)
    private Double maxDiscountAmount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer usageLimit;
    private Integer usedCount = 0;

    private Boolean active = true;

    private LocalDateTime createdAt;

    public Coupon() {}

    // Tính số tiền được giảm
    public Double calculateDiscount(Double orderAmount) {
        if (orderAmount == null || minimumOrderAmount != null && orderAmount < minimumOrderAmount) return 0.0;
        if ("PERCENT".equalsIgnoreCase(discountType)) {
            double discount = orderAmount * discountValue / 100.0;
            if (maxDiscountAmount != null) discount = Math.min(discount, maxDiscountAmount);
            return discount;
        } else {
            return Math.min(discountValue, orderAmount);
        }
    }

    // Getters & Setters
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    public Double getMinimumOrderAmount() { return minimumOrderAmount != null ? minimumOrderAmount : 0.0; }
    public void setMinimumOrderAmount(Double minimumOrderAmount) { this.minimumOrderAmount = minimumOrderAmount; }

    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public Integer getUsedCount() { return usedCount != null ? usedCount : 0; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }

    public Boolean getActive() { return active != null ? active : true; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
