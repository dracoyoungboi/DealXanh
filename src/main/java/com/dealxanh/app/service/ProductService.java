package com.dealxanh.app.service;

import com.dealxanh.app.concurrency.ResourceLockManager;
import com.dealxanh.app.entity.Product;
import com.dealxanh.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ResourceLockManager lockManager;

    // Get all products with pagination
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable);
    }

    // Get products by approval status
    public Page<Product> getProductsByApprovalStatus(String status, Pageable pageable) {
        return productRepository.findByApprovalStatus(status, pageable);
    }

    // Get pending approval products
    public List<Product> getPendingProducts() {
        return productRepository.findPendingApproval();
    }

    // Search products
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return productRepository.searchAvailable(keyword, now, pageable);
    }

    // Get product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .filter(p -> !p.getDeleted())
                .orElse(null);
    }

    // Create new product
    @Transactional
    public Product createProduct(Product product) {
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        if (product.getApprovalStatus() == null) {
            product.setApprovalStatus("APPROVED");
        }
        if (product.getActive() == null) {
            product.setActive(true);
        }
        if (product.getDeleted() == null) {
            product.setDeleted(false);
        }
        return productRepository.save(product);
    }

    // Update product
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        ReentrantLock lock = lockManager.acquireLock("PRODUCT", id);
        try {
            Product product = getProductById(id);
            if (product == null) return null;

            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setImageUrl(productDetails.getImageUrl());
            product.setOriginalPrice(productDetails.getOriginalPrice());
            product.setDealPrice(productDetails.getDealPrice());
            product.setStockQuantity(productDetails.getStockQuantity());
            product.setExpiryDate(productDetails.getExpiryDate());
            product.setDealStartTime(productDetails.getDealStartTime());
            product.setDealEndTime(productDetails.getDealEndTime());
            product.setPickupDeadline(productDetails.getPickupDeadline());
            product.setCategory(productDetails.getCategory());
            product.setUpdatedAt(LocalDateTime.now());

            return productRepository.save(product);
        } finally {
            lock.unlock();
        }
    }

    // Approve product
    @Transactional
    public Product approveProduct(Long id) {
        ReentrantLock lock = lockManager.acquireLock("PRODUCT", id);
        try {
            Product product = getProductById(id);
            if (product == null) return null;

            productRepository.approveProduct(id);
        return getProductById(id);
        } finally {
            lock.unlock();
        }
    }

    // Reject product
    @Transactional
    public Product rejectProduct(Long id, String reason) {
        ReentrantLock lock = lockManager.acquireLock("PRODUCT", id);
        try {
            Product product = getProductById(id);
            if (product == null) return null;

            productRepository.rejectProduct(id, reason != null ? reason : "");
            return getProductById(id);
        } finally {
            lock.unlock();
        }
    }

    // Toggle active status (Ngừng bán / Kích hoạt)
    @Transactional
    public Product toggleActive(Long id) {
        ReentrantLock lock = lockManager.acquireLock("PRODUCT", id);
        try {
            Product product = getProductById(id);
            if (product == null) return null;

            if (product.getActive()) {
                productRepository.setActive(id, false);
                return getProductById(id); // return fresh entity after @Modifying
            }

            if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
                throw new IllegalArgumentException("Không thể kích hoạt: sản phẩm đã hết hàng (stock = 0).");
            }
            if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Không thể kích hoạt: sản phẩm đã hết hạn (" +
                    product.getExpiryDate().toLocalDate().toString() + ").");
            }

            productRepository.setActive(id, true);
            return getProductById(id); // return fresh entity after @Modifying
        } finally {
            lock.unlock();
        }
    }

    // Soft delete
    @Transactional
    public boolean deleteProduct(Long id) {
        ReentrantLock lock = lockManager.acquireLock("PRODUCT", id);
        try {
            Product product = getProductById(id);
            if (product == null) return false;

            product.setDeleted(true);
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);

            return true;
        } finally {
            lock.unlock();
        }
    }

    // Get statistics
    public long getTotalProducts() {
        return productRepository.countByDeletedFalseAndActive(true);
    }

    public long getActiveProducts() {
        return productRepository.countByDeletedFalseAndActive(true);
    }

    public long getOutOfStockProducts() {
        return productRepository.countByDeletedFalseAndActive(false);
    }

    public long getPendingApprovalProducts() {
        return productRepository.countByDeletedFalseAndApprovalStatus("PENDING");
    }

    public long getApprovedProducts() {
        return productRepository.countByDeletedFalseAndApprovalStatus("APPROVED");
    }

    public long getRejectedProducts() {
        return productRepository.countByDeletedFalseAndApprovalStatus("REJECTED");
    }

    /** Lấy sản phẩm không thuộc deal ACTIVE nào — dùng cho "Gợi ý hôm nay" */
    public List<Product> getSuggestedProducts() {
        return productRepository.findProductsNotInActiveDeal(PageRequest.of(0, 50)).getContent();
    }
}
