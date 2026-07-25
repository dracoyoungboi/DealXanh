package com.dealxanh.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    // Giá gốc trước khi deal
    private Double originalPrice;

    // Giá deal sau khi giảm
    private Double dealPrice;

    // Giá hiển thị hiện tại (bị ảnh hưởng bởi countdown HSD)
    private Double currentPrice;

    // Số lượng còn lại
    private Integer stockQuantity = 0;

    // Loại sản phẩm: SPECIFIC_DEAL, COMBO
    private String productType = "SPECIFIC_DEAL";

    // Hạn sử dụng / Ngày cận date
    private LocalDateTime expiryDate;

    // Ngày sản xuất
    private LocalDateTime manufacturingDate;

    // Đề xuất giảm giá (từ hệ thống, cho seller xác nhận)
    private Double suggestedDiscount;
    private Double suggestedPrice;
    private LocalDateTime suggestedAt;

    // Thời gian deal có hiệu lực
    private LocalDateTime dealStartTime;
    private LocalDateTime dealEndTime;

    // Khách phải đến lấy trước giờ này
    private LocalDateTime pickupDeadline;

    @Version
    private Long version;

    // Cờ kích hoạt
    private Boolean active = true;

    // Soft delete
    private Boolean deleted = false;

    // Trạng thái duyệt: PENDING, APPROVED, REJECTED
    private String approvalStatus = "PENDING";

    // Lý do từ chối (nếu rejected)
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews;

    public Product() {}

    // Helper: kiểm tra sản phẩm còn khả dụng không
    public boolean isAvailable() {
        if (Boolean.TRUE.equals(deleted) || !Boolean.TRUE.equals(active)) return false;
        if (!"APPROVED".equals(approvalStatus)) return false;
        if (stockQuantity == null || stockQuantity <= 0) return false;
        LocalDateTime now = LocalDateTime.now();
        if (dealStartTime != null && now.isBefore(dealStartTime)) return false;
        if (dealEndTime != null && now.isAfter(dealEndTime)) return false;
        return true;
    }

    // Phần trăm giảm giá
    @JsonIgnore
    public double getDiscountPercent() {
        if (originalPrice == null || originalPrice == 0 || dealPrice == null) return 0;
        return Math.round(((originalPrice - dealPrice) / originalPrice) * 100);
    }

    // Getters & Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }

    public Double getDealPrice() { return dealPrice; }
    public void setDealPrice(Double dealPrice) { this.dealPrice = dealPrice; }

    public Double getCurrentPrice() { return currentPrice != null ? currentPrice : originalPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }

    // Giá hiển thị cho buyer: ưu tiên currentPrice (countdown), fallback originalPrice
    @JsonIgnore
    public double getDisplayPrice() {
        if (currentPrice != null) return currentPrice;
        if (originalPrice != null) return originalPrice;
        return 0;
    }

    public Integer getStockQuantity() { return stockQuantity != null ? stockQuantity : 0; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public LocalDateTime getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDateTime manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public Double getSuggestedDiscount() { return suggestedDiscount; }
    public void setSuggestedDiscount(Double suggestedDiscount) { this.suggestedDiscount = suggestedDiscount; }

    public Double getSuggestedPrice() { return suggestedPrice; }
    public void setSuggestedPrice(Double suggestedPrice) { this.suggestedPrice = suggestedPrice; }

    public LocalDateTime getSuggestedAt() { return suggestedAt; }
    public void setSuggestedAt(LocalDateTime suggestedAt) { this.suggestedAt = suggestedAt; }

    /** Số giờ còn lại đến HSD (có thể âm nếu đã hết hạn) */
    public long getRemainingHours() {
        if (expiryDate == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.HOURS.between(java.time.LocalDateTime.now(), expiryDate);
    }

    /** Tổng số giờ từ NSX đến HSD */
    public long getTotalShelfLifeHours() {
        if (manufacturingDate == null || expiryDate == null) return -1;
        return java.time.temporal.ChronoUnit.HOURS.between(manufacturingDate, expiryDate);
    }

    /** Tỉ lệ thời gian còn lại (0.0 - 1.0), -1 nếu không tính được */
    public double getRemainingRatio() {
        long total = getTotalShelfLifeHours();
        if (total <= 0) return -1;
        long remaining = getRemainingHours();
        return Math.max(0.0, Math.min(1.0, (double) remaining / total));
    }

    public LocalDateTime getDealStartTime() { return dealStartTime; }
    public void setDealStartTime(LocalDateTime dealStartTime) { this.dealStartTime = dealStartTime; }

    public LocalDateTime getDealEndTime() { return dealEndTime; }
    public void setDealEndTime(LocalDateTime dealEndTime) { this.dealEndTime = dealEndTime; }

    public LocalDateTime getPickupDeadline() { return pickupDeadline; }
    public void setPickupDeadline(LocalDateTime pickupDeadline) { this.pickupDeadline = pickupDeadline; }

    public Boolean getActive() { return active != null ? active : true; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getDeleted() { return deleted != null ? deleted : false; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (approvalStatus == null) approvalStatus = "PENDING";
        if (currentPrice == null) currentPrice = originalPrice;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
