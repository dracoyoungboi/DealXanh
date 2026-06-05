package com.dealxanh.app.service;

import com.dealxanh.app.concurrency.ResourceLockManager;
import com.dealxanh.app.entity.*;
import com.dealxanh.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DealService {

    // Validation Constants
    private static final double MAX_DISCOUNT_PERCENT = 85.0;
    private static final double MIN_DISCOUNT_PERCENT = 1.0;
    private static final double MIN_SALE_PRICE_PERCENT = 0.20; // Absolute floor: min 20% of original
    private static final int MAX_DEAL_DURATION_DAYS = 30;
    private static final double MIN_FIXED_DISCOUNT = 1000.0;
    private static final double MAX_FIXED_DISCOUNT = 500000.0;

    // Tiered discount caps (platform + store combined)
    private static final double MAX_COMBINED_DISCOUNT_NORMAL = 65.0;   // Hàng thường
    private static final double MAX_COMBINED_DISCOUNT_FLASH_SALE = 70.0; // Flash Sale
    private static final double MAX_COMBINED_DISCOUNT_NEAR_EXPIRY = 80.0; // Hàng cận date

    // Platform-only caps (admin gán danh mục)
    private static final double MAX_PLATFORM_DISCOUNT = 30.0;    // Chặn cứng
    private static final double PLATFORM_DISCOUNT_WARNING = 28.0; // Cảnh báo

    // Near expiry threshold (days)
    private static final int NEAR_EXPIRY_DAYS = 3;

    // Deal type specific duration limits (in hours)
    private static final int FLASH_SALE_MAX_HOURS = 6;
    private static final int VOUCHER_MAX_DAYS = 30;
    private static final int COMBO_MAX_DAYS = 15;
    private static final int SEASONAL_MAX_DAYS = 30;

    // Discount limits based on average product price
    private static final double MAX_DISCOUNT_MULTIPLIER_LOW_PRICE = 0.5;  // For avg < 50k
    private static final double MAX_DISCOUNT_MULTIPLIER_MID_PRICE = 0.7;   // For avg 50k-200k
    private static final double MAX_DISCOUNT_MULTIPLIER_HIGH_PRICE = 0.8;  // For avg > 200k
    private static final double LOW_PRICE_THRESHOLD = 50000.0;
    private static final double HIGH_PRICE_THRESHOLD = 200000.0;

    // Minimum products requirement
    private static final int MIN_PRODUCTS_FOR_AUTO_APPLY = 1;
    private static final int MIN_CATEGORIES_FOR_PLATFORM_DEAL = 1;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private DealProductRepository dealProductRepository;

    @Autowired
    private DealCategoryRepository dealCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ResourceLockManager lockManager;

    // ============ DEAL CRUD ============

    public Page<Deal> getAllDeals(Pageable pageable) {
        return dealRepository.findAll(pageable);
    }

    public Deal getDealById(Long dealId) {
        return dealRepository.findById(dealId).orElse(null);
    }

    @Transactional
    public Deal createDeal(Deal deal) {
        // FIFO lock on store to prevent concurrent deal creation with overlapping times
        ReentrantLock lock = null;
        if (deal.getStore() != null) {
            lock = lockManager.acquireLock("STORE", deal.getStore().getStoreId());
        }
        try {
            validateDeal(deal, null); // null = new deal (no existing dealId)
            deal.setCreatedAt(LocalDateTime.now());
            deal.setUpdatedAt(LocalDateTime.now());
            deal.setStatus("ACTIVE");
            deal.setUsageCount(0L);
            return dealRepository.save(deal);
        } finally {
            if (lock != null) lock.unlock();
        }
    }

    @Transactional
    public Deal updateDeal(Long dealId, Deal dealDetails) {
        ReentrantLock lock = lockManager.acquireLock("DEAL", dealId);
        try {
            Deal deal = getDealById(dealId);
            if (deal == null) return null;

            deal.setDealName(dealDetails.getDealName());
            deal.setDealCode(dealDetails.getDealCode());
            deal.setDescription(dealDetails.getDescription());
            deal.setDealType(dealDetails.getDealType());
            deal.setDiscountType(dealDetails.getDiscountType());
            deal.setDiscountValue(dealDetails.getDiscountValue());
            deal.setMaxDiscountAmount(dealDetails.getMaxDiscountAmount());
            deal.setMinOrderAmount(dealDetails.getMinOrderAmount());
            deal.setMaxUsageCount(dealDetails.getMaxUsageCount());
            deal.setUsagePerUser(dealDetails.getUsagePerUser());
            deal.setStartTime(dealDetails.getStartTime());
            deal.setEndTime(dealDetails.getEndTime());
            deal.setApplyMethod(dealDetails.getApplyMethod());
            deal.setScope(dealDetails.getScope());
            deal.setImageUrl(dealDetails.getImageUrl());
            deal.setBannerUrl(dealDetails.getBannerUrl());
            deal.setPriority(dealDetails.getPriority());
            deal.setUpdatedAt(LocalDateTime.now());

            return dealRepository.save(deal);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public boolean deleteDeal(Long dealId) {
        ReentrantLock lock = lockManager.acquireLock("DEAL", dealId);
        try {
            Deal deal = getDealById(dealId);
            if (deal == null) return false;

            deal.setStatus("CANCELLED");
            deal.setUpdatedAt(LocalDateTime.now());
            dealRepository.save(deal);

            return true;
        } finally {
            lock.unlock();
        }
    }

    // ============ DEAL CATEGORIES (Admin gán categories vào platform deals) ============

    @Transactional
    public DealCategory addCategoryToDeal(Long dealId, Long categoryId, Integer priority) {
        ReentrantLock lock = lockManager.acquireLock("DEAL", dealId);
        try {
            Deal deal = getDealById(dealId);
            Category category = categoryRepository.findById(categoryId).orElse(null);

            if (deal == null || category == null) return null;

            if ("ALL_STORES".equals(deal.getScope()) && deal.getStore() == null) {
                validatePlatformCategoryDiscount(deal, category);
            }

            DealCategory existingDealCategory = dealCategoryRepository
                    .findByDealAndCategory(deal, category);
            if (existingDealCategory != null) {
                existingDealCategory.setActive(true);
                existingDealCategory.setPriority(priority);
                return dealCategoryRepository.save(existingDealCategory);
            }

            DealCategory dealCategory = new DealCategory();
            dealCategory.setDeal(deal);
            dealCategory.setCategory(category);
            dealCategory.setPriority(priority != null ? priority : 0);
            dealCategory.setActive(true);

            return dealCategoryRepository.save(dealCategory);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Validate that adding this platform deal to a category won't push any product
     * over the 30% cumulative platform discount cap.
     */
    private void validatePlatformCategoryDiscount(Deal newDeal, Category category) {
        List<Product> productsInCategory = productRepository.findByCategoryAndDeletedFalse(category);
        if (productsInCategory == null || productsInCategory.isEmpty()) return;

        double newDealDiscount = newDeal.getDiscountValue() != null ? newDeal.getDiscountValue() : 0;
        boolean isPercent = "PERCENT".equals(newDeal.getDiscountType());

        java.util.List<String> blockedProducts = new java.util.ArrayList<>();

        for (Product product : productsInCategory) {
            if (product.getOriginalPrice() == null || product.getOriginalPrice() <= 0) continue;

            // Sum all existing platform deal discounts on this product
            double existingPlatformDiscountPercent = getExistingPlatformDiscountPercent(product, newDeal);

            double newDiscountPercent = isPercent ? newDealDiscount : (newDealDiscount / product.getOriginalPrice() * 100);
            double totalPlatformPercent = existingPlatformDiscountPercent + newDiscountPercent;

            if (totalPlatformPercent > MAX_PLATFORM_DISCOUNT) {
                blockedProducts.add(String.format("%s (đã giảm %.0f%% + thêm %.0f%% = %.0f%%)",
                    product.getName(), existingPlatformDiscountPercent, newDiscountPercent, totalPlatformPercent));
            }
        }

        if (!blockedProducts.isEmpty()) {
            String productList = String.join(", ", blockedProducts.stream().limit(5).toList());
            if (blockedProducts.size() > 5) {
                productList += " và " + (blockedProducts.size() - 5) + " sản phẩm khác";
            }
            throw new IllegalArgumentException(String.format(
                "Không thể gán danh mục '%s': %d sản phẩm đã vượt giới hạn giảm giá nền tảng (%.0f%%). %s",
                category.getName(), blockedProducts.size(), MAX_PLATFORM_DISCOUNT, productList));
        }
    }

    /**
     * Calculate total existing platform discount percentage on a product (excluding the given deal).
     */
    private double getExistingPlatformDiscountPercent(Product product, Deal excludeDeal) {
        if (product.getCategory() == null) return 0;

        List<DealCategory> platformDealCategories = dealCategoryRepository
                .findActiveByCategory(product.getCategory());

        if (platformDealCategories == null || platformDealCategories.isEmpty()) return 0;

        double totalPercent = 0;
        for (DealCategory dc : platformDealCategories) {
            Deal platformDeal = dc.getDeal();
            if (platformDeal == null) continue;
            if (platformDeal.getDealId().equals(excludeDeal.getDealId())) continue;
            if (!"ACTIVE".equals(platformDeal.getStatus()) && !"SCHEDULED".equals(platformDeal.getStatus())) continue;

            if ("PERCENT".equals(platformDeal.getDiscountType())) {
                totalPercent += platformDeal.getDiscountValue() != null ? platformDeal.getDiscountValue() : 0;
            } else {
                if (product.getOriginalPrice() != null && product.getOriginalPrice() > 0) {
                    totalPercent += (platformDeal.getDiscountValue() / product.getOriginalPrice()) * 100;
                }
            }
        }
        return totalPercent;
    }

    @Transactional
    public boolean removeCategoryFromDeal(Long dealId, Long categoryId) {
        ReentrantLock lock = lockManager.acquireLock("DEAL", dealId);
        try {
            Deal deal = getDealById(dealId);
            Category category = categoryRepository.findById(categoryId).orElse(null);

            if (deal == null || category == null) return false;

            DealCategory dealCategory = dealCategoryRepository
                    .findByDealAndCategory(deal, category);
            if (dealCategory == null) return false;

            dealCategory.setActive(false);
            dealCategoryRepository.save(dealCategory);

            return true;
        } finally {
            lock.unlock();
        }
    }

    public List<DealCategory> getDealCategories(Long dealId) {
        Deal deal = getDealById(dealId);
        if (deal == null) return List.of();

        return dealCategoryRepository.findByDealAndActiveTrue(deal);
    }

    /**
     * Returns deal categories with platform discount warnings.
     * [{dealCategory, warning: "Sản phẩm X đã đạt 28% giảm giá nền tảng"}, ...]
     */
    public java.util.List<java.util.Map<String, Object>> getDealCategoriesWithWarnings(Long dealId) {
        Deal deal = getDealById(dealId);
        if (deal == null) return List.of();

        List<DealCategory> categories = dealCategoryRepository.findByDealAndActiveTrue(deal);
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();

        for (DealCategory dc : categories) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("dealCategory", dc);

            // Check products in this category for platform discount warnings
            if (dc.getCategory() != null) {
                java.util.List<String> warnings = buildPlatformDiscountWarnings(dc.getCategory(), deal);
                if (!warnings.isEmpty()) {
                    item.put("warnings", warnings);
                }
            }
            result.add(item);
        }
        return result;
    }

    private java.util.List<String> buildPlatformDiscountWarnings(Category category, Deal excludeDeal) {
        java.util.List<String> warnings = new java.util.ArrayList<>();
        List<Product> products = productRepository.findByCategoryAndDeletedFalse(category);
        if (products == null) return warnings;

        for (Product product : products) {
            if (product.getOriginalPrice() == null || product.getOriginalPrice() <= 0) continue;
            double existingPercent = getExistingPlatformDiscountPercent(product, excludeDeal);
            if (existingPercent >= PLATFORM_DISCOUNT_WARNING) {
                warnings.add(String.format("%s (đã giảm %.0f%% từ deal nền tảng khác)",
                    product.getName(), existingPercent));
            }
        }
        return warnings;
    }

    public List<Category> getAvailableCategoriesForDeal(Long dealId) {
        // Get categories that are not already in this deal
        Deal deal = getDealById(dealId);
        if (deal == null) return List.of();

        List<Long> existingCategoryIds = dealCategoryRepository
                .findByDealAndActiveTrue(deal)
                .stream()
                .map(dc -> dc.getCategory().getCategoryId())
                .toList();

        if (existingCategoryIds.isEmpty()) {
            return categoryRepository.findAll();
        }

        return categoryRepository.findAll().stream()
                .filter(cat -> !existingCategoryIds.contains(cat.getCategoryId()))
                .toList();
    }

    // ============ DEAL PRODUCTS (Seller gán products vào store deals) ============

    @Transactional
    public DealProduct addProductToDeal(Long dealId, Long productId, Double originalPrice,
                                          Double salePrice, Integer maxQuantity, Integer priority) {
        Deal deal = getDealById(dealId);
        Product product = productRepository.findById(productId).orElse(null);

        if (deal == null || product == null) {
            throw new IllegalArgumentException("Deal hoặc Product không tồn tại");
        }

        // FIFO lock on product to prevent concurrent addition to overlapping deals
        ReentrantLock lock = lockManager.acquireLock("PRODUCT", productId);
        try {
            validateProductPrices(originalPrice, salePrice);

            // Auto-cap sale price if deal has maxDiscountAmount
            if (deal.getMaxDiscountAmount() != null && deal.getMaxDiscountAmount() > 0) {
                double minAllowedSale = originalPrice - deal.getMaxDiscountAmount();
                if (salePrice < minAllowedSale) {
                    salePrice = minAllowedSale;
                }
            }

            if (maxQuantity != null && maxQuantity <= 0) {
                throw new IllegalArgumentException("Số lượng tối đa phải lớn hơn 0");
            }

            // Validate product is not in another overlapping active deal
            validateProductNotInOverlappingDeal(product, deal);

            // Validate cumulative discount from platform deals (category-based) doesn't exceed 65%
            validateCumulativeDiscount(product, deal, salePrice, originalPrice);

            DealProduct existingDealProduct = dealProductRepository
                    .findByDealAndProduct(deal, product);
            if (existingDealProduct != null) {
                existingDealProduct.setOriginalPrice(originalPrice);
                existingDealProduct.setSalePrice(salePrice);
                existingDealProduct.setMaxQuantity(maxQuantity);
                existingDealProduct.setPriority(priority);
                return dealProductRepository.save(existingDealProduct);
            }

            DealProduct dealProduct = new DealProduct();
            dealProduct.setDeal(deal);
            dealProduct.setProduct(product);
            dealProduct.setOriginalPrice(originalPrice);
            dealProduct.setSalePrice(salePrice);
            dealProduct.setMaxQuantity(maxQuantity);
            dealProduct.setSoldQuantity(0);
            dealProduct.setPriority(priority != null ? priority : 0);

            return dealProductRepository.save(dealProduct);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public boolean removeProductFromDeal(Long dealId, Long productId) {
        ReentrantLock lock = lockManager.acquireLock("PRODUCT", productId);
        try {
            Deal deal = getDealById(dealId);
            Product product = productRepository.findById(productId).orElse(null);

            if (deal == null || product == null) return false;

            DealProduct dealProduct = dealProductRepository
                    .findByDealAndProduct(deal, product);
            if (dealProduct == null) return false;

            dealProductRepository.delete(dealProduct);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public DealProduct updateDealProduct(Long dealProductId, DealProduct details) {
        DealProduct dealProduct = dealProductRepository.findById(dealProductId).orElse(null);
        if (dealProduct == null) return null;

        Long productId = dealProduct.getProduct() != null ? dealProduct.getProduct().getProductId() : null;
        ReentrantLock lock = productId != null ? lockManager.acquireLock("PRODUCT", productId) : null;
        try {
            dealProduct.setOriginalPrice(details.getOriginalPrice());
            dealProduct.setSalePrice(details.getSalePrice());
            dealProduct.setMaxQuantity(details.getMaxQuantity());
            dealProduct.setPriority(details.getPriority());

            return dealProductRepository.save(dealProduct);
        } finally {
            if (lock != null) lock.unlock();
        }
    }

    public List<DealProduct> getDealProducts(Long dealId) {
        Deal deal = getDealById(dealId);
        if (deal == null) return List.of();

        return dealProductRepository.findByDeal(deal);
    }

    // ============ STATISTICS ============

    public long getTotalDeals() {
        return dealRepository.count();
    }

    public long getActiveDeals() {
        return dealRepository.countByStatus("ACTIVE");
    }

    public long getScheduledDeals() {
        return dealRepository.countByStatus("SCHEDULED");
    }

    public long getPausedDeals() {
        return dealRepository.countByStatus("PAUSED");
    }

    public long getEndedDeals() {
        return dealRepository.countByStatus("ENDED");
    }

    // ============ VALIDATION METHODS ============

    private void validateDeal(Deal deal, Long existingDealId) {
        // 0. Reject deprecated FREESHIP type
        if ("FREESHIP".equals(deal.getDealType())) {
            throw new IllegalArgumentException("Freeship không còn được hỗ trợ. Vui lòng chọn loại deal khác.");
        }

        // 1. Validate deal code uniqueness
        validateDealCodeUniqueness(deal.getDealCode(), existingDealId);

        // 2. Validate discount value
        if (deal.getDiscountValue() == null) {
            throw new IllegalArgumentException("Giá trị giảm giá không được để trống");
        }

        if ("PERCENT".equals(deal.getDiscountType())) {
            validatePercentDiscount(deal.getDiscountValue());
        } else if ("FIXED".equals(deal.getDiscountType())) {
            validateFixedDiscount(deal.getDiscountValue());
            // 3. Validate fixed discount vs average product price
            validateFixedDiscountVsProductPrice(deal);
        }

        // 4. Validate time range by deal type
        if (deal.getStartTime() != null && deal.getEndTime() != null) {
            validateTimeRangeByDealType(deal);
        }

        // 5. Validate time overlap for same store
        validateTimeOverlap(deal, existingDealId);

        // 6. Validate seller eligibility
        validateSellerEligibility(deal);

        // 7. Validate min order amount
        if (deal.getMinOrderAmount() != null && deal.getMinOrderAmount() < 0) {
            throw new IllegalArgumentException("Giá trị đơn tối thiểu không được âm");
        }

        // 8. Validate auto-apply deals have products/categories
        validateAutoApplyDealRequirements(deal);
    }

    private void validateDealCodeUniqueness(String dealCode, Long excludeDealId) {
        if (dealCode == null || dealCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã deal không được để trống");
        }

        // Validate format: no spaces, special chars
        if (!dealCode.matches("^[A-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Mã deal chỉ được chứa chữ hoa, số, dấu gạch dưới và gạch ngang");
        }

        Long count = dealRepository.countByDealCodeAndStatusNotCancelled(dealCode, excludeDealId);
        if (count > 0) {
            throw new IllegalArgumentException("Mã deal '" + dealCode + "' đã tồn tại. Vui lòng chọn mã khác.");
        }
    }

    private void validateFixedDiscountVsProductPrice(Deal deal) {
        // Only validate for store deals (not platform deals)
        if (deal.getStore() == null) {
            return; // Platform deal - skip validation
        }

        Double avgPrice = dealRepository.getAverageProductPriceByStore(deal.getStore().getStoreId());
        if (avgPrice == null || avgPrice == 0) {
            throw new IllegalArgumentException("Store chưa có sản phẩm nào. Không thể tạo deal.");
        }

        Double discountValue = deal.getDiscountValue();

        // Calculate max allowed discount based on average product price
        double maxAllowedDiscount;
        if (avgPrice < LOW_PRICE_THRESHOLD) {
            // Low price products (avg < 50k): max discount = 50% of avg price
            maxAllowedDiscount = avgPrice * MAX_DISCOUNT_MULTIPLIER_LOW_PRICE;
        } else if (avgPrice < HIGH_PRICE_THRESHOLD) {
            // Mid price products (50k - 200k): max discount = 70% of avg price
            maxAllowedDiscount = avgPrice * MAX_DISCOUNT_MULTIPLIER_MID_PRICE;
        } else {
            // High price products (> 200k): max discount = 80% of avg price
            maxAllowedDiscount = avgPrice * MAX_DISCOUNT_MULTIPLIER_HIGH_PRICE;
        }

        if (discountValue > maxAllowedDiscount) {
            throw new IllegalArgumentException(String.format(
                "Giảm tiền cố định %,.0fđ quá lớn so với giá sản phẩm trung bình của store (%,.0fđ). " +
                "Tối đa cho phép: %,.0fđ",
                discountValue, avgPrice, maxAllowedDiscount
            ));
        }
    }

    private void validateTimeRangeByDealType(Deal deal) {
        LocalDateTime startTime = deal.getStartTime();
        LocalDateTime endTime = deal.getEndTime();

        // Basic validation: end > start
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        String dealType = deal.getDealType();
        long hours = ChronoUnit.HOURS.between(startTime, endTime);
        long days = ChronoUnit.DAYS.between(startTime, endTime);

        int maxHours = Integer.MAX_VALUE;
        int maxDays = MAX_DEAL_DURATION_DAYS;

        switch (dealType) {
            case "FLASH_SALE":
                maxHours = FLASH_SALE_MAX_HOURS;
                if (hours > maxHours) {
                    throw new IllegalArgumentException(String.format(
                        "Flash Sale chỉ được chạy tối đa %d giờ. Bạn đang chọn %d giờ.",
                        maxHours, hours
                    ));
                }
                break;

            case "VOUCHER":
                maxDays = VOUCHER_MAX_DAYS;
                if (days > maxDays) {
                    throw new IllegalArgumentException(String.format(
                        "Voucher chỉ được chạy tối đa %d ngày. Bạn đang chọn %d ngày.",
                        maxDays, days
                    ));
                }
                break;

            case "COMBO":
                maxDays = COMBO_MAX_DAYS;
                if (days > maxDays) {
                    throw new IllegalArgumentException(String.format(
                        "Combo chỉ được chạy tối đa %d ngày. Bạn đang chọn %d ngày.",
                        maxDays, days
                    ));
                }
                break;

            case "SEASONAL":
                maxDays = SEASONAL_MAX_DAYS;
                if (days > maxDays) {
                    throw new IllegalArgumentException(String.format(
                        "Seasonal Sale chỉ được chạy tối đa %d ngày. Bạn đang chọn %d ngày.",
                        maxDays, days
                    ));
                }
                break;
        }
    }

    private void validateTimeOverlap(Deal deal, Long excludeDealId) {
        // Only validate for store deals
        if (deal.getStore() == null) {
            return;
        }

        List<Deal> overlappingDeals = dealRepository.findOverlappingDealsForStore(
            deal.getStore().getStoreId(),
            deal.getEndTime(),
            deal.getStartTime(),
            excludeDealId
        );

        if (!overlappingDeals.isEmpty()) {
            String overlappingDealNames = overlappingDeals.stream()
                .map(Deal::getDealName)
                .limit(2)
                .collect(java.util.stream.Collectors.joining(", "));

            int moreCount = overlappingDeals.size() - 2;
            String suffix = moreCount > 0 ? " và " + moreCount + " deal khác" : "";

            throw new IllegalArgumentException(String.format(
                "Thời gian deal trùng với các deal đang chạy: %s%s. " +
                "Vui lòng chọn thời gian khác hoặc hủy các deal cũ.",
                overlappingDealNames, suffix
            ));
        }
    }

    private void validateSellerEligibility(Deal deal) {
        // Only validate for store deals
        if (deal.getStore() == null) {
            return;
        }

        Long approvedProductCount = dealRepository.countApprovedProductsByStore(deal.getStore().getStoreId());
        if (approvedProductCount == 0) {
            throw new IllegalArgumentException(
                "Store chưa có sản phẩm nào được duyệt. " +
                "Vui lòng thêm sản phẩm và chờ admin duyệt trước khi tạo deal."
            );
        }
    }

    private void validateAutoApplyDealRequirements(Deal deal) {
        // Only validate for AUTO_APPLY method
        if (!"AUTO_APPLY".equals(deal.getApplyMethod())) {
            return;
        }

        // For store deals: must have at least MIN_PRODUCTS
        if (deal.getStore() != null) {
            // This will be validated when products are added
            // For now, just warn that products must be added
            return;
        }

        // For platform deals: must have at least MIN_CATEGORIES
        if ("ALL_STORES".equals(deal.getScope())) {
            // This will be validated when categories are assigned
            // For now, just warn that categories must be added
            return;
        }
    }

    private void validatePercentDiscount(Double discountValue) {
        if (discountValue < MIN_DISCOUNT_PERCENT) {
            throw new IllegalArgumentException(
                String.format("Giảm giá phần trăm tối thiểu %,.0f%%", MIN_DISCOUNT_PERCENT)
            );
        }
        if (discountValue > MAX_DISCOUNT_PERCENT) {
            throw new IllegalArgumentException(
                String.format("Giảm giá phần trăm tối đa %,.0f%%", MAX_DISCOUNT_PERCENT)
            );
        }
    }

    private void validateFixedDiscount(Double discountValue) {
        if (discountValue < MIN_FIXED_DISCOUNT) {
            throw new IllegalArgumentException(
                String.format("Giảm tiền cố định tối thiểu %,.0fđ", MIN_FIXED_DISCOUNT)
            );
        }
        if (discountValue > MAX_FIXED_DISCOUNT) {
            throw new IllegalArgumentException(
                String.format("Giảm tiền cố định tối đa %,.0fđ", MAX_FIXED_DISCOUNT)
            );
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        long days = ChronoUnit.DAYS.between(startTime, endTime);
        if (days > MAX_DEAL_DURATION_DAYS) {
            throw new IllegalArgumentException(
                String.format("Thời gian deal không vượt quá %d ngày", MAX_DEAL_DURATION_DAYS)
            );
        }
    }

    private void validateProductNotInOverlappingDeal(Product product, Deal currentDeal) {
        // Find all deal products for this product
        List<DealProduct> existingDealProducts = dealProductRepository.findByProduct(product);

        if (existingDealProducts == null || existingDealProducts.isEmpty()) {
            return; // Product not in any deal
        }

        LocalDateTime currentStart = currentDeal.getStartTime();
        LocalDateTime currentEnd = currentDeal.getEndTime();

        for (DealProduct dp : existingDealProducts) {
            Deal otherDeal = dp.getDeal();
            // Skip if same deal or deal is cancelled/ended
            if (otherDeal.getDealId().equals(currentDeal.getDealId())) continue;
            if ("CANCELLED".equals(otherDeal.getStatus()) || "ENDED".equals(otherDeal.getStatus())) continue;

            // Check time overlap
            LocalDateTime otherStart = otherDeal.getStartTime();
            LocalDateTime otherEnd = otherDeal.getEndTime();

            if (currentStart != null && currentEnd != null && otherStart != null && otherEnd != null) {
                boolean overlaps = currentStart.isBefore(otherEnd) && currentEnd.isAfter(otherStart);
                if (overlaps) {
                    throw new IllegalArgumentException(String.format(
                        "Sản phẩm '%s' đã có trong deal '%s' (thời gian: %s - %s). Không thể thêm vào deal này.",
                        product.getName(),
                        otherDeal.getDealName(),
                        otherStart.toLocalDate().toString(),
                        otherEnd.toLocalDate().toString()
                    ));
                }
            }
        }
    }

    private void validateCumulativeDiscount(Product product, Deal currentDeal,
                                              Double salePrice, Double originalPrice) {
        if (product.getCategory() == null) return;

        List<DealCategory> activeDealCategories = dealCategoryRepository
                .findActiveByCategory(product.getCategory());

        if (activeDealCategories == null || activeDealCategories.isEmpty()) return;

        double maxCap = getMaxDiscountCap(product, currentDeal);

        for (DealCategory dc : activeDealCategories) {
            Deal platformDeal = dc.getDeal();
            if (platformDeal == null) continue;
            if (platformDeal.getDealId().equals(currentDeal.getDealId())) continue;
            if (!"ACTIVE".equals(platformDeal.getStatus()) && !"SCHEDULED".equals(platformDeal.getStatus())) continue;
            // Skip CODE_REQUIRED platform deals — only AUTO_APPLY affects cumulative cap
            if (!"AUTO_APPLY".equals(platformDeal.getApplyMethod())) continue;

            if (currentDeal.getStartTime() != null && currentDeal.getEndTime() != null
                && platformDeal.getStartTime() != null && platformDeal.getEndTime() != null) {
                boolean overlaps = currentDeal.getStartTime().isBefore(platformDeal.getEndTime())
                        && currentDeal.getEndTime().isAfter(platformDeal.getStartTime());
                if (!overlaps) continue;
            }

            double platformDiscount = calculateDealDiscount(platformDeal, originalPrice);
            double currentDiscount = originalPrice - salePrice;
            double totalDiscountPercent = ((platformDiscount + currentDiscount) / originalPrice) * 100.0;

            if (totalDiscountPercent > maxCap) {
                throw new IllegalArgumentException(String.format(
                    "Sản phẩm '%s' không thể giảm thêm. " +
                    "Danh mục đã có deal nền tảng '%s' giảm %s. " +
                    "Tổng giảm sẽ là %,.0f%% (vượt quá giới hạn %,.0f%% cho %s).",
                    product.getName(),
                    platformDeal.getDealName(),
                    formatDiscountDisplay(platformDeal),
                    totalDiscountPercent,
                    maxCap,
                    getCapLabel(product, currentDeal)
                ));
            }
        }
    }

    /**
     * Returns the maximum allowed combined discount percentage for a product+deal.
     */
    private double getMaxDiscountCap(Product product, Deal deal) {
        if (product.getExpiryDate() != null
                && product.getExpiryDate().isBefore(java.time.LocalDateTime.now().plusDays(NEAR_EXPIRY_DAYS))) {
            return MAX_COMBINED_DISCOUNT_NEAR_EXPIRY;
        }
        if ("FLASH_SALE".equals(deal.getDealType())) {
            return MAX_COMBINED_DISCOUNT_FLASH_SALE;
        }
        return MAX_COMBINED_DISCOUNT_NORMAL;
    }

    private String getCapLabel(Product product, Deal deal) {
        if (product.getExpiryDate() != null
                && product.getExpiryDate().isBefore(java.time.LocalDateTime.now().plusDays(NEAR_EXPIRY_DAYS))) {
            return "hàng cận date";
        }
        if ("FLASH_SALE".equals(deal.getDealType())) {
            return "Flash Sale";
        }
        return "hàng thường";
    }

    private double calculateDealDiscount(Deal deal, double originalPrice) {
        if ("PERCENT".equals(deal.getDiscountType())) {
            double discount = originalPrice * deal.getDiscountValue() / 100.0;
            if (deal.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, deal.getMaxDiscountAmount());
            }
            return discount;
        }
        return deal.getDiscountValue() != null ? deal.getDiscountValue() : 0;
    }

    private String formatDiscountDisplay(Deal deal) {
        if ("PERCENT".equals(deal.getDiscountType())) {
            return String.format("%.0f%%", deal.getDiscountValue());
        }
        return String.format("%,.0fđ", deal.getDiscountValue());
    }

    private void validateProductPrices(Double originalPrice, Double salePrice) {
        if (originalPrice == null || salePrice == null) {
            throw new IllegalArgumentException("Giá gốc và giá sale không được để trống");
        }
        if (originalPrice <= 0) {
            throw new IllegalArgumentException("Giá gốc phải lớn hơn 0");
        }
        double minPrice = originalPrice * MIN_SALE_PRICE_PERCENT;
        if (salePrice < minPrice) {
            throw new IllegalArgumentException(
                String.format("Sản phẩm đã giảm giá kịch sàn, không thể giảm hơn. Giá sale tối thiểu %,.0fđ (tối đa giảm 65%% giá gốc %,.0fđ)", minPrice, originalPrice)
            );
        }
        if (salePrice >= originalPrice) {
            throw new IllegalArgumentException("Giá sale phải nhỏ hơn giá gốc");
        }
    }
}
