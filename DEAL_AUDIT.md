# Deal System Audit — Phân Tích & Đề Xuất Sửa Đổi

> Dựa trên file `dealxanh_master.sql` (backup DB hiện tại) + toàn bộ codebase

---

## 1. Dữ Liệu Hiện Tại — 12 Deals

| ID | Tên | Loại | Discount | Status | Scope | Start | End | Store |
|----|-----|------|----------|--------|-------|-------|-----|-------|
| D1 | Flash Sale 40% Phở Bún | FLASH_SALE | PERCENT 40% (max 40k) | **ACTIVE** ⚠️ | Store1 | 14/5 | **28/5** | Store1 |
| D2 | Voucher 20K Khai Trương | VOUCHER | FIXED 20k | ACTIVE | Platform | 10/5 | 10/6 | NULL |
| D3 | Combo Cơm Trà Siêu Rẻ | COMBO | FIXED 15k | **ACTIVE** ⚠️ | Store3 | 12/5 | **28/5** | Store3 |
| D4 | Sale Hè 2026 25% | SEASONAL | PERCENT 25% (max 50k) | **SCHEDULED** ⚠️ | Platform | **28/5** | 15/6 | NULL |
| D5 | Flash Sale 50% Bánh Ngọt | FLASH_SALE | PERCENT 50% (max 30k) | ENDED ✅ | Store4 | 16/5 | 16/5 | Store4 |
| D6 | Voucher 15% Người Mới | VOUCHER | PERCENT 15% (max 30k) | ACTIVE | Platform | 1/5 | 1/7 | NULL |
| D7 | Combo Bánh Trà Chiều | COMBO | FIXED 10k | **SCHEDULED** ⚠️ | Store4 | **28/5** | 5/6 | Store4 |
| D8 | Tết Đoan Ngọ 20% | SEASONAL | PERCENT 20% (max 40k) | SCHEDULED | Platform | 30/5 | 10/6 | NULL |
| D9 | Flash Sale Cơm Trưa 30% | FLASH_SALE | PERCENT 30% (max 20k) | ENDED ✅ | Store1 | 1/5 | 1/5 | Store1 |
| D10 | Voucher 50K Đơn Lớn | VOUCHER | FIXED 50k | ACTIVE | Platform | 8/5 | 8/6 | NULL |
| D11 | Sale Cuối Tuần 15% | SEASONAL | PERCENT 15% (max 30k) | PAUSED | Platform | 20/5 | 20/6 | NULL |
| D12 | Flash Sale Phở Bò 35% | FLASH_SALE | PERCENT 35% (max 35k) | PAUSED | Store1 | 18/5 | 30/5 | Store1 |

> ⚠️ = Có vấn đề (xem phân tích bên dưới)

---

## 2. Phát Hiện Vấn Đề

### 2.1. CRITICAL: Deal Hết Hạn Nhưng Vẫn ACTIVE

```
D1: endTime = 2026-05-28, hôm nay = 2026-05-31 → ĐÃ HẾT HẠN 3 NGÀY, vẫn ACTIVE ❌
D3: endTime = 2026-05-28, hôm nay = 2026-05-31 → ĐÃ HẾT HẠN 3 NGÀY, vẫn ACTIVE ❌
```

**Nguyên nhân:** `ExpiryCountdownService.endExpiredDeals()` chạy mỗi 30 phút. Có thể service chưa kịp chạy, hoặc query bị lỗi trong phiên chạy trước. Nhưng dù sao, đây là bug — deal quá hạn vẫn hiển thị cho buyer, gây nhầm lẫn.

**Fix:** Đảm bảo scheduler chạy đúng. Thêm check ở đầu mỗi request: nếu deal ACTIVE mà endTime < now → tự động ENDED.

### 2.2. CRITICAL: FIXED Deal — Sale Price Không Khớp DiscountValue

```
D3 (Combo Cơm Trà): discountValue = 15,000đ FIXED
├─ P3 (Cơm tấm):  original 40k → sale 28k → discount = 12k
└─ P3 (Trà sữa):  original 30k → sale 25k → discount = 5k
   TỔNG DISCOUNT = 17k ≠ 15k ❌ (lệch 2,000đ!)

D7 (Combo Bánh Trà): discountValue = 10,000đ FIXED
├─ P4 (Bánh quy):  original 60k → sale 50k → discount = 10k
└─ P4 (Kẹo dẻo):   original 25k → sale 20k → discount = 5k
   TỔNG DISCOUNT = 15k ≠ 10k ❌ (lệch 5,000đ!)
```

**Nguyên nhân:** Khi seller gán SP vào deal FIXED, họ nhập salePrice thủ công cho từng SP. Hệ thống **không validate** tổng discount có khớp với `discountValue` của deal không.

