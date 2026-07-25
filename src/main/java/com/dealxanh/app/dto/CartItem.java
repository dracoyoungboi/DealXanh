package com.dealxanh.app.dto;

import com.dealxanh.app.entity.Product;

public class CartItem {

    private Product product;
    private Integer quantity;

    public CartItem() {}

    public CartItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Tổng tiền dòng này (dùng dealPrice nếu có)
    public Double getSubtotal() {
        if (product == null || quantity == null) return 0.0;
        double price = (product.getDealPrice() != null) ? product.getDealPrice() : product.getOriginalPrice();
        return price * quantity;
    }

    // Tổng tiền theo giá gốc
    public Double getOriginalSubtotal() {
        if (product == null || quantity == null) return 0.0;
        return product.getOriginalPrice() * quantity;
    }

    // Tiền tiết kiệm được
    public Double getSavings() {
        return getOriginalSubtotal() - getSubtotal();
    }

    // Getters & Setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
