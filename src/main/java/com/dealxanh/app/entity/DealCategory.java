package com.dealxanh.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * DealCategory Entity - Gán Category vào Deal
 * Nhiều-nhiều: một deal có thể áp dụng cho nhiều categories, một category có thể có nhiều deals
 */
@Entity
@Table(name = "deal_categories")
public class DealCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dealCategoryId;

    @ManyToOne
    @JoinColumn(name = "deal_id")
    private Deal deal;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Ưu tiên hiển thị trong danh sách categories của deal
    private Integer priority;

    // Cờ kích hoạt (có thể tắt category này khỏi deal mà không xóa)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public DealCategory() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getDealCategoryId() { return dealCategoryId; }
    public void setDealCategoryId(Long dealCategoryId) { this.dealCategoryId = dealCategoryId; }

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
