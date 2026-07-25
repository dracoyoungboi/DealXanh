# Hệ thống Deal — DealXanh

## 1. Tổng quan kiến trúc

DealXanh có **2 tầng deal** hoạt động song song:

```
                    ┌─────────────────────────────────┐
                    │          DEAL SYSTEM            │
                    └─────────────────────────────────┘
                                   │
              ┌────────────────────┴────────────────────┐
              ▼                                         ▼
    ┌──────────────────┐                      ┌──────────────────┐
    │  DEAL SÀN (Platform)│                    │ DEAL STORE        │
    │  Admin tạo          │                    │ Seller tạo        │
    │  Gán vào DANH MỤC   │                    │ Gán vào SẢN PHẨM  │
    │  scope=ALL_STORES   │                    │ scope=SPECIFIC    │
    │  store = NULL       │                    │ store = {storeId} │
    └────────┬───────────┘                      └────────┬─────────┘
             │                                           │
             ▼                                           ▼
    ┌──────────────────┐                      ┌──────────────────┐
    │   DealCategory    │                      │   DealProduct    │
    │   deal → category │                      │   deal → product │
    │   TẤT CẢ SP trong │                      │   CHỈ SP được    │
    │   category đều    │                      │   chọn mới có    │
    │   được áp dụng    │                      │   giá deal       │
    └──────────────────┘                      └──────────────────┘
```

## 2. Entity Relationships

```
Deal (1) ─────────── (N) DealCategory ────────── (1) Category
  │                                                   │
  │                                                   │ (1)
  │                                                   ▼
  │                                               Product
  │                                                   ▲
  │                                                   │
Deal (1) ─────────── (N) DealProduct  ────────── (1) Product
```

Mỗi Deal có 2 cách áp dụng:
- **AUTO_APPLY**: giảm giá tự động, SP hiển thị giá sale ngay
- **CODE_REQUIRED**: cần nhập mã, giảm trên tổng đơn (Voucher)

## 3. Luồng hoạt động đầy đủ

### 3.1. Tạo Deal (Admin/Seller)

