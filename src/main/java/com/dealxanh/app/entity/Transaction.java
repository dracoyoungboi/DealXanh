package com.dealxanh.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // Có thể null nếu giao dịch rút tiền
    private Order order;

    /**
     * Payout (rút tiền từ sàn về ví), 
     * Sale (doanh thu bán hàng), 
     * Refund (hoàn tiền),
     * Fee (phí nền tảng)
     */
    private String type; 

    private Double amount;
    private Double platformFee = 0.0;
    
    // Số tiền ròng cửa hàng nhận được (amount - platformFee)
    private Double netAmount;

    // Trạng thái: COMPLETED, PENDING, FAILED
    private String status = "PENDING";

    private String description;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Transaction() {}

    // Getters & Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getPlatformFee() { return platformFee != null ? platformFee : 0.0; }
    public void setPlatformFee(Double platformFee) { this.platformFee = platformFee; }

    public Double getNetAmount() { return netAmount; }
    public void setNetAmount(Double netAmount) { this.netAmount = netAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