**Fix:** Thêm validation trong `DealService.addProductToDeal()`: khi deal là FIXED, kiểm tra tổng (originalPrice - salePrice) của tất cả SP trong deal ≤ discountValue.

### 2.3. MODERATE: SCHEDULED Deal Đã Qua StartTime Nhưng Không Tự ACTIVE

```
D4 (Sale Hè 2026):  startTime = 28/5, hôm nay = 31/5 → ĐÃ QUA 3 NGÀY, vẫn SCHEDULED ❌
D7 (Combo Bánh Trà): startTime = 28/5, hôm nay = 31/5 → ĐÃ QUA 3 NGÀY, vẫn SCHEDULED ❌
```

**Nguyên nhân:** Không có cơ chế tự động chuyển SCHEDULED → ACTIVE khi đến startTime. Admin/Seller phải tự tay kích hoạt.

**Fix:** Thêm scheduled task (mỗi 5 phút) auto-activate SCHEDULED deals khi startTime ≤ now.

### 2.4. MODERATE: Platform Deals Chồng Lấn Danh Mục

```
Category "Món mặn" (cat_savory):
├─ D2  (Voucher 20K, FIXED, ACTIVE)
├─ D6  (Voucher 15%, PERCENT, ACTIVE)
├─ D10 (Voucher 50K, FIXED, ACTIVE)
└─ D11 (Weekend 15%, PERCENT, PAUSED)
   → 3 ACTIVE platform deals cùng category!

Category "Đồ uống" (cat_drinks):
├─ D2, D4 (SCHEDULED), D6, D10, D11
   → 3 ACTIVE + 1 SCHEDULED
```

Nếu D4 và D8 được kích hoạt, chồng lấn còn nghiêm trọng hơn. Tuy nhiên các deal này đều là **CODE_REQUIRED** (voucher), không phải AUTO_APPLY, nên không tự động cộng dồn. Voucher do buyer chọn thủ công, nên không gây xung đột.

Nhưng nếu có AUTO_APPLY platform deal trùng danh mục, cumulative discount có thể vượt 30% cap.

**Fix:** Khi admin tạo platform deal AUTO_APPLY, hiển thị cảnh báo nếu danh mục đã có ACTIVE platform deal khác. Tính cumulative discount và hiển thị cho admin biết.

### 2.5. MINOR: Thiếu Validation Khi Tạo Deal

```
D1 (Flash Sale 40%): CODE_REQUIRED cho Flash Sale? Flash Sale thường là AUTO_APPLY
D12 (Flash Sale 35%): CODE_REQUIRED cho Flash Sale? 
```

Flash Sale với `CODE_REQUIRED` là bất thường — Flash Sale thường là `AUTO_APPLY` để buyer thấy giá sale ngay. Việc bắt nhập mã cho Flash Sale làm mất tính khẩn cấp.

**Fix:** Nên tự động set Flash Sale luôn là AUTO_APPLY (hoặc ít nhất cảnh báo khi admin/seller chọn CODE_REQUIRED cho Flash Sale).

### 2.6. MINOR: Usage Count Không Được Validate Khi Checkout

```
D1: maxUsageCount=100, usageCount=35 → còn 65 lượt
D5: maxUsageCount=80,  usageCount=78 → còn 2 lượt (đã ENDED)
D6: maxUsageCount=1000, usageCount=320 → còn 680 lượt
D9: maxUsageCount=60,  usageCount=60 → HẾT LƯỢT (đã ENDED) ✅
```

Usage count được ghi nhận nhưng **không thấy code tăng usageCount khi checkout**. Nếu không tăng, `maxUsageCount` và `usagePerUser` không có tác dụng.

**Fix:** Trong `POST /buyer/checkout/confirm`, sau khi tạo Order, tăng `usageCount` cho mỗi deal được áp dụng. Kiểm tra `usagePerUser` để giới hạn số lần mỗi user dùng deal.

---

## 3. Đề Xuất Sửa Đổi — Priorities

### Priority 1: Fix Ngay (Bug Ảnh Hưởng Trực Tiếp)

**A. Auto-end expired deals ngay khi query**
File: `DealRepository.java`, thêm method:
```java
// Gọi trước mỗi query ACTIVE deals để cleanup
@Modifying
@Query("UPDATE Deal d SET d.status='ENDED' WHERE d.status='ACTIVE' AND d.endTime < CURRENT_TIMESTAMP")
int endExpiredDealsNow();
```
Gọi method này ở đầu `HomeController.home()` và `CartService.getCartData()` để đảm bảo không hiển thị deal hết hạn.

