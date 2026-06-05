package com.dealxanh.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * DealProduct Entity - Quản lý sản phẩm áp dụng cho Deal
 * Nhiều-nhiều: một deal có thể áp dụng cho nhiều sản phẩm, một sản phẩm có thể có nhiều deal
 */
@Entity
@Table(name = "deal_products")
public class DealProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dealProductId;

    @ManyToOne
    @JoinColumn(name = "deal_id")
    private Deal deal;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Giá gốc của sản phẩm tại thời điểm tạo deal (snapshot)
    private Double originalPrice;

    // Giá sale (sau khi áp dụng deal)
    private Double salePrice;

    // Số lượng sản phẩm có thể mua với giá deal (null = vô hạn)
    private Integer maxQuantity;

    // Số lượng đã bán
    private Integer soldQuantity;

    // Sắp xếp ưu tiên trong danh sách deal
    private Integer priority;

    @Version
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public DealProduct() {
        this.soldQuantity = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getDealProductId() { return dealProductId; }
    public void setDealProductId(Long dealProductId) { this.dealProductId = dealProductId; }

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }

    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }

    public Integer getMaxQuantity() { return maxQuantity; }
    public void setMaxQuantity(Integer maxQuantity) { this.maxQuantity = maxQuantity; }

    public Integer getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(Integer soldQuantity) { this.soldQuantity = soldQuantity; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Helper methods
    @JsonIgnore
    public Integer getRemainingQuantity() {
        if (maxQuantity == null) return -1; // Unlimited
        return Math.max(0, maxQuantity - (soldQuantity != null ? soldQuantity : 0));
    }

    @JsonIgnore
    public Double getDiscountPercentage() {
        if (originalPrice == null || originalPrice == 0 || salePrice == null) return 0.0;
        return ((originalPrice - salePrice) / originalPrice) * 100;
    }

    @JsonIgnore
    public boolean isAvailable() {
        if (maxQuantity == null) return true;
        return (soldQuantity != null ? soldQuantity : 0) < maxQuantity;
    }
}