```
┌─────────────────────────────────────────────────────────────────┐
│ BƯỚC 1: FORM TẠO DEAL                                           │
│                                                                 │
│ ┌─────────────┐  ┌──────────────┐  ┌─────────────┐             │
│ │ Loại Deal   │  │ Giảm giá     │  │ Thời gian    │             │
│ │ FLASH_SALE  │  │ PERCENT 1-85%│  │ Start→End    │             │
│ │ VOUCHER     │  │ FIXED 1k-500k│  │              │             │
│ │ COMBO       │  │              │  │ Max duration │             │
│ │ SEASONAL    │  │              │  │ theo loại    │             │
│ └─────────────┘  └──────────────┘  └─────────────┘             │
│                                                                 │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ VALIDATE                                                     │ │
│ │ ├─ Mã deal: [A-Z0-9_-]+, unique                             │ │
│ │ ├─ Discount PERCENT: 1% → 85%                               │ │
│ │ ├─ Discount FIXED: 1,000đ → 500,000đ                        │ │
│ │ ├─ FIXED vs avg giá SP store: max 50%/70%/80%               │ │
│ │ ├─ Duration: FlashSale≤6h, Voucher≤30d, Combo≤15d,          │ │
│ │ │            Seasonal≤30d, tổng ≤30d                         │ │
│ │ ├─ Không trùng thời gian với deal khác cùng store           │ │
│ │ └─ Store phải có ≥1 SP APPROVED                              │ │
│ └─────────────────────────────────────────────────────────────┘ │
│ → Trạng thái: SCHEDULED                                          │
│ → Chưa hiển thị cho buyer (cần ACTIVE)                          │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2. Gán Danh Mục / Sản Phẩm

```
┌──────────────────────────────────────────────────────────────┐
│ BƯỚC 2: GÁN ĐỐI TƯỢNG ÁP DỤNG                                │
│                                                              │
│ DEAL SÀN (Admin):                                            │
│ ├─ Chọn categories → tạo DealCategory                        │
│ ├─ Validate platform discount ≤30% trên mỗi SP               │
│ │  (tổng tất cả deal sàn trên 1 category không vượt 30%)     │
│ └─ Cảnh báo khi ≥28%                                         │
│                                                              │
│ DEAL STORE (Seller):                                         │
│ ├─ Chọn products → tạo DealProduct                           │
│ ├─ Set salePrice cho từng SP                                 │
│ ├─ Validate: salePrice ≥ 20% × originalPrice                 │
│ ├─ Validate: SP không nằm trong deal khác trùng thời gian    │
│ └─ Validate: tổng discount (sàn + store) ≤ 65%/70%/80%       │
│                                                              │
│ CAPS:                                                        │
│ ├─ Hàng thường: 65%                                          │
│ ├─ Flash Sale:  70%                                          │
│ └─ Hàng cận date (≤3 ngày): 80%                              │
└──────────────────────────────────────────────────────────────┘
```

### 3.3. Hiển Thị Cho Buyer

```
┌───────────────────────────────────────────────────────────────┐
│ HIỂN THỊ SẢN PHẨM CHO BUYER                                   │
│                                                               │
│ Home Page:                                                    │
│ ├─ Query tất cả ACTIVE deals (trong time window)              │
│ ├─ Duyệt DealProduct → build card với giá deal                │
│ ├─ Sort theo khoảng cách (gần nhất → xa nhất)                 │
│ ├─ Tách riêng: Flash Sale section, Combo section              │
│ └─ PHẢI có DealProduct link thì SP mới hiện trên home         │
│                                                               │
│ Category Page:                                                │
│ ├─ Query SP theo category                                     │
│ ├─ Build dealProductMap: map productId → giá deal             │
│ ├─ Hiển thị giá deal nếu có, không thì giá thường             │
│ └─ Filter giá: <50k / 50-100k / 100-200k / >200k              │
│                                                               │
│ Search:                                                       │
│ ├─ Search SP + build dealProductMap                           │
│ ├─ Filter theo loại deal (Flash Sale/Voucher/Combo/Seasonal)  │
│ └─ Sort: Mặc định / Giá↑ / Giá↓ / Tên A-Z                    │
└───────────────────────────────────────────────────────────────┘
```

### 3.4. Trong Giỏ Hàng

```
┌────────────────────────────────────────────────────────────┐
│ CART — CartService.getCartData()                            │
│                                                            │
│ Với mỗi CartItem:                                          │
│                                                            │
│ 1. Check product.isAvailable() + approvalStatus=APPROVED   │
│    → Nếu false: đánh dấu unavailable, grey out             │
│                                                            │
│ 2. Tìm deal store (DealProduct):                            │
│    → Query DealProduct.findByProduct(product)              │
│    → Lọc deal ACTIVE + trong time window                   │
│    → FIXED: salePrice = originalPrice (giữ nguyên)         │
│    → PERCENT: salePrice = dp.salePrice                     │
│                                                            │
│ 3. Nếu không có deal store, tìm deal sàn (DealCategory):    │
│    → Query DealCategory.findActiveByCategory(category)     │
│    → Lọc deal ACTIVE + trong time window                   │
│    → PERCENT: salePrice = orig × (1 - %/100)               │
│    → FIXED: salePrice = originalPrice (giữ nguyên)         │
│                                                            │
│ 4. Không có deal nào: salePrice = product.currentPrice     │
│                                                            │
│ Kết quả mỗi item có: salePrice, dealId, dealType,          │
│    dealName, discountType, discountValue                   │
└────────────────────────────────────────────────────────────┘
```

### 3.5. Checkout — Tính Discount

```
┌──────────────────────────────────────────────────────────────┐
│ CHECKOUT — HomeController                                    │
│                                                              │
│ Group cart items theo store → mỗi store 1 Order riêng        │
│                                                              │
│ Cách tính discount:                                           │
│                                                              │
│ ┌──────────────────────────────────────────────────────────┐ │
│ │ PERCENT DEAL (hoặc store deal PERCENT):                  │ │
│ │   discount = Σ (originalPrice - salePrice) × quantity    │ │
│ │   → Tính TRÊN TỪNG SẢN PHẨM                             │ │
│ │                                                          │ │
│ │ FIXED DEAL (sàn hoặc store):                             │ │
│ │   Gom unique dealId → cộng discountValue ĐÚNG 1 LẦN     │ │
│ │   → KHÔNG nhân theo số lượng SP                          │ │
│ │   → Nếu 2 SP khác store cùng FIXED deal: mỗi store       │ │
│ │     được giảm 1 lần (vì mỗi store 1 Order)              │ │
│ │                                                          │ │
│ │ VOUCHER (CODE_REQUIRED):                                  │ │
│ │   → Áp dụng 1 lần cho Order đầu tiên                     │ │
│ │   → PERCENT: cartTotal × % (có maxDiscountAmount cap)    │ │
│ │   → FIXED: trừ thẳng discountValue                       │ │
│ └──────────────────────────────────────────────────────────┘ │
│                                                              │
│ finalAmount = totalAmount - discountAmount - voucherDiscount  │
└──────────────────────────────────────────────────────────────┘
```

## 4. Trạng Thái Deal & Luồng Chuyển Đổi

```
                    ┌──────────┐
                    │  TẠO MỚI │
                    └────┬─────┘
                         │ (luôn)
                         ▼
                   ┌──────────┐
                   │SCHEDULED │ ← Chưa hiển thị
                   └────┬─────┘
                        │
            ┌───────────┼───────────┐
            │ (manual)  │           │
            ▼           ▼           ▼
      ┌─────────┐ ┌─────────┐ ┌──────────┐
      │ ACTIVE  │ │CANCELLED│ │   ENDED  │
      └────┬────┘ └─────────┘ └──────────┘
           │                         ▲
      ┌────┴────┐                    │
      ▼         ▼                    │
