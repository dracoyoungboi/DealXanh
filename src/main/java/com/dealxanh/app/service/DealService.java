package com.dealxanh.app.service;

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

@Service
public class DealService {

    // Validation Constants
    private static final double MAX_DISCOUNT_PERCENT = 85.0;
    private static final double MIN_DISCOUNT_PERCENT = 1.0;
    private static final double MIN_SALE_PRICE_PERCENT = 0.15;
    private static final int MAX_DEAL_DURATION_DAYS = 30;
    private static final double MIN_FIXED_DISCOUNT = 1000.0;
    private static final double MAX_FIXED_DISCOUNT = 500000.0;

    // Deal type specific duration limits (in hours)
    private static final int FLASH_SALE_MAX_HOURS = 6;
    private static final int VOUCHER_MAX_DAYS = 30;
    private static final int FREESHIP_MAX_DAYS = 30;
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

    // ============ DEAL CRUD ============

    public Page<Deal> getAllDeals(Pageable pageable) {
        return dealRepository.findAll(pageable);
    }

    public Deal getDealById(Long dealId) {
        return dealRepository.findById(dealId).orElse(null);
    }

    @Transactional
    public Deal createDeal(Deal deal) {
        validateDeal(deal, null); // null = new deal (no existing dealId)
        deal.setCreatedAt(LocalDateTime.now());
        deal.setUpdatedAt(LocalDateTime.now());
        deal.setStatus("SCHEDULED");
        deal.setUsageCount(0L);
        return dealRepository.save(deal);
    }

    @Transactional
    public Deal updateDeal(Long dealId, Deal dealDetails) {
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
    }

    @Transactional
    public boolean deleteDeal(Long dealId) {
        Deal deal = getDealById(dealId);
        if (deal == null) return false;

        deal.setStatus("CANCELLED");
        deal.setUpdatedAt(LocalDateTime.now());
        dealRepository.save(deal);

        return true;
    }

    // ============ DEAL CATEGORIES (Admin gán categories vào platform deals) ============

    @Transactional
    public DealCategory addCategoryToDeal(Long dealId, Long categoryId, Integer priority) {
        Deal deal = getDealById(dealId);
        Category category = categoryRepository.findById(categoryId).orElse(null);

        if (deal == null || category == null) {
            return null;
        }

        // Check if deal category already exists
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
    }

    @Transactional
    public boolean removeCategoryFromDeal(Long dealId, Long categoryId) {
        Deal deal = getDealById(dealId);
        Category category = categoryRepository.findById(categoryId).orElse(null);

        if (deal == null || category == null) {
            return false;
        }

        DealCategory dealCategory = dealCategoryRepository
                .findByDealAndCategory(deal, category);
        if (dealCategory == null) {
            return false;
        }

        dealCategory.setActive(false);
        dealCategoryRepository.save(dealCategory);

        return true;
    }

    public List<DealCategory> getDealCategories(Long dealId) {
        Deal deal = getDealById(dealId);
        if (deal == null) return List.of();

        return dealCategoryRepository.findByDealAndActiveTrue(deal);
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

        validateProductPrices(originalPrice, salePrice);

        if (maxQuantity != null && maxQuantity <= 0) {
            throw new IllegalArgumentException("Số lượng tối đa phải lớn hơn 0");
        }

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
    }

    @Transactional
    public boolean removeProductFromDeal(Long dealId, Long productId) {
        Deal deal = getDealById(dealId);
        Product product = productRepository.findById(productId).orElse(null);

        if (deal == null || product == null) {
            return false;
        }

        DealProduct dealProduct = dealProductRepository
                .findByDealAndProduct(deal, product);
        if (dealProduct == null) {
            return false;
        }

        dealProductRepository.delete(dealProduct);
        return true;
    }

    @Transactional
    public DealProduct updateDealProduct(Long dealProductId, DealProduct details) {
        DealProduct dealProduct = dealProductRepository.findById(dealProductId).orElse(null);
        if (dealProduct == null) return null;

        dealProduct.setOriginalPrice(details.getOriginalPrice());
        dealProduct.setSalePrice(details.getSalePrice());
        dealProduct.setMaxQuantity(details.getMaxQuantity());
        dealProduct.setPriority(details.getPriority());

        return dealProductRepository.save(dealProduct);
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

            case "FREESHIP":
                maxDays = FREESHIP_MAX_DAYS;
                if (days > maxDays) {
                    throw new IllegalArgumentException(String.format(
                        "Freeship chỉ được chạy tối đa %d ngày. Bạn đang chọn %d ngày.",
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
                String.format("Giá sale tối thiểu %,.0fđ (15%% giá gốc)", minPrice)
            );
        }
        if (salePrice >= originalPrice) {
            throw new IllegalArgumentException("Giá sale phải nhỏ hơn giá gốc");
        }
    }
}