**B. Validate FIXED deal pricing**
File: `DealService.java`, thêm method:
```java
private void validateFixedDealPricing(Deal deal, Product product, double salePrice, double originalPrice) {
    if (!"FIXED".equals(deal.getDiscountType())) return;
    double newDiscount = originalPrice - salePrice;
    double discountValue = deal.getDiscountValue() != null ? deal.getDiscountValue() : 0;
    // Tổng discount hiện tại của deal
    List<DealProduct> existing = dealProductRepository.findByDeal(deal);
    double existingDiscount = 0;
    for (DealProduct dp : existing) {
        if (dp.getProduct() != null && !dp.getProduct().getProductId().equals(product.getProductId())) {
            existingDiscount += (dp.getOriginalPrice() - dp.getSalePrice());
        }
    }
    if (existingDiscount + newDiscount > discountValue) {
        throw new IllegalArgumentException(String.format(
            "Tổng mức giảm (%,.0fđ) vượt quá giá trị giảm của deal (%,.0fđ). Vui lòng điều chỉnh giá sale.",
            existingDiscount + newDiscount, discountValue));
    }
}
```

**C. Auto-activate SCHEDULED deals**
File: `DealRepository.java`, thêm query:
```java
@Modifying
@Query("UPDATE Deal d SET d.status='ACTIVE' WHERE d.status='SCHEDULED' AND d.startTime <= CURRENT_TIMESTAMP")
int activateScheduledDeals();
```
File: `ExpiryCountdownService.java`, thêm scheduled method gọi query này mỗi 5 phút.

### Priority 2: Bảo Vệ Buyer & Seller

**D. Increment usageCount khi checkout**
File: `HomeController.java`, trong `confirmCheckout()`, sau khi tạo Order:
```java
// Tăng usageCount cho mỗi deal được dùng
Set<Long> usedDealIds = new HashSet<>();
for (Map<String, Object> item : cart) {
    Long dealId = item.get("dealId") != null ? ((Number) item.get("dealId")).longValue() : 0;
    if (dealId > 0 && !usedDealIds.contains(dealId)) {
        dealRepository.incrementUsageCount(dealId);
        usedDealIds.add(dealId);
    }
}
```

**E. Kiểm tra usagePerUser khi checkout**
```java
// Trước khi tạo Order, kiểm tra user đã dùng deal bao nhiêu lần
for (Long dealId : usedDealIds) {
    Deal deal = dealRepository.findById(dealId).orElse(null);
    if (deal != null && deal.getUsagePerUser() != null) {
        long userUsage = orderRepository.countByUserAndDealCode(user.getUserId(), deal.getDealCode());
        if (userUsage >= deal.getUsagePerUser()) {
            return "redirect:/buyer/cart?error=usage_limit";
        }
    }
}
```

**F. Cảnh báo platform deal overlap khi admin tạo**
File: `AdminController.java`, khi tạo platform deal mới:
```java
// Kiểm tra danh mục đã có ACTIVE platform deal chưa
List<DealCategory> overlapping = dealCategoryRepository.findActiveByCategory(category);
if (!overlapping.isEmpty()) {
    model.addAttribute("warning", "Danh mục này đã có " + overlapping.size() + " deal sàn đang ACTIVE");
}
```

### Priority 3: Cải Thiện UX

**G. Flash Sale luôn AUTO_APPLY**
File: `DealService.java`, trong `validateDeal()`:
```java
if ("FLASH_SALE".equals(deal.getDealType()) && !"AUTO_APPLY".equals(deal.getApplyMethod())) {
    throw new IllegalArgumentException("Flash Sale phải là Tự động áp dụng (AUTO_APPLY)");
}
```

**H. Hiển thị rõ FIXED vs PERCENT trong cart**
Đã làm: cart summary hiển thị "-20,000đ (50%)" cho mỗi item.

**I. Validate startTime không được trong quá khứ**
```java
if (deal.getStartTime() != null && deal.getStartTime().isBefore(LocalDateTime.now())) {
    throw new IllegalArgumentException("Thời gian bắt đầu phải trong tương lai");
}
```

---

## 4. Tổng Kết: Nếu Là Tôi, Tôi Sẽ Làm Gì

### Ngay lập tức (hôm nay):
1. ✅ Fix auto-end expired deals (Priority 1.A) — đảm bảo deal hết hạn không hiển thị
2. ✅ Fix FIXED deal pricing validation (Priority 1.B) — chặn seller set sai giá
3. ✅ Auto-activate SCHEDULED deals (Priority 1.C) — deal tự động kích hoạt

### Tuần này:
4. Increment usageCount + check usagePerUser (Priority 2.D, 2.E)
5. Cảnh báo platform deal overlap (Priority 2.F)
6. Flash Sale luôn AUTO_APPLY (Priority 2.G)

### Tuần sau:
7. Validate startTime không quá khứ
8. Clean up data: sửa D1, D3 thành ENDED; sửa D4, D7 thành ACTIVE (đã qua startTime)
9. Thêm audit log cho thay đổi deal (ai pause/resume/edit, lúc nào)