┌─────────┐ ┌────────┐              │
│ PAUSED  │ │ ENDED  │──────────────┘
└────┬────┘ │(auto:  │ (auto mỗi 30ph
     │      │ endTime │  khi endTime
     │      │ passed) │  đã qua)
     ▼      └────────┘
  ┌────────┐
  │ ACTIVE │ (resume, nếu còn trong time window)
  └────────┘
```

**Auto-transitions:**
- `ACTIVE → ENDED`: `ExpiryCountdownService.endExpiredDeals()` chạy mỗi 30 phút, UPDATE trực tiếp tất cả ACTIVE deal có `endTime < now`

**Manual transitions:**
- `ACTIVE → PAUSED`: Admin/Seller pause. Gửi notification cho buyer có SP trong giỏ.
- `PAUSED → ACTIVE`: Resume. Validate `endTime` chưa qua.
- `SCHEDULED → ACTIVE`: Phải active thủ công (không tự động).
- Any → `CANCELLED`: Soft delete.

## 5. Tất Cả Validation Rules

### 5.1. Khi Tạo Deal (DealService.createDeal)

| # | Rule | Chi tiết | Code |
|---|------|----------|------|
| 1 | Cấm FREESHIP | Đã deprecated | Line 472 |
| 2 | Format mã deal | `^[A-Z0-9_-]+$` | Line 518 |
| 3 | Mã deal unique | Không trùng với deal khác (trừ CANCELLED) | Line 522 |
| 4 | PERCENT range | 1% → 85% | Lines 688-698 |
| 5 | FIXED range | 1,000đ → 500,000đ | Lines 701-711 |
| 6 | FIXED vs avg giá SP store | ≤50% nếu avg<50k, ≤70% nếu 50k-200k, ≤80% nếu >200k | Lines 528-561 |
| 7 | Flash Sale duration | ≤6 giờ | Line 569 |
| 8 | Voucher duration | ≤30 ngày | Line 586 |
| 9 | Combo duration | ≤15 ngày | Line 591 |
| 10 | Seasonal duration | ≤30 ngày | Line 605 |
| 11 | Tổng duration | ≤30 ngày | Lines 718-723 |
| 12 | Time overlap | Không trùng SCHEDULED/ACTIVE deal cùng store | Lines 622-650 |
| 13 | Seller eligibility | Store có ≥1 SP APPROVED | Lines 658-665 |
| 14 | Min order amount | ≥0 | Line 504 |
| 15 | Banner type | JPG/PNG/WEBP | Multiple |
| 16 | Banner size | ≤5MB | Multiple |

### 5.2. Khi Gán SP Vào Deal (DealService.addProductToDeal)

| # | Rule | Chi tiết | Code |
|---|------|----------|------|
| 1 | Giá gốc > 0 | originalPrice > 0 | Line 853 |
| 2 | Giá sale sàn | salePrice ≥ 20% × originalPrice | Lines 856-861 |
| 3 | Giá sale < giá gốc | salePrice < originalPrice | Line 862 |
| 4 | Max quantity > 0 | Nếu được set | Line 365 |
| 5 | SP không overlap | Không nằm trong deal khác trùng thời gian | Lines 726-759 |
| 6 | Cumulative discount | Tổng (platform + store) ≤ 65%/70%/80% | Lines 762-803 |

### 5.3. Khi Gán Danh Mục Vào Deal Sàn (DealService.addCategoryToDeal)

| # | Rule | Chi tiết | Code |
|---|------|----------|------|
| 1 | Platform cap | Tổng discount từ tất cả deal sàn trên 1 SP ≤ 30% | Lines 210-224 |
| 2 | Warning threshold | Cảnh báo khi ≥28% | Lines 320-323 |

### 5.4. Cumulative Discount Caps

| Loại SP | Cap | Code |
|----------|-----|------|
| Hàng thường | 65% | `MAX_COMBINED_DISCOUNT_NORMAL` |
| Flash Sale | 70% | `MAX_COMBINED_DISCOUNT_FLASH_SALE` |
| Hàng cận date (≤3 ngày) | 80% | `MAX_COMBINED_DISCOUNT_NEAR_EXPIRY` |
| Platform-only (sàn) | 30% | `MAX_PLATFORM_DISCOUNT` |

### 5.5. Khi Pause/Resume Deal

| # | Rule | Code |
|---|------|------|
| 1 | Chỉ pause được ACTIVE | AdminController:2120, SellerController:633 |
| 2 | Chỉ resume được PAUSED | AdminController:2184, SellerController:700 |
| 3 | Resume phải trong time window | AdminController:2185, SellerController:703 |
| 4 | Notify buyer có SP trong giỏ | AdminController:2136, SellerController:654 |

## 6. Ảnh Hưởng Của Deal Đến Hệ Thống

### 6.1. Khi Deal ACTIVE

| Thành phần | Ảnh hưởng |
|---|---|
| **Home page** | SP trong deal xuất hiện ở section tương ứng (Flash Sale/Combo/Deal Nổi Bật). Sort theo khoảng cách. |
| **Category page** | SP hiển thị giá deal (salePrice), có badge % giảm, loại deal. |
| **Search** | Filter được theo loại deal. Sort theo giá deal. |
| **Product detail** | Hiển thị giá deal + % giảm + thông tin deal. |
| **Cart** | Giá sale được tính từ deal (PERCENT: giảm theo SP, FIXED: giữ giá gốc). |
| **Checkout** | PERCENT: discount per-item. FIXED: 1 lần/unique deal. Voucher: 1 lần/đơn đầu. |

### 6.2. Khi Deal PAUSED

| Thành phần | Ảnh hưởng |
|---|---|
| **Home/Category/Search** | SP không còn hiển thị với giá deal (deal không còn ACTIVE). |
| **Cart** | `getCartData()` không tìm thấy ACTIVE deal → giá rơi về `currentPrice`. Buyer được thông báo. |
| **Đơn đã đặt** | Giá đã snapshot trong OrderItem, không bị ảnh hưởng. |

### 6.3. Khi Deal ENDED (hết hạn tự động)

| Thành phần | Ảnh hưởng |
|---|---|
| **Tất cả** | Giống hệt PAUSED. Deal không còn hiển thị. |
| **Auto** | `ExpiryCountdownService` chạy mỗi 30ph, UPDATE tất cả ACTIVE deal quá endTime → ENDED. |

### 6.4. Khi Deal CANCELLED

| Thành phần | Ảnh hưởng |
|---|---|
| **Tất cả** | Giống PAUSED. Deal biến mất khỏi tất cả view. |
| **DB** | Soft delete: status=CANCELLED, không xóa record. |

## 7. Validation Khi Tạo Deal — Chi Tiết

### 7.1. FIXED Discount vs Giá Trung Bình SP Store

```
Nếu avg giá SP của store:
├─ < 50,000đ:      max FIXED discount = 50% × avg  (vd: avg=30k → max=15k)
├─ 50k → 200k:     max FIXED discount = 70% × avg  (vd: avg=100k → max=70k)
└─ > 200,000đ:     max FIXED discount = 80% × avg  (vd: avg=300k → max=240k)

