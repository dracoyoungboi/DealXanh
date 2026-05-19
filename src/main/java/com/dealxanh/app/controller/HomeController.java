package com.dealxanh.app.controller;

import com.dealxanh.app.entity.Deal;
import com.dealxanh.app.entity.DealCategory;
import com.dealxanh.app.entity.DealProduct;
import com.dealxanh.app.entity.Order;
import com.dealxanh.app.entity.Product;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.repository.DealRepository;
import com.dealxanh.app.repository.DealCategoryRepository;
import com.dealxanh.app.repository.DealProductRepository;
import com.dealxanh.app.repository.OrderRepository;
import com.dealxanh.app.repository.ProductRepository;
import com.dealxanh.app.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private DealCategoryRepository dealCategoryRepository;

    @Autowired
    private DealProductRepository dealProductRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("activeNav", "home");
        model.addAttribute("cartItemCount", 0);
        model.addAttribute("notifCount", 0);

        // Stats
        model.addAttribute("totalStores", storeRepository.countByStatus("ACTIVE"));
        model.addAttribute("totalUsers", "45K");
        model.addAttribute("totalDeals", dealRepository.countActiveDeals());

        // Fetch active deals with categories and products
        LocalDateTime now = LocalDateTime.now();
        List<Deal> activeDeals = dealRepository.findByStatusAndStartTimeBeforeAndEndTimeAfterOrderByPriorityDesc(
                "ACTIVE", now, now);

        // Build featured deals DTO list
        List<Map<String, Object>> featuredDeals = new ArrayList<>();
        for (Deal deal : activeDeals) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("dealId", deal.getDealId());
            dto.put("dealName", deal.getDealName());
            dto.put("dealType", deal.getDealType());
            dto.put("dealCode", deal.getDealCode());
            dto.put("description", deal.getDescription());
            dto.put("discountType", deal.getDiscountType());
            dto.put("discountValue", deal.getDiscountValue());

            // Calculate discount percent for display
            double discountPercent = 0;
            if ("PERCENT".equals(deal.getDiscountType())) {
                discountPercent = deal.getDiscountValue();
            } else {
                // For FIXED, estimate percent from deal products
                List<DealProduct> dps = dealProductRepository.findByDeal(deal);
                if (dps != null && !dps.isEmpty()) {
                    double avgOriginal = dps.stream().mapToDouble(DealProduct::getOriginalPrice).average().orElse(100000);
                    discountPercent = (deal.getDiscountValue() / avgOriginal) * 100;
                }
            }
            dto.put("discountPercent", discountPercent);
            dto.put("minOrderAmount", deal.getMinOrderAmount() != null ? deal.getMinOrderAmount() : 0);
            dto.put("bannerUrl", deal.getBannerUrl());
            dto.put("storeName", deal.getStore() != null ? deal.getStore().getStoreName() : null);
            dto.put("applyMethod", deal.getApplyMethod());

            // Categories (names only for display)
            List<DealCategory> dealCategories = dealCategoryRepository.findByDealAndActiveTrue(deal);
            List<String> categoryNames = new ArrayList<>();
            if (dealCategories != null) {
                for (DealCategory dc : dealCategories) {
                    if (dc.getCategory() != null) {
                        categoryNames.add(dc.getCategory().getName());
                    }
                }
            }
            dto.put("categories", categoryNames);

            // Products (names only for display)
            List<DealProduct> dealProducts = dealProductRepository.findByDeal(deal);
            List<String> productNames = new ArrayList<>();
            if (dealProducts != null) {
                for (DealProduct dp : dealProducts) {
                    if (dp.getProduct() != null) {
                        productNames.add(dp.getProduct().getName());
                    }
                }
            }
            dto.put("products", productNames);

            featuredDeals.add(dto);
        }

        model.addAttribute("featuredDeals", featuredDeals);
        return "buyer/home";
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        return home(model);
    }

    @GetMapping("/store/{storeId}")
    public String storePage(@PathVariable Long storeId, Model model) {
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null || !"ACTIVE".equals(store.getStatus())) {
            return "redirect:/home";
        }

        // Active deals for this store
        LocalDateTime now = LocalDateTime.now();
        List<Deal> activeDeals = dealRepository.findByStoreStoreIdAndStatus(storeId, "ACTIVE");
        activeDeals = activeDeals.stream()
            .filter(d -> d.getStartTime() != null && d.getStartTime().isBefore(now)
                      && d.getEndTime() != null && d.getEndTime().isAfter(now))
            .toList();

        // Active products
        List<Product> products = productRepository.findByStoreStoreIdAndDeletedFalse(storeId).stream()
            .filter(p -> p.getActive() && "APPROVED".equals(p.getApprovalStatus()))
            .toList();

        model.addAttribute("store", store);
        model.addAttribute("activeDeals", activeDeals);
        model.addAttribute("products", products);
        return "buyer/store";
    }

    // ============ PICKUP CONFIRMATION (QR scan) ============

    @GetMapping("/pickup/{qrCode}")
    public String pickupConfirm(@PathVariable String qrCode, Model model) {
        Order order = orderRepository.findByPickupQrCode(qrCode).orElse(null);
        if (order == null) {
            model.addAttribute("error", "Mã QR không hợp lệ hoặc đơn hàng không tồn tại");
            return "buyer/pickup-confirm";
        }

        if ("COMPLETED".equals(order.getStatus())) {
            model.addAttribute("alreadyCompleted", true);
            model.addAttribute("order", order);
            return "buyer/pickup-confirm";
        }

        if (!"READY_FOR_PICKUP".equals(order.getStatus())) {
            model.addAttribute("error", "Đơn hàng chưa sẵn sàng để nhận. Trạng thái hiện tại: " + order.getStatus());
            model.addAttribute("order", order);
            return "buyer/pickup-confirm";
        }

        model.addAttribute("order", order);
        model.addAttribute("items", order.getOrderItems());
        return "buyer/pickup-confirm";
    }

    @PostMapping("/pickup/{qrCode}/confirm")
    @ResponseBody
    public java.util.Map<String, Object> confirmPickup(@PathVariable String qrCode) {
        Order order = orderRepository.findByPickupQrCode(qrCode).orElse(null);
        if (order == null) return java.util.Map.of("success", false, "message", "Mã QR không hợp lệ");

        if ("COMPLETED".equals(order.getStatus()))
            return java.util.Map.of("success", false, "message", "Đơn hàng đã được nhận trước đó");

        if (!"READY_FOR_PICKUP".equals(order.getStatus()))
            return java.util.Map.of("success", false, "message", "Đơn hàng chưa sẵn sàng để nhận");

        order.setStatus("COMPLETED");
        order.setActualPickupTime(java.time.LocalDateTime.now());
        orderRepository.save(order);

        return java.util.Map.of("success", true, "message", "✅ Xác nhận nhận hàng thành công! Cảm ơn bạn đã mua sắm tại DealXanh.");
    }
}
