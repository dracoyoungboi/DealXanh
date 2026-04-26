package com.dealxanh.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private Double totalAmount;
    private Double discountAmount = 0.0;
    private String couponCode;
    private Double finalAmount;

    // Trạng thái: PENDING, CONFIRMED, READY_FOR_PICKUP, COMPLETED, CANCELLED
    private String status = "PENDING";

    // Thanh toán: UNPAID, PAID
    private String paymentStatus = "UNPAID";

    // Phương thức: BANK_TRANSFER, CASH, MOMO, ZALOPAY
    private String paymentMethod = "BANK_TRANSFER";

    // Giờ lấy hàng khách chọn
    private LocalDateTime scheduledPickupTime;

    // Giờ lấy hàng thực tế (cửa hàng xác nhận qua QR)
    private LocalDateTime actualPickupTime;

    // QR code để cửa hàng quét xác nhận lấy hàng
    private String pickupQrCode;

    private String note;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    public Order() {}

    // Getters & Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getDiscountAmount() { return discountAmount != null ? discountAmount : 0.0; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public Double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getScheduledPickupTime() { return scheduledPickupTime; }
    public void setScheduledPickupTime(LocalDateTime scheduledPickupTime) { this.scheduledPickupTime = scheduledPickupTime; }

    public LocalDateTime getActualPickupTime() { return actualPickupTime; }
    public void setActualPickupTime(LocalDateTime actualPickupTime) { this.actualPickupTime = actualPickupTime; }

    public String getPickupQrCode() { return pickupQrCode; }
    public void setPickupQrCode(String pickupQrCode) { this.pickupQrCode = pickupQrCode; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}