Nhưng luôn bị chặn cứng bởi MAX_FIXED_DISCOUNT = 500,000đ
```

### 7.2. PERCENT Discount

```
Đơn giản: 1% → 85%
Có maxDiscountAmount để chặn trần tiền giảm cho SP đắt
Ví dụ: giảm 20%, max 50,000đ → SP 500k chỉ giảm 50k (không phải 100k)
```

### 7.3. Sale Price Floor (Sàn Giá Sale)

```
salePrice ≥ originalPrice × 20%
⇔ Giảm tối đa 80% giá gốc

Ví dụ: SP giá 100k → giá sale tối thiểu 20k
       SP giá 50k  → giá sale tối thiểu 10k
```

## 8. Phân Biệt PERCENT vs FIXED Trong Toàn Bộ Pipeline

| Giai đoạn | PERCENT | FIXED |
|---|---|---|
| **Cart** (CartService) | salePrice = giá đã giảm % | salePrice = originalPrice (giữ nguyên) |
| **Checkout display** | discount = Σ(orig-sale)×qty mỗi SP | discount = Σ unique deal discountValue 1 lần |
| **Checkout confirm** | discount per-item | discount 1 lần/unique dealId/store |
| **OrderItem.unitPrice** | salePrice (đã giảm) | originalPrice (chưa giảm, discount ở order level) |
| **Hiển thị cart summary** | "-Xđ (Y%)" per item | "-Xđ (Y%)" per item (từ unique deal) |

## 9. Concurrency & Locking

```
Tất cả thao tác ghi đều qua ResourceLockManager (FIFO ReentrantLock):

┌──────────────────┬─────────────────────┐
│ Thao tác          │ Lock Key            │
├──────────────────┼─────────────────────┤
│ Tạo deal          │ "STORE:{storeId}"   │
│ Sửa/Xóa deal      │ "DEAL:{dealId}"     │
│ Gán SP vào deal   │ "PRODUCT:{prodId}"  │
│ Pause/Resume      │ "DEAL:{dealId}"     │
│ Checkout           │ "CART:{userId}"     │
│ Cancel Order       │ "ORDER:{orderId}"   │
│ PayOS confirm      │ "ORDER:{orderId}"   │
└──────────────────┴─────────────────────┘
```

## 10. File Upload — Banner Deal

```
Admin: timestamp + filename → uploads/{timestamp}_{filename}
Seller: MD5 hash → uploads/{md5hash}.{ext}
Cả 2: validate JPG/PNG/WEBP, max 5MB
```

## 11. File Index

| File | Vai trò |
|---|---|
| `entity/Deal.java` | Entity chính, 30+ fields, helper methods |
| `entity/DealProduct.java` | Liên kết deal↔product, giá sale |
| `entity/DealCategory.java` | Liên kết deal↔category |
| `repository/DealRepository.java` | 15+ queries, bao gồm `endExpiredActiveDeals()` |
| `repository/DealProductRepository.java` | 8 queries |
| `repository/DealCategoryRepository.java` | 5 queries |
| `service/DealService.java` | Toàn bộ business logic + validation (~870 dòng) |
| `service/CartService.java` | Tính giá deal trong giỏ hàng |
| `service/ExpiryCountdownService.java` | Auto-end deal hết hạn (mỗi 30ph) |
| `controller/admin/AdminController.java` | CRUD deal + gán category/product (admin) |
| `controller/SellerController.java` | CRUD deal + gán product (seller) |
| `controller/HomeController.java` | Hiển thị deal cho buyer + checkout discount |
