# DEALXANH — BRD/FSD (Tài Liệu Đặc Tả Nghiệp Vụ)

**Version:** 1.0.0-beta | **Date:** 2026-05-28 | **Status:** ~92% hoàn thành
**Tech Stack:** Java 17 · Spring Boot 3.2.3 · Thymeleaf · MySQL · Flyway · Leaflet.js · PayOS

---

## Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [Domain Model](#2-domain-model)
3. [Phân Quyền & RBAC](#3-phân-quyền)
4. [State Machines](#4-state-machines)
5. [Luồng Nghiệp Vụ Admin](#5-admin)
6. [Luồng Nghiệp Vụ Moderator](#6-moderator)
7. [Luồng Nghiệp Vụ Seller](#7-seller)
8. [Luồng Nghiệp Vụ Staff](#8-staff)
9. [Luồng Nghiệp Vụ Buyer](#9-buyer)
10. [Luồng Auth & Onboarding](#10-auth)
11. [Hệ Thống Thanh Toán PayOS](#11-payos)
12. [Hệ Thống Deal — Rules & Validate](#12-deal-rules)
13. [Hệ Thống Lọc](#13-filters)
14. [Hệ Thống Thông Báo](#14-notifications)
15. [Lịch Jobs & Cron](#15-jobs)
16. [Concurrency Control](#16-concurrency)
17. [Database & Migrations](#17-database)
18. [File Upload & Storage](#18-uploads)
19. [Security Config](#19-security)
20. [Toàn Bộ API Index](#20-api-index)
21. [Toàn Bộ Trang / Site Map](#21-site-map)
22. [Checklist Chuẩn Bị Release](#22-checklist)

---

## 1. Tổng Quan

**DealXanh là nền tảng O2O (Online-to-Offline) Deal Marketplace**, KHÔNG phải e-commerce truyền thống.

### 1.1 Business Model

```
E-commerce:       Product → Price → Buy
DealXanh O2O:     Deal → DealProduct/DealCategory → Product/Category → Buy at deal price → Pickup at store
```

Người bán tạo **Deal** (chương trình giảm giá) gán vào sản phẩm hoặc danh mục. Người mua thấy deal, đặt hàng online, đến cửa hàng pickup.

### 1.2 Số Liệu Dự Án

| Thành phần | Số lượng |
|-----------|---------|
| Controllers | 11 |
| Services | 9 |
| Entities | 17 |
| Repositories | 17 (80+ custom queries) |
| Templates | 87 HTML files |
| Flyway Migrations | V1 → V9 |
| Trạng thái | ~92% |

### 1.3 Cấu Hình Chính (application.properties)

| Setting | Value |
|---------|-------|
| Port | 8084 |
| DB | MySQL `dealxanh_db` @ localhost:3306 |
| Hibernate DDL | `update` |
| Session | JDBC-backed, timeout 30 phút |
| File upload | Max 10MB |
| Gmail SMTP | `nguyenxuanphananh@gmail.com` |
| Google OAuth2 | Client ID đã cấu hình |
| PayOS | Production URL `api-merchant.payos.vn` |
| Logging | DEBUG cho `com.dealxanh`, INFO cho security |

---

## 2. Domain Model

### 2.1 Entity Relationship Diagram (Tóm tắt)

```
User (1) ──< Order >── (1) Store
User (1) ──< Review >── (1) Product
User (1) ──< Notification
User (1) ──< UserAddress
User (1) ─── Cart (1-1)
Cart  (1) ──< CartItem >── (1) Product
Store (1) ──< Product >── (1) Category
Store (1) ──< Deal
Deal  (1) ──< DealProduct >── (1) Product
Deal  (1) ──< DealCategory >── (1) Category
Order (1) ──< OrderItem >── (1) Product
Order (1) ──< Transaction
Store (1) ──< Transaction
Order (1) ──< Dispute >── (1) User
```

### 2.2 Entity Fields (Rút Gọn)

#### User
`userId, username, email, password(BCrypt), fullName, phone, address, avatarUrl, active, weakPassword, provider(local/google), providerId, resetToken, resetTokenExpiry, createdAt, updatedAt` + FK `role_id`, `work_store_id`

#### Store
`storeId, storeName, description, address, phone, logoUrl, coverImageUrl, latitude, longitude, openTime, closeTime, averageRating, totalReviews, status(PENDING/ACTIVE/SUSPENDED/REJECTED), businessType, categories, city, district, cccdUrl, businessLicenseUrl, vsattpUrl, bankName, bankAccountNumber, bankAccountOwner, operatingDays, pickupSlots, maxSlotsPerTime, pickupDurationMinutes, approvalMode, partnerTier(BRONZE/SILVER/GOLD/DIAMOND), storePageConfig(JSON TEXT)` + FK `owner_id, approved_by, rejected_by`

- `getCommissionRate()`: BRONZE 10% · SILVER 8% · GOLD 6% · DIAMOND 4%
- `getPartnerTierLabel()`: "Đồng" / "Bạc" / "Vàng" / "Kim Cương"

#### Product
`productId, name, description(TEXT), imageUrl, originalPrice, dealPrice, currentPrice, stockQuantity, productType(SPECIFIC_DEAL/COMBO), expiryDate, dealStartTime, dealEndTime, pickupDeadline, version(@Version), active, deleted(soft delete), approvalStatus(PENDING/APPROVED/REJECTED), rejectionReason(TEXT), createdAt, updatedAt` + FK `store_id, category_id, created_by`

- `isAvailable()`: `!deleted && active && "APPROVED".equals(approvalStatus) && stockQuantity > 0 && dealStartTime <= now && dealEndTime >= now`

#### Deal
`dealId, dealName, dealCode, description, dealType(FLASH_SALE/VOUCHER/COMBO/SEASONAL), discountType(PERCENT/FIXED), discountValue, maxDiscountAmount, minOrderAmount, maxUsageCount, usageCount, usagePerUser, startTime, endTime, status(SCHEDULED/ACTIVE/PAUSED/ENDED/CANCELLED), scope(ALL_STORES/SPECIFIC_STORES/SPECIFIC_PRODUCTS), applyMethod(CODE_REQUIRED/AUTO_APPLY), imageUrl, bannerUrl, version(@Version), priority, createdAt, updatedAt` + FK `created_by, store_id`

#### Order
`orderId, totalAmount, discountAmount, couponCode, finalAmount, status, paymentStatus(UNPAID/PAID), paymentMethod, scheduledPickupTime, actualPickupTime, pickupQrCode, version(@Version), note, cancellationReason, createdAt, updatedAt` + FK `user_id, store_id`

#### OrderItem
`orderItemId, quantity, unitPrice` + FK `order_id, product_id` · `getSubtotal()` = quantity × unitPrice

#### Transaction
`transactionId, type(Sale/Payout/Refund/Fee), amount, platformFee, netAmount, status(COMPLETED/PENDING/FAILED), transactionRef, paymentMethod, description, createdAt, updatedAt` + FK `store_id, order_id`

#### Cart / CartItem
- **Cart**: `cartId, user(1-1), createdAt, updatedAt`
- **CartItem**: `cartItemId, cart, product, quantity, unitPrice`

#### UserAddress
`id, name, phone, address, city, district, ward, isDefault` + FK `user_id` · `getFullAddress()` → "Số X, Phường Y, Quận Z, TP W"

#### Các Entity Khác
- **Category**: `categoryId, name, iconUrl, description`
- **DealProduct**: `deal, product, originalPrice, salePrice, maxQuantity, soldQuantity, priority`
- **DealCategory**: `deal, category, priority, active`
- **Review**: `rating(1-5), comment(TEXT), imageUrl, verified` + FK `user, product, store, order`
- **Notification**: `title, message(TEXT), type, linkUrl, isRead, createdAt` + FK `user`
- **Dispute**: `reason, description(TEXT), evidenceUrl, status, adminNote` + FK `order, complainant`
- **Role**: `name(USER/STORE_OWNER/STORE_STAFF/MODERATOR/ADMIN)`

---

## 3. Phân Quyền & RBAC

| Role | Truy Cập |
|------|---------|
| **ADMIN** | `/admin/**` (toàn bộ) |
| **MODERATOR** | `/admin/**` (giới hạn: dashboard, seller-verify, không có finance/payout/delete) |
| **STORE_OWNER** | `/seller/**`, `/store/dashboard/**` |
| **STORE_STAFF** | `/staff/**` |
| **USER** | `/`, `/buyer/**`, `/profile`, `/cart/**`, `/deals/**`, `/store/**` (public pages) |

**Ma Trận Quyền Chi Tiết:**

| Chức năng | ADMIN | MOD | OWNER | STAFF | USER |
|-----------|-------|-----|-------|-------|------|
| Dashboard + Analytics | ✅ | ✅ | ✅ | ✅ | ❌ |
| Quản lý User (toggle/edit/view) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Xét duyệt Store (approve/reject/reset) | ✅ | ✅ | ❌ | ❌ | ❌ |
| Tạo Deal Platform | ✅ | ❌ | ❌ | ❌ | ❌ |
| Gán Category vào Deal | ✅(ADMIN only) | ❌ | ❌ | ❌ | ❌ |
| Tạo Deal Store | ❌ | ❌ | ✅ | ❌ | ❌ |
| Gán Product vào Deal | ✅ | ❌ | ✅ | ❌ | ❌ |
| Pause/Resume Deal | ✅ | ❌ | ✅ | ❌ | ❌ |
| CRUD Product | ✅ | ❌(view only) | ✅ | ✅(create) | ❌ |
| Approve/Reject Product | ✅ | ❌ | ❌ | ❌ | ❌ |
| Cập nhật trạng thái Order | ❌ | ❌ | ✅ | ✅ | ❌ |
| QR Pickup Confirm | ❌ | ❌ | ✅ | ✅ | ❌ |
| Finance (payout, reconciliation) | ✅(ADMIN only) | ❌ | ✅(view+request) | ❌ | ❌ |
| Dispute (review/resolve) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Manage Staff | ❌ | ❌ | ✅(OWNER only) | ❌ | ❌ |
| Store Page Builder | ❌ | ❌ | ✅ | ❌ | ❌ |
| Đặt hàng (buyer) | ❌ | ❌ | ❌ | ❌ | ✅ |
| Profile cá nhân | ✅ | ❌ | ✅ | ✅ | ✅ |
| Đổi mật khẩu | ✅ | ❌ | ✅ | ✅ | ✅ |
| Sổ địa chỉ | ❌ | ❌ | ❌ | ❌ | ✅ |

---

## 4. State Machines

### 4.1 Order Status

```
PENDING ──→ CONFIRMED ──→ READY_FOR_PICKUP ──→ COMPLETED
   │                                                   ↑
   └──→ CANCELLED                          (hoặc qua QR scan)
```
- **PENDING → CONFIRMED**: Seller/Staff xác nhận đơn
- **PENDING → CANCELLED**: Seller/Staff hủy đơn
- **CONFIRMED → READY_FOR_PICKUP**: Seller/Staff chuẩn bị xong, tự động tạo QR code `DX-{orderId}-{6 random chars}`
- **READY_FOR_PICKUP → COMPLETED**: 2 cách — Seller/Staff xác nhận, hoặc Buyer scan QR `/pickup/{qrCode}/confirm`
- **Deadlines**: PENDING tự hủy sau 60 phút · CONFIRMED/READY có 2h grace period sau scheduled pickup
- **State validation**: Mọi transition không hợp lệ bị reject với message "Không thể chuyển từ X sang Y"

### 4.2 Deal Status

```
SCHEDULED ──→ ACTIVE ──→ PAUSED ──→ ACTIVE (resume, validate endTime chưa qua)
                 │
                 └──→ ENDED (tự động khi endTime qua)
                 └──→ CANCELLED (admin soft-delete)
```
- Seller pause: ACTIVE → PAUSED, resume: PAUSED → ACTIVE (check `now.isAfter(startTime) && now.isBefore(endTime)`)
- Admin pause: ACTIVE → PAUSED, resume: PAUSED → ACTIVE (check `endTime.isAfter(now)`)

### 4.3 Store Status

```
PENDING ──→ ACTIVE (approve)
       ──→ REJECTED (reject, có lý do)
REJECTED ──→ PENDING (reset-to-pending, gửi email thông báo)
```

### 4.4 Dispute Status

```
PENDING ──→ REVIEWING ──→ RESOLVED_REFUND (approve refund)
                      ──→ RESOLVED_REJECTED (reject)
```

### 4.5 Product Approval

```
PENDING ──→ APPROVED (admin approve, active=true)
       ──→ REJECTED (admin reject + reason, active=false)
APPROVED ──→ PENDING (seller edit → re-submit)
```

---

## 5. Luồng Nghiệp Vụ Admin

### 5.1 Dashboard (`/admin/dashboard`)
- KPIs: GMV hôm nay, đơn hôm nay, commission (10% GMV), total buyers, total sellers, active stores, pending stores
- Chart: GMV + Orders 7 ngày
- Top stores theo rating
- Pending stores gần đây
- AdminDashboardApiController: `GET /admin/api/dashboard/stats` với period=today/yesterday/7d/30d/3m, tính delta % vs kỳ trước

### 5.2 User Management (`/admin/users`)
- Filter: role (buyer/seller/admin) + status (active/inactive) + search (username/email/name/phone)
- **Auto-disable**: Nếu store có ≥40 reviews và averageRating ≤2.5 → tự động `user.active = false`
- Quality label: Kém (rating ≤2.5 + ≥40 review) / Ổn (3.0-4.5) / Tốt (≥4.5)
- Actions: toggle active (không thể tự disable chính mình), edit (fullName, email, phone, address), xem detail

### 5.3 Seller Verification (`/admin/seller-verify`)
- Tabs: Pending / Approved / Rejected (có count)
- Filter: search (tên store, email owner, tên owner), city, sort (oldest/newest)
- Detail panel: thông tin owner + store, xem document (CCCD, license, VSATTP, logo) trong image viewer modal
- **Approve**: PENDING → ACTIVE, ghi nhận `approvedBy` + `reviewedAt`
- **Reject**: PENDING → REJECTED, yêu cầu `rejectionReason`, ghi `rejectedBy`
- **Reset to Pending**: REJECTED → PENDING, xóa rejection tracking, gửi email `sendSellerReReviewEmail`

### 5.4 Deal Management (`/admin/deals`)
- Filter: type (FLASH_SALE/VOUCHER/COMBO/SEASONAL), status, scope (PLATFORM/STORE), search
- **Create Deal**: Validate deal code (unique, `[A-Z0-9_-]+`), discount (1-85% hoặc 1,000-500,000đ), thời gian theo loại, banner (JPG/PNG/WEBP max 5MB)
- **Edit Deal**: Update tất cả fields qua modal, giữ banner cũ nếu không upload mới
- **Pause/Resume**: ACTIVE→PAUSED, PAUSED→ACTIVE (check endTime chưa qua)
- **Assign Categories** (ADMIN only): Gán category vào platform deal → validate cumulative discount ≤30%. Nếu vượt, reject với danh sách SP bị chặn
- **Assign Products**: Gán SP vào store deal → validate product prices, overlap, cumulative discount
- Card hiển thị: deal info + danh mục (cột dọc) + sản phẩm (ngang, max 2 + "+N SP khác")

### 5.5 Product Management (`/admin/products`)
- Tabs: All / Pending / Approved / Rejected (có count)
- Filter: category, status (active/inactive/outofstock), search
- **Approve**: PENDING → APPROVED, active=true
- **Reject**: PENDING → REJECTED với reason
- **Toggle**: active ↔ inactive
- **Delete** (ADMIN only): soft delete
- Detail modal: đầy đủ thông tin SP + store + category + expiry + rejection reason

### 5.6 Finance (`/admin/finance`)
- Date range filter (default: tháng hiện tại)
- KPIs: GMV, commission, paid payouts, pending count + amount
- Chart 7 ngày GMV + commission
- **Pending payouts**: bảng các khoản chờ thanh toán
- **Process payout** (ADMIN only): kiểm tra balance (commission - paidOut ≥ pending), chuyển tất cả pending → COMPLETED
- **Wallet popup**: balance = commission - paidOut - pending
- **Reconciliation**: từng dòng store, xác nhận riêng + counter + undo, commit tổng
- **Export CSV**: BOM UTF-8 cho Excel

### 5.7 Dispute (`/admin/dispute`)
- KPIs: Total / Pending / Reviewing / Resolved-Refund
- Filter: status, search
- Detail panel: complainant + order info
- **Review**: PENDING → REVIEWING
- **Resolve**: REVIEWING → RESOLVED_REFUND (approve) hoặc RESOLVED_REJECTED (reject, có admin note)

### 5.8 Analytics (`/admin/analytics`)
- `GET /admin/api/analytics/overview?days=30`: revenue, orders, avg order, commission, active/scheduled deals, stores, buyers, pickup rate, cancel rate, revenue/order change % vs kỳ trước
- `GET /admin/api/analytics/chart?days=30`: daily revenue + orders + max revenue
- `GET /admin/api/analytics/top-stores?days=30`: top 10 stores theo revenue
- `GET /admin/api/analytics/recommendations`: 7 loại cảnh báo (pending disputes, cancel rate >15%, pending stores, deals expiring ≤3d, products pending approval, GMV decline, low-rated stores ≥10 reviews rating ≤3.0)

### 5.9 Orders (`/admin/orders`)
- Hiển thị theo store: danh sách store kèm total orders, revenue, pending orders
- Filter: status, city, search (tên store)
- Click store → modal orders table: filter theo status, expand item list, pagination

### 5.10 Admin Profile (`/admin/profile`)
- Info tab: xem thông tin
- Edit tab: sửa fullName, phone, avatar
- Security tab: đổi password (validate current + new ≥6 chars + confirm)

---

## 6. Luồng Nghiệp Vụ Moderator

Moderator có quyền giới hạn hơn Admin:
- `/moderator/dashboard`: KPIs + chart + pending stores
- `/moderator/seller-verify`: Approve/Reject store (giống admin nhưng không có reset-to-pending)
- **Không có quyền**: Finance (payout, reconciliation), Dispute management, delete product, gán category vào deal

---

## 7. Luồng Nghiệp Vụ Seller

### 7.1 Dashboard (`/seller/dashboard`)
- KPIs: total deals, active deals, scheduled deals, total products, pending orders, total orders
- Recent orders (5 đơn gần nhất)

### 7.2 Staff Management (`/seller/manage-staff`) — STORE_OWNER only
- **Add staff — mode=create**: username + password ≥6 + fullName → tạo user mới với ROLE_STORE_STAFF, weakPassword=true
- **Add staff — mode=existing**: lookup user by username/email, assign ROLE_STORE_STAFF + workStore
- **Remove staff**: revert về ROLE_USER, xóa workStore
- **Toggle staff**: active ↔ inactive

### 7.3 Deal Management (`/seller/deals`)
- Tabs: Live / Lên lịch / Tạm dừng (có count)
- Filter: status, search (dealName/dealCode)
- **Create Deal**: Form với auto-fill theo type (Flash Sale→AUTO_APPLY+20%, Voucher→FIXED+20k, Combo→PERCENT+15%, Seasonal→PERCENT+25%). Banner JPG/PNG/WEBP max 5MB. Scope=SPECIFIC_STORES, priority=50, status=SCHEDULED
- **Assign Products**: Chọn SP từ store (chưa có trong deal), set salePrice/originalPrice/maxQuantity, validate qua DealService
- **Pause**: ACTIVE→PAUSED (FIFO lock "DEAL")
- **Resume**: PAUSED→ACTIVE (check `now.isAfter(startTime) && now.isBefore(endTime)`)
- Card: tên deal, loại, giảm giá, thời gian, badge trạng thái, nút hành động

### 7.4 Product Management (`/seller/products`)
- Filter: status (active/inactive/outofstock), search, category, pagination (default 8/page)
- KPIs: approvedCount, pendingCount
- **Create Product**: Form với tên, giá, stock, category, HSD, ảnh (JPG/PNG/WEBP). COMBO: chọn ≥3 SP, giá = trung bình, HSD = sớm nhất
- **Quick Push** (`/seller/api/products/quick-push`):
  - Auto-detect SP sắp hết HSD (≤N ngày), nhóm theo category
  - Nhóm có ≥3 SP → tạo combo tự động: giá = 65% trung bình (giảm 35%), stock=10
- **Edit Product**: Reset approval → PENDING (phải duyệt lại)
- **Toggle**: Chỉ cho phép DEACTIVATE (active→inactive). Kích hoạt lại phải liên hệ admin
- **Delete**: Chỉ SP PENDING mới xóa được (soft delete)
- Detail popup: platform deals + store deals + finalPrice (floor 15% originalPrice)

### 7.5 Order Management (`/seller/orders`)
- Filter: status, search (orderId, customer name/username, product name), pagination
- Status counts: PENDING / CONFIRMED / READY_FOR_PICKUP / COMPLETED / CANCELLED
- Today's revenue
- **Update status** (FIFO lock "ORDER"): state machine như Section 4.1
- Order detail popup: items + quantity + price + deadline timer (PENDING: 60min auto-cancel, urgent ≤15min)

### 7.6 Finance (`/seller/finance`)
- Wallet: availableBalance = totalEarned - totalCommission - totalPaidOut - pendingPayout
- Commission rate theo partner tier
- Recent 10 completed orders, pending 20 payout requests
- **Request Payout** (`POST /seller/api/finance/request-payout`):
  - amount > 0, ≥ 50,000 VND, ≤ availableBalance
  - Tạo PAYOUT transaction PENDING (FIFO lock "STORE")
- **Edit Payout**: Chỉ PENDING, recalculate balance
- **Cancel Payout**: PENDING → FAILED

### 7.7 Analytics (`/seller/analytics`)
- Overview (30 ngày): revenue, orders, avg order, active deals, products, completion rate, commission
- Chart 14 ngày: daily revenue + orders
- Top 10 products (theo stock)
- Order stats: phân phối theo status
- **Recommendations** (6 loại):
  1. Low stock (≤5) → warning
  2. Deals expiring ≤3 ngày → warning
  3. Products pending approval → info
  4. Inactive products → info
  5. Tier upgrade suggestion (nếu BRONZE) → success
  6. No active deals → warning

### 7.8 Store Page Builder (`/seller/store-page`)
- 7 sections kéo thả: cover, info, description, categories, deals, products, hours
- Mỗi section: toggle on/off, expand edit, mini preview
- HTML5 drag-and-drop reorder
- Cover image upload (JPG/PNG/WEBP)
- Lưu config JSON: `sectionToggles||sectionOrder`

### 7.9 Profile (`/seller/profile`)
- **Update store**: name, address, city, district, phone, businessType, categories, bank info, logo (JPG/PNG/WEBP)
- **Update documents**: CCCD, business license, VSATTP. Validate: ảnh hoặc PDF, max 5MB
- **Change password**: current + new ≥6 + confirm (BCrypt). Nếu là staff → weakPassword=false

---

## 8. Luồng Nghiệp Vụ Staff

### 8.1 Dashboard (`/staff/dashboard`)
- Slot-based: chọn pickup slot (default slot đầu tiên)
- 3 queues: PENDING / CONFIRMED / READY_FOR_PICKUP (max 50 mỗi queue, filter theo slot)
- Counts: servedToday (COMPLETED), pendingToday
- Default slots: `["16:00-18:00", "17:00-19:00", "18:00-20:00"]`

### 8.2 Orders (`/staff/orders`)
- Giống seller: filter status + search + pagination
- **Update status**: Cùng state machine như seller (Section 4.1), FIFO lock "ORDER"
- **Order detail**: `findByIdWithAllDetails` — full info với items

### 8.3 Products (`/staff/products`)
- Filter: search + category, pagination 8/page
- **Create**: Form giống seller, JPG/PNG/WEBP max 5MB. Lưu trực tiếp `productRepository.save()`
- Combo: chọn SP ≥3, giá trung bình, HSD sớm nhất
- Detail: check SP thuộc store của staff

### 8.4 Profile (`/staff/profile`)
- Update: fullName, email, phone, avatar (JPG/PNG/WEBP max 2MB)
- Change password: current + new ≥6 + confirm → weakPassword=false
- Nếu weakPassword=true → popup "Mật khẩu quá yếu" → redirect đổi mật khẩu

---

## 9. Luồng Nghiệp Vụ Buyer

### 9.1 Home Page (`/`)
- Hero section: video background + stats (totalStores, totalUsers, totalDeals từ DB)
- **Quick filter chips**: Tất cả (scroll top) / Flash Sale (scroll → `#flashGrid`) / Combo (scroll → `#comboGrid`)
- **Flash Sale section**: Countdown timer dựa trên `endTime` sớm nhất, grid SP
- **Deal Nổi Bật**: Grid SP từ tất cả active deals
- **Combo section**: Banner gradient vàng + grid SP combo
- **Categories**: Grid category cards
- **Trust section**: 3 cards (An toàn, Chất lượng, Hỗ trợ 24/7)
- Dữ liệu: product-centric — mỗi DealProduct → 1 card với deal pricing

### 9.2 Category Page (`/category/{id}`)
- Back bar `history.back()`
- **Partner banners**: gradient 6 màu, store logo + name + product count
- **Search**: debounce 300ms, server-side filter
- **Price filter chips**: Tất cả / < 50k / 50-100k / 100-200k / > 200k
- **Store filter chips**: lọc theo cửa hàng, giữ search query
- Product grid: compact auto-fill, gold discount badge, store name
- Empty state riêng cho search vs không có SP

### 9.3 Product Detail (`/products/{id}`)
- Ảnh SP + tên + mô tả
- Deal info (nếu có): giá sale, giá gốc, % giảm, loại deal
- Store card: logo, tên, tier, rating, địa chỉ
- Nút "Thêm vào giỏ" + "Mua ngay"
- Related products từ cùng store (max 6)

### 9.4 Deal Detail (`/deals/{id}`)
- Deal header: badge loại + tên + mã + thời gian còn lại
- Discount card: % hoặc số tiền + điều kiện đơn tối thiểu
- Store card: logo + tên + rating
- Product list: ảnh + tên + giá sale + giá gốc + nút "Thêm vào giỏ"

### 9.5 Deal Map (`/buyer/deal-map`)
- **Map**: Leaflet + OpenStreetMap, fullscreen
- **Auto-locate GPS**: 800ms sau load, silent mode, update USER_LAT/USER_LNG
- **Data**: Chỉ store có lat/lng + có deal active + có DealProduct available
- **Urgency color**: danger (<1h) red, warning (<3h) yellow, safe (>3h) green
- **Floating controls**: Left (back, locate), Right (filter, zoom+, zoom-, radius toggle)
- **Category pills**: Tất cả / Flash Sale / Voucher / Combo / Seasonal
- **Bottom sheet**: kéo lên/xuống, danh sách deal kèm khoảng cách + thời gian
- **Filter panel**: Sắp xếp (gần nhất/urgency/discount), Bán kính (0.5-5km), Thời gian còn lại, Khoảng giá, Mức giảm giá
- Tất cả filter hoạt động JS client-side + kết hợp với category pills

### 9.6 Cart (`/buyer/cart`)
- **DB persist** (Cart + CartItem), migrate từ session khi login
- Hiển thị: ảnh SP, tên, store, giá sale + gốc, ±/remove
- **Stock validation**: không thể tăng quá stock, item unavailable → grey out + "Sản phẩm đã hết hàng"
- **Voucher**: modal chọn voucher (CODE_REQUIRED, active, trong thời gian), tính discount (percent → cap max, fixed → trừ thẳng)
- Cart badge mobile-nav: live count từ API `/api/cart/count`
- "Bạn chưa đăng nhập" state khi chưa auth

### 9.7 Checkout (`/buyer/checkout`)
- Items grouped by store
- Price breakdown: tạm tính, giảm giá deal, voucher discount, tổng
- Stepper 4 bước: Cart → Checkout → Payment → Done
- Notes field

### 9.8 Payment (`/buyer/payment`)
- Payment method radio cards: Bank Transfer (default, "Khuyên dùng"), MoMo (disabled), Credit Card (disabled)
- Order summary
- **POST `/buyer/checkout/confirm`**: FIFO lock "CART", tạo Order + OrderItems cho mỗi store, QR code tạm `DX-TMP-xxxxxx` → update `DX-{orderId}-xxxxxx`, clear cart + session voucher

### 9.9 Order Complete (`/buyer/order-complete`)
- Success hero + animated checkmark + confetti canvas
- Order info: mã đơn, tổng tiền, payment method, payment status (UNPAID badge)
- 4-step pickup guide (bao gồm thanh toán PayOS tại cửa hàng)

### 9.10 Order Tracking (`/buyer/orders`)
- 6 tabs: Tất cả / Chờ xác nhận / Đang xử lý / Sẵn sàng nhận / Hoàn thành / Đã hủy
- Client-side JS filter (data-status attributes)
- Color-coded status badges
- Full Vietnamese labels

### 9.11 Order Detail (`/buyer/orders/{id}`)
- Order info + items + status timeline
- Pickup QR code display (nếu READY_FOR_PICKUP)

### 9.12 Search (`/buyer/search`)
- Search header với debounce 300ms
- **Filter bar**: Loại deal (Tất cả/Flash Sale/Combo/Voucher), Khoảng giá (<50k/50-100k/100-200k/>200k), Sắp xếp (Phù hợp/Giá thấp→cao/Giá cao→thấp/Giảm giá nhiều)
- Product grid: `auto-fill, minmax(150px, 1fr)`, compact cards
- Store results: logo + tên + địa chỉ + tier badge, cùng padding với grid
- Empty state + initial hint

### 9.13 Store Pages
- **`/store/{storeId}`**: Cover ảnh, logo, tên, rating, tier badge, info chips (địa chỉ, giờ mở cửa, SĐT), description, categories, active deals scroll, products grid
- **`/buyer/store/{storeId}/profile`**: Back bar + store header (logo tròn, tên, địa chỉ, rating, tier) + description + products compact grid

### 9.14 Profile (`/buyer/profile` + `/profile`)
- Green gradient header: avatar (click để upload), tên, email, role badge
- Stats row: Đơn hàng / Đã hoàn thành / Đang xử lý
- Recent orders: 5 đơn gần nhất, color-coded status badge
- **Menu**:
  - Đơn hàng của tôi → `/buyer/orders`
  - **Thông tin cá nhân** (expandable): edit fullName, phone, address + avatar upload (JPG/PNG/WEBP/GIF max 3MB, MD5 dedup)
  - **Sổ địa chỉ** (expandable): add/edit/delete address, set default. Form: name + phone + city + district + ward + address + isDefault checkbox. Bottom sheet modal
  - **Đổi mật khẩu** (expandable): current + new ≥6 + confirm, BCrypt verify
- Logout button

### 9.15 Pickup QR Confirm (`/pickup/{qrCode}`)
- Public endpoint (không cần auth)
- GET: hiển thị order info + nút xác nhận
- POST confirm: FIFO lock "ORDER", verify READY_FOR_PICKUP → COMPLETED + actualPickupTime
- Chặn double-confirm: check `COMPLETED` status

---

## 10. Luồng Auth & Onboarding

### 10.1 Đăng Ký Buyer
1. Form: fullName, email, phone (VN: `03/05/07/08/09` + 8 số), password ≥8 ký tự
2. Gửi OTP email (5 phút expiry)
3. Xác thực OTP
4. Tạo User: username = email prefix, role = USER, provider = local
5. Avatar upload: JPG/PNG/WEBP/GIF max 5MB

### 10.2 Đăng Ký Seller (3 bước)
**Bước 1**: firstName, lastName, email, phone, password, storeName, businessType, categories, address, city, district, description

**Bước 2 — Documents**: CCCD (max 2 files, ảnh/PDF, max 5MB), Business License (required), VSATTP (optional). Validate type + size. Nếu user đã có document cũ, có thể skip upload.

**Bước 3 — Bank**: bankName, bankAccountNumber, bankAccountOwner

- Store status = PENDING
- User role → STORE_OWNER
- Check duplicate: storeName (unique), phone (unique, trừ của chính user)
- **Re-submit**: Nếu store bị reject, seller có thể đăng ký lại → reset về PENDING

### 10.3 Onboarding Pending (`/auth/onboarding-pending`)
- Hiển thị: storeStatus, storeName, submittedAt, trạng thái document (đã nộp/chưa), bank info
- Tự redirect nếu store đã ACTIVE

### 10.4 Password Reset
- **Send OTP**: Kiểm tra email tồn tại (vẫn trả success nếu không tồn tại — security)
- **Block admin reset**: Admin không được reset qua web
- **Verify OTP + Reset**: OTP 10 phút expiry, password mới ≥8 ký tự, BCrypt encode

### 10.5 Login Variants
- `/login` — Buyer
- `/admin/login` — Admin/Moderator
- `/seller/login` — Seller/Staff (chung cổng)
- Google OAuth2 → tự động tạo/link account

### 10.6 Logout
- **Generic `/logout`**: Xác định redirect từ Referer header → `/admin/login` / `/seller/login` / `/login`
- **Buyer `/api/auth/logout`**: JSON API, invalidate session, clear context, delete all cookies
- **Seller `/seller/logout`**: SecurityContextLogoutHandler → redirect `/seller/login`

### 10.7 Session Management
- Timeout: 30 phút (cả session lẫn cookie)
- JDBC-backed: Spring Session, bảng `SPRING_SESSION`
- Max 1 session/user: login mới → expire session cũ
- Session fixation protection: migrateSession
- Stale session handling: nếu user không tồn tại trong DB → force logout, xóa cookies, redirect `?session_expired=true`

### 10.8 Get Current User Pattern
```java
User user = userRepository.findByUsername(principal.getName()).orElse(null);
if (user == null) user = userRepository.findByEmail(principal.getName()).orElse(null);
// Seller: nếu workStore null, tự lookup storeRepository.findByOwner(user)
```

---

## 11. Hệ Thống Thanh Toán PayOS

### 11.1 Luồng

```
1. Order READY_FOR_PICKUP
2. Seller/Staff tạo payment QR → POST /api/payment/create/{orderId}
   → PayOSService.createPaymentLink()
      - description: "Thanh toan don hang DX-{orderId}"
      - orderCode: orderId * 1000 + (epochSeconds % 1000)
      - cancelUrl/returnUrl: localhost:8084/buyer/orders/{orderId}
      - expiredAt: now + 30 phút
      - Signature: HMAC-SHA256("amount=X&cancelUrl=X&description=X&orderCode=X&returnUrl=X")
      - POST /v2/payment-requests → nhận checkoutUrl + qrCode (VietQR NAPAS string)

3. QR ảnh: generate từ api.qrserver.com (third-party)

4. Buyer quét QR → thanh toán qua app ngân hàng

5. Xác nhận:
   - Production: PayOS webhook → POST /api/payment/webhook (public)
   - Localhost: Polling → GET /api/payment/status/{orderId}
     → PayOSService.checkPaymentStatus()
```

### 11.2 3 Case Xử Lý

| Case | Điều kiện | Hành động |
|------|----------|-----------|
| **Đủ tiền** | `amountPaid >= expectedAmount` | order.status=COMPLETED, paymentStatus=PAID, deduct stock (PESSIMISTIC_WRITE lock, set active=false nếu stock=0), tạo Transaction SALE |
| **Thiếu tiền** | `amountPaid < expectedAmount` | paymentStatus=PARTIAL, note ghi số đã trả + còn thiếu |
| **Thừa tiền** | `amountPaid > expectedAmount` | COMPLETED + warning message "Số tiền thanh toán nhiều hơn" |

### 11.3 Exactly-Once Guarantee
- **PESSIMISTIC_WRITE lock**: `orderRepository.findByIdWithLock()` trước khi deduct stock
- **Double-check**: kiểm tra `paymentStatus != "PAID"` trước khi xử lý
- **FIFO lock**: `lockManager.acquireLock("ORDER", orderId)` bọc ngoài

### 11.4 Platform Fee
- `calculatePlatformFee(amount, store)`: dùng `store.getCommissionRate()` (mặc định 10% cho BRONZE)
- Transaction: type=SALE, amount=finalAmount, platformFee=commission, netAmount=amount-platformFee

---

## 12. Hệ Thống Deal — Rules & Validate

### 12.1 Validate Deal Creation (DealService — 8 bước)

| Bước | Rule | Chi Tiết |
|------|------|----------|
| **0** | FREESHIP bị từ chối | Throw ngay nếu dealType = FREESHIP |
| **1** | Code unique | Không null/rỗng, match `^[A-Z0-9_-]+$`, không trùng deal chưa CANCELLED |
| **2** | Discount value | PERCENT: 1%–85%, FIXED: 1,000đ–500,000đ |
| **3** | Fixed discount vs giá TB SP | avgPrice < 50k → max = avgPrice×0.5; 50k-200k → ×0.7; >200k → ×0.8 |
| **4** | Time range theo loại | FLASH_SALE ≤6h, VOUCHER ≤30d, COMBO ≤15d, SEASONAL ≤30d, max chung 30d |
| **5** | Time overlap | SP không nằm trong deal khác cùng store overlap thời gian |
| **6** | Seller eligibility | Store phải có ≥1 approved product mới được tạo deal |
| **7** | Min order amount | Không âm |
| **8** | AUTO_APPLY requirements | Placeholder check |

### 12.2 Validate Product vào Deal

| Rule | Chi Tiết |
|------|----------|
| **Product prices** | originalPrice > 0, salePrice ≥ 20% originalPrice, salePrice < originalPrice |
| **No overlap** | SP không nằm trong deal ACTIVE/SCHEDULED khác cùng thời gian |
| **Cumulative discount** | Tổng discount (platform + store) ≤ cap: 65% thường, 70% Flash Sale, 80% near-expiry (≤3 ngày) |
| **Platform cap** | Platform-only: cap cứng 30%, warning 28%. Khi gán category: duyệt từng SP, reject nếu vượt |

### 12.3 Tính Cumulative Discount

```java
for each platform deal on product's category (ACTIVE/SCHEDULED, overlap time):
    platformDiscount += calculateDealDiscount(deal, originalPrice)
currentDiscount = originalPrice - salePrice
totalPercent = ((platformDiscount + currentDiscount) / originalPrice) * 100
cap = getMaxDiscountCap(product, deal) // 65/70/80%
if totalPercent > cap → REJECT
```

---

## 13. Hệ Thống Lọc

### 13.1 Search Page (`/buyer/search`)

| Filter | Options | Mechanism |
|--------|---------|-----------|
| Query | text input, debounce 300ms | Server-side: `product.name.toLowerCase().contains(q)` |
| Deal type | Tất cả / Flash Sale / Combo / Voucher | Server-side: filter theo `dealProductMap` |
| Price range | Tất cả / <50k / 50-100k / 100-200k / >200k | Server-side: filter theo salePrice hoặc originalPrice |
| Sort | Phù hợp nhất / Giá thấp→cao / Giá cao→thấp / Giảm giá nhiều | Server-side: sort stream |

### 13.2 Category Page (`/category/{id}`)

| Filter | Options | Mechanism |
|--------|---------|-----------|
| Search | debounce 300ms | Server-side filter bổ sung |
| Store | chips theo featured stores | Server-side: `stream.filter(storeId)` |
| Price | Tất cả / <50k / 50-100k / 100-200k / >200k | Server-side: `getEffectivePrice()` |

### 13.3 Deal Map (`/buyer/deal-map`)

| Filter | Options | Mechanism |
|--------|---------|-----------|
| Category pills | Tất cả / Flash Sale / Voucher / Combo / Seasonal | Client-side: `DEALS.filter(d.dealType)` |
| Sort | Gần nhất / Urgency / Discount | Client-side sort |
| Radius | 0.5–5 km | Client-side: `calcDist() <= radius * 1000` |
| Time remaining | <30 phút / <1h / <3h / >3h | Client-side: filter theo urgency + timeLeft |
| Price range | Min–Max input | Client-side: filter theo `d.price` |
| Discount | ≥30% / ≥50% / ≥70% | Client-side: filter theo `d.discount` |

### 13.4 Search Overlay (Shopee-style)

| State | Behavior |
|-------|----------|
| Chưa gõ / <2 ký tự | "Tìm kiếm phổ biến" chips (Bánh mì, Cà phê, Bún phở, Trà sữa, Cơm, Bánh ngọt) |
| Gõ ≥2 ký tự | `GET /api/search/suggest?q=` → 4 SP (ảnh + tên + store) + 3 Store (logo + tên + địa chỉ + tier) |
| Click suggestion SP | Redirect `/buyer/search?q=` |
| Click suggestion Store | Redirect `/store/{id}` |
| Enter / "Tìm kiếm toàn hệ thống" | Redirect `/buyer/search?q=` |

### 13.5 Home Page Quick Filter

| Chip | Behavior |
|------|----------|
| Tất cả | `scrollTo('all')` — cuộn lên đầu |
| Flash Sale | `scrollTo('flash')` — cuộn đến `#flashGrid` |
| Combo | `scrollTo('combo')` — cuộn đến `#comboGrid` |

### 13.6 Admin/Seller Tables

Tất cả bảng admin/seller dùng pattern: form GET với `oninput="debounceSearch()"` 300ms + select `onchange="this.form.submit()"` + phân trang giữ nguyên filter params.

---

## 14. Hệ Thống Thông Báo

### 14.1 Loại Thông Báo

| Type | Trigger | Link |
|------|---------|------|
| ORDER_CONFIRMED | Seller confirm đơn | `/buyer/orders` |
| ORDER_READY | Đơn READY_FOR_PICKUP | `/buyer/orders` |
| ORDER_COMPLETED | Đơn hoàn thành | `/buyer/orders` |
| ORDER_CANCELLED | Đơn bị hủy | `/buyer/orders` |
| DEAL_NEARBY | Deal mới gần vị trí (notifyNewDealNearby) | `/products/{id}` |
| STORE_APPROVED | Cửa hàng được duyệt | (seller) |

### 14.2 Giao Diện

- **Bell icon** (header buyer): badge đỏ hiển thị số chưa đọc, tự refresh mỗi 60s
- **Dropdown popup**: 20 thông báo gần nhất, icon theo loại (📦/🎉/✅/❌), chấm xanh nếu chưa đọc, time ago
- **Click**: mark as read + điều hướng (nếu có linkUrl)
- **"Đánh dấu đã đọc"**: mark all read
- **API**: `GET /api/notifications`, `POST /api/notifications/read`, `POST /api/notifications/read-all`, `GET /api/notifications/count`

### 14.3 NotificationService
- `createNotification(user, title, message, type, linkUrl)`: tạo mới, isRead=false, createdAt=now
- `notifyOrderStatusChange(user, orderId, newStatus)`: map CONFIRMED/READY_FOR_PICKUP/COMPLETED/CANCELLED → notification
- `notifyNewDealNearby(user, storeName, dealName, dealId)`: type=DEAL_NEARBY

---

## 15. Lịch Jobs & Cron

### 15.1 ExpiryCountdownService
**Cron:** `0 0 1 * * *` (1:00 AM hàng ngày)

```
for each product (active, not deleted, has expiryDate):
    daysUntilExpiry = days between today and expiryDate
    if daysUntilExpiry <= 0:
        currentPrice = 0, active = false (FIFO lock "PRODUCT")
    elif daysUntilExpiry <= 7:
        currentPrice = round(originalPrice * daysUntilExpiry / 7)
    else:
        if currentPrice != originalPrice → reset = originalPrice
```

### 15.2 StoreService.upgradePartnerTiers()
**Cron:** `0 0 2 1 * *` (2:00 AM ngày 1 hàng tháng)

```
for each ACTIVE store:
    revenue = sumRevenueByPeriod(lastMonthStart, lastMonthEnd)
    orders = countOrdersByPeriod(lastMonthStart, lastMonthEnd)
    rating = store.averageRating

    if revenue > 500M && orders > 2000 && rating >= 4.5 → DIAMOND
    elif revenue > 200M && orders > 500 && rating >= 4.3 → GOLD
    elif revenue > 50M && orders > 100 && rating >= 4.0 → SILVER
    else → BRONZE

    if tier changed → FIFO lock "STORE" + update
```

### 15.3 ResourceLockManager.cleanupUnusedLocks()
**Fixed rate:** 5 phút → xóa lock không còn locked + không có thread waiting

---

## 16. Concurrency Control

### 16.1 ResourceLockManager
- `ConcurrentHashMap<String, ReentrantLock>` với `fair = true` (FIFO)
- Key format: `"{ENTITY_TYPE}:{entityId}"`
- Pattern sử dụng:
```java
ReentrantLock lock = lockManager.acquireLock("ORDER", orderId);
try {
    // critical section
} finally {
    lock.unlock();
}
```

### 16.2 Lock Usage Matrix

| Entity | Sử Dụng Ở |
|--------|-----------|
| ORDER | Seller/Staff update status, PayOS payment confirm/webhook, Pickup QR confirm |
| PRODUCT | DealService add/remove product, ProductService update/approve/reject/toggle/delete, ExpiryCountdown |
| DEAL | DealService update/delete, Seller pause/resume |
| STORE | DealService createDeal, StoreService tier upgrade, Seller payout request |
| CART | CartService add/remove/update/clear, HomeController checkout confirm |

### 16.3 Database-Level Locking
- `orderRepository.findByIdWithLock()`: `@Lock(PESSIMISTIC_WRITE)` — dùng trong PayOS confirm payment để đảm bảo exactly-once stock deduction

---

## 17. Database & Migrations

### 17.1 Tables (17 entities → 16 tables + SPRING_SESSION)

| # | Table | Key Columns |
|---|-------|-------------|
| 1 | `users` | user_id, username, email, password, full_name, phone, address, avatar_url, active, weak_password, provider, provider_id, reset_token, role_id, work_store_id |
| 2 | `roles` | role_id, name, description |
| 3 | `stores` | store_id, store_name, ..., latitude, longitude, partner_tier, bank_*, cccd_url(1000), business_license_url(1000), vsattp_url(1000), store_page_config(TEXT), owner_id, approved_by, rejected_by |
| 4 | `products` | product_id, name, description(TEXT), image_url, original_price, deal_price, current_price, stock_quantity, product_type, expiry_date, deal_start/end_time, active, deleted, approval_status, rejection_reason(TEXT), store_id, category_id, created_by |
| 5 | `categories` | category_id, name, icon_url, description |
| 6 | `deals` | deal_id, deal_name, deal_code, deal_type, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, start_time, end_time, status, scope, apply_method, banner_url, store_id, created_by |
| 7 | `deal_products` | deal_product_id, deal_id, product_id, original_price, sale_price, max_quantity, sold_quantity, priority |
| 8 | `deal_categories` | deal_category_id, deal_id, category_id, priority, active |
| 9 | `orders` | order_id, total_amount, discount_amount, coupon_code, final_amount, status, payment_status, payment_method, pickup_qr_code, note, cancellation_reason, user_id, store_id |
| 10 | `order_items` | order_item_id, order_id, product_id, quantity, unit_price |
| 11 | `transactions` | transaction_id, store_id, order_id, type, amount, platform_fee, net_amount, status, transaction_ref, payment_method, description |
| 12 | `reviews` | review_id, rating(1-5), comment(TEXT), image_url, verified, user_id, product_id, store_id, order_id |
| 13 | `notifications` | notification_id, title, message(TEXT), type, link_url, is_read, created_at, user_id |
| 14 | `disputes` | dispute_id, order_id, user_id, reason, description(TEXT), evidence_url, status, admin_note |
| 15 | `carts` | cart_id, user_id (UNIQUE), created_at, updated_at |
| 16 | `cart_items` | cart_item_id, cart_id, product_id, quantity, unit_price (UNIQUE cart_id+product_id) |
| 17 | `user_addresses` | id, user_id, name, phone, address, city, district, ward, is_default(TINYINT) |

### 17.2 Flyway Migrations

| Version | Nội Dung |
|---------|----------|
| V1 | Core schema (JPA auto-generate từ entities) |
| V2 | `products.approval_status VARCHAR(20) DEFAULT 'PENDING'`, `rejection_reason TEXT`, index |
| V3a | `deals.apply_method VARCHAR(20) DEFAULT 'CODE_REQUIRED'` |
| V3b | `deal_categories` table (deal_id, category_id, priority, active) |
| V4 | CHECK constraints: discount range, time range, min_order, sale price (≥15% original), sold_quantity ≤ max_quantity |
| V5 | `stores.partner_tier VARCHAR(20) DEFAULT 'BRONZE'` |
| V6 | `products.current_price DOUBLE DEFAULT NULL` |
| V7 | `transactions.transaction_ref VARCHAR(255)`, `payment_method VARCHAR(50)` + `products.created_by BIGINT FK` |
| V8 | `carts` + `cart_items` tables + `users.weak_password TINYINT(1) DEFAULT 0` |
| V9 | `user_addresses` table (id, user_id FK, name, phone, address, city, district, ward, is_default) |

---

## 18. File Upload & Storage

### 18.1 Quy Tắc Chung
- **Thư mục**: `./uploads/` (serve qua `/uploads/**` → WebMvcConfig)
- **Tên file**: MD5 hash của nội dung → tự động dedup (cùng nội dung = cùng file, không ghi đè)
- **Max request size**: 10MB (Spring multipart config)

### 18.2 Validate Theo Loại File

| Loại | Định Dạng | Max Size | Nơi Sử Dụng |
|------|-----------|----------|-------------|
| Ảnh SP/Banner/Logo | JPG/PNG/WEBP | 5MB | Seller/Admin/Staff create/edit product, deal banner, store logo |
| Avatar | JPG/PNG/WEBP/GIF | 3MB (buyer), 2MB (staff) | Profile update |
| Document (CCCD, license, VSATTP) | JPG/PNG/WEBP/GIF/BMP/PDF | 5MB | Seller registration/onboarding, seller profile update |

---

## 19. Security Config

### 19.1 Route Permissions (đầy đủ)

| Pattern | Access |
|---------|--------|
| `/css/**`, `/js/**`, `/img/**`, `/images/**`, `/uploads/**`, `/static/**` | PUBLIC |
| `/login`, `/admin/login`, `/seller/login`, `/register/**`, `/forgot-password/**`, `/reset-password`, `/terms`, `/logout`, `/oauth2/**`, `/api/auth/**` | PUBLIC |
| `/seller/onboarding-pending`, `/auth/onboarding-pending` | AUTHENTICATED |
| `/admin/**` | ADMIN hoặc MODERATOR |
| `/seller/**`, `/store/dashboard/**` | STORE_OWNER |
| `/staff/**` | STORE_STAFF |
| `/`, `/home`, `/shop/**`, `/product/**`, `/products/**`, `/store/**` | PUBLIC |
| `/profile`, `/checkout/**`, `/my-orders/**`, `/buyer/profile`, `/buyer/checkout/**`, `/buyer/payment`, `/buyer/order-complete`, `/buyer/checkout/confirm` | AUTHENTICATED |
| `/cart/**`, `/buyer/cart`, `/category/**`, `/deals/**`, `/buyer/search`, `/buyer/store/**`, `/buyer/deal-map`, `/buyer/orders/**` | PUBLIC |
| Mọi request khác | PUBLIC (permitAll) |

### 19.2 CSRF
- **Enabled globally**, disabled cho: `/cart/*`, `/api/**`, `/perform_login`, tất cả admin/seller/staff POST endpoints (deal, product, order, finance, staff, profile), `/buyer/checkout/confirm`, `/pickup/*/confirm`, moderator approve/reject

### 19.3 Other Security
- BCrypt password encoder
- Remember-me: 7 ngày, key `dealxanh-remember-me-secret-key-2024`
- Max 1 session/user, new login → expire old
- OAuth2 Google: tự động tạo/link account qua email

---

## 20. Toàn Bộ API Index

### 20.1 Buyer/Public (HomeController)

| Method | URL | Auth |
|--------|-----|------|
| GET | `/`, `/home` | Public |
| GET | `/category/{id}` | Public |
| GET | `/products/{id}` | Public |
| GET | `/deals/{id}` | Public |
| GET | `/store/{id}` | Public |
| GET | `/buyer/deal-map` | Public |
| GET | `/buyer/search` | Public |
| GET | `/buyer/store/{id}/profile` | Public |
| GET | `/api/search/suggest` | Public |
| GET | `/pickup/{qrCode}` | Public |
| POST | `/pickup/{qrCode}/confirm` | Public |
| GET | `/buyer/cart` | Public |
| GET | `/buyer/orders` | Public |
| GET | `/buyer/orders/{id}` | Public |
| GET | `/buyer/profile` | Auth |
| GET | `/buyer/checkout` | Auth |
| GET | `/buyer/payment` | Auth |
| GET | `/buyer/order-complete` | Auth |
| POST | `/buyer/checkout/confirm` | Auth |
| GET | `/api/cart` | Auth |
| POST | `/api/cart/add` | Auth |
| POST | `/api/cart/remove` | Auth |
| POST | `/api/cart/update` | Auth |
| GET | `/api/cart/count` | Auth |
| GET | `/api/vouchers/available` | Public |
| POST | `/api/cart/apply-voucher` | Auth |
| POST | `/api/cart/remove-voucher` | Auth |
| GET | `/api/notifications` | Auth |
| POST | `/api/notifications/read` | Auth |
| POST | `/api/notifications/read-all` | Auth |
| GET | `/api/notifications/count` | Auth |
| POST | `/api/buyer/profile/update` | Auth |
| POST | `/api/buyer/profile/change-password` | Auth |
| POST | `/api/buyer/profile/avatar` | Auth |
| GET | `/api/buyer/addresses` | Auth |
| POST | `/api/buyer/addresses/add` | Auth |
| POST | `/api/buyer/addresses/update` | Auth |
| POST | `/api/buyer/addresses/delete` | Auth |

### 20.2 Admin

| Method | URL | Role |
|--------|-----|------|
| GET | `/admin/dashboard` | ADMIN/MOD |
| GET/POST | `/admin/users` + toggle/edit/detail | ADMIN |
| GET/POST | `/admin/seller-verify` + approve/reject/reset | ADMIN/MOD |
| GET/POST | `/admin/deals` + create/update/pause/resume + assign/remove category/product | ADMIN/MOD |
| GET/POST | `/admin/products` + approve/reject/toggle/delete | ADMIN/MOD |
| GET/POST | `/admin/finance` + payout/CSV/wallet/reconciliation | ADMIN |
| GET/POST | `/admin/dispute` + review/resolve | ADMIN |
| GET | `/admin/analytics` + API overview/chart/top-stores/recommendations | ADMIN/MOD |
| GET | `/admin/orders` + shop orders | ADMIN/MOD |
| GET/POST | `/admin/profile` + update/password | ADMIN/MOD |
| GET | `/admin/api/dashboard/stats` | ADMIN/MOD |
| GET | `/admin/api/buyers/stats` | ADMIN/MOD |

### 20.3 Seller

| Method | URL | Role |
|--------|-----|------|
| GET | `/seller/dashboard` | OWNER/STAFF |
| GET/POST | `/seller/manage-staff` + add/remove/toggle | OWNER |
| GET/POST | `/seller/deals` + create/pause/resume + assign/remove product | OWNER/STAFF |
| GET/POST | `/seller/products` + create/edit/toggle/delete/detail + combo-available/expiring-soon/quick-push | OWNER/STAFF |
| GET/POST | `/seller/orders` + status update + detail | OWNER/STAFF |
| GET/POST | `/seller/finance` + wallet/transactions/request-payout/edit-payout/cancel-payout | OWNER/STAFF |
| GET | `/seller/analytics` + overview/chart/top-products/order-stats/recommendations | OWNER/STAFF |
| GET/POST | `/seller/store-page` + update | OWNER |
| GET/POST | `/seller/profile` + update-store/update-documents/change-password | OWNER/STAFF |

### 20.4 Staff

| Method | URL | Role |
|--------|-----|------|
| GET | `/staff/dashboard` | STAFF |
| GET/POST | `/staff/orders` + status update + detail | STAFF |
| GET/POST | `/staff/products` + create + combo-available + detail | STAFF |
| GET/POST | `/staff/profile` + update + change-password | STAFF |

### 20.5 Payment, Auth, Other

| Method | URL | Auth |
|--------|-----|------|
| POST | `/api/payment/create/{orderId}` | OWNER/STAFF |
| POST | `/api/payment/webhook` | Public |
| GET | `/api/payment/status/{orderId}` | OWNER/STAFF |
| POST | `/api/auth/send-otp`, `/verify-otp` | Public |
| POST | `/api/auth/register-buyer`, `/register-seller` | Public |
| GET | `/api/auth/seller-status` | Public |
| POST | `/api/auth/send-reset-otp`, `/verify-reset-otp`, `/reset-password` | Public |
| GET | `/api/auth/check-auth` | Public |
| GET | `/logout`, `/api/auth/logout`, `/seller/logout` | Auth |
| POST | `/api/util/update-admin-password` | **NO AUTH** (⚠️ security gap) |

---

## 21. Toàn Bộ Trang / Site Map

### 21.1 Buyer (21 pages)
```
/                              — Home
/category/{id}                 — Category (Shopee-style)
/products/{id}                 — Product Detail
/deals/{id}                    — Deal Detail
/store/{id}                    — Dynamic Store Page
/buyer/deal-map                — Bản đồ Deal (Leaflet)
/buyer/search?q=               — Tìm kiếm tổng + filter
/buyer/cart                    — Giỏ hàng
/buyer/checkout                — Checkout
/buyer/payment                 — Thanh toán
/buyer/order-complete          — Hoàn tất đơn
/buyer/orders                  — Theo dõi đơn (6 tabs)
/buyer/orders/{id}             — Chi tiết đơn
/buyer/profile                 — Profile buyer
/buyer/store/{id}/profile      — Store Profile cho buyer
/pickup/{qrCode}               — Xác nhận pickup (QR)
/login                         — Đăng nhập buyer
/splash                        — Splash screen
/filter                        — Filter page
```

### 21.2 Admin (12 pages)
```
/admin/login                   — Login admin
/admin/dashboard               — Dashboard KPIs + chart
/admin/users                   — Quản lý user
/admin/seller-verify           — Xét duyệt store
/admin/deals                   — Quản lý deal
/admin/products                — Quản lý sản phẩm
/admin/orders                  — Quản lý đơn hàng (theo store)
/admin/finance                 — Tài chính & Đối soát
/admin/analytics               — Phân tích & Báo cáo
/admin/dispute                 — Khiếu nại & Tranh chấp
/admin/profile                 — Profile admin
```

### 21.3 Moderator (2 pages)
```
/moderator/dashboard           — Dashboard mod
/moderator/seller-verify        — Xét duyệt store (giới hạn)
```

### 21.4 Seller (15 pages)
```
/seller/login                  — Login seller/staff
/seller/dashboard              — Dashboard KPIs
/seller/deals + /create        — Quản lý deal
/seller/products + /create     — Quản lý sản phẩm
/seller/orders                 — Quản lý đơn hàng
/seller/finance                — Ví & Tài chính
/seller/analytics              — Phân tích
/seller/profile                — Profile cửa hàng
/seller/store-page             — Store builder (drag-drop)
/seller/manage-staff           — Quản lý nhân viên
/seller/fulfillment            — Xử lý đơn
```

### 21.5 Staff (4 pages)
```
/staff/dashboard               — Dashboard slot-based
/staff/orders                  — Quản lý đơn
/staff/products + /create      — Quản lý SP
/staff/profile                 — Profile staff
```

### 21.6 Auth (8 pages)
```
/register                      — Chọn role
/register/buyer                — Đăng ký buyer
/seller/register               — Đăng ký seller
/auth/onboarding-step1/2/3     — Onboarding 3 bước
/auth/onboarding-pending       — Chờ duyệt
/forgot-password               — Quên mật khẩu
/terms                         — Điều khoản
```

### 21.7 Common Fragments
```
common/buyer/  — header, footer, mobile-nav, css, js, search-overlay
common/admin/  — admin-layout, sidebar, header, css, js
common/seller/ — bottom-nav, header, sidebar
common/staff/  — bottom-nav, header, css
common/auth/   — back-header, css, js
```

---

## 22. Checklist Chuẩn Bị Release

### Trước Mỗi Commit
- [ ] Fragment dùng `th:replace` đúng pattern, không hardcode sidebar/header
- [ ] `#numbers.formatDecimal(val, 0, 0)` cho mọi số trong Thymeleaf
- [ ] `@JsonIgnore` trên tất cả `@OneToMany` trong Entity
- [ ] `th:classappend` không ghi đè bằng `th:class`
- [ ] Pipe syntax `|...|` cho `th:text` tiếng Việt
- [ ] `data-*` + `onclick` thay vì `th:onclick` với string variable
- [ ] `getCurrentUser()` thử cả `findByUsername` VÀ `findByEmail`
- [ ] CSRF ignore cho POST endpoint mới (nếu không nằm trong `/api/**`)
- [ ] Không dùng backtick `${}` template literal trong file `.html` (xung đột Thymeleaf)
- [ ] `event.stopPropagation()` trong button nằm trong clickable card
- [ ] Validate file upload (type + size) trước `saveUploadedFile()`
- [ ] `Map.of()` chỉ ≤10 cặp, nếu không dùng `HashMap`
- [ ] Null-safe + empty string check trước `LocalDateTime.parse()`
- [ ] `@PreAuthorize` đúng role cho endpoint mới
- [ ] Run Flyway migration nếu có schema change
- [ ] `product.isAvailable()` thay vì check từng field riêng

---

*Tài liệu được tạo từ phân tích toàn bộ codebase: 11 controllers, 9 services, 17 entities, 17 repositories, 87 templates, 9 migrations, CLAUDE.md, và git history (50+ commits).*

---

## 23. Development Timeline

**Dev:** Phan Anh · **Period:** Apr 26 – Jun 5, 2026 (41 ngày) · **Total:** 41 commits

### Week 1: Foundation & Auth (Apr 26 – May 2)

**Apr 26 (Sat):** Khởi tạo Spring Boot project (pom.xml, application.properties, .gitignore). Tạo AppConstants (40+ constants), WebMvcConfig. Tạo README.md.

**Apr 27 (Sun):** Tạo 16 entity classes + 12 repository interfaces. Tạo DTO: RegisterBuyerRequest, RegisterSellerRequest, CartItem.

**Apr 28 (Mon):** Tạo SecurityConfig (RBAC 5 roles, OAuth2 Google, form login, session JDBC, CSRF, remember-me). Tạo AuthController, AuthApiController (API register với OTP), HomeController skeleton.

**Apr 29 (Tue):** Tạo OtpService (6-digit OTP, 5-min expiry). Tạo EmailService (Gmail SMTP). Tạo CSS system (variables, reset, layout, components). Tạo templates: login, register-buyer, register-seller, forgot-password, terms.

**Apr 30 (Wed):** Fix register-buyer form validation + avatar upload (MD5 hash). Fix seller registration file upload. Tạo auth/onboarding templates (step1/2/3, pending).

**May 1 (Thu):** Tạo admin templates: login, dashboard, users. Tạo error pages: 403, 404, 500. Tạo common/auth fragments.

**May 2 (Fri):** Tạo data.sql, fix_roles.sql. Tạo password reset flow (send-reset-otp, verify-reset-otp, reset-password). Block admin reset qua web.

### Week 2: Auth Handlers & Admin Core (May 3 – 9)

**May 3 (Sat):** Tạo CustomAuthenticationSuccessHandler, CustomAuthenticationFailureHandler, CustomOAuth2UserService.

**May 4 (Sun):** Tạo CustomAuthenticationEntryPoint, CustomInvalidSessionStrategy, CustomSessionExpiredStrategy, CustomUserDetailsService.

**May 5 (Mon):** Fix login error handling. Fix sai trang login detection. Tăng cccd_url lên 1000 chars. Fix onboarding role management.

**May 6 (Tue):** Implement `/profile` buyer. Tạo mobile-nav.html (5 tabs + auth check). Refactor login & profile templates.

**May 7 (Wed):** Tạo AdminController (dashboard, seller-verify, users, dispute). Tạo AdminDashboardApiController (stats + period filter + delta). Tạo PasswordUpdateController, PasswordGenerator, script tạo admin/mod.

**May 8 (Thu):** Tạo ModeratorController (dashboard, seller-verify). Moderator login redirect về /admin/dashboard. Tạo moderator templates standalone.

**May 9 (Fri):** Tạo admin layout system (admin-layout, header, sidebar). Bổ sung StoreRepository, UserRepository queries. Admin UI polish.

### Week 3: Admin Nâng Cao (May 10 – 16)

**May 10 (Sat):** Admin user management: edit modal, quality label (Kém/Ổn/Tốt), auto-disable seller (≥40 reviews & rating ≤2.5).

**May 11 (Sun):** Seller verification: reset-to-pending + email. Fix CSRF ignore.

**May 12 (Mon):** Remove Coupon entity + CouponRepository. Fix doc link disabled state.

**May 13 (Tue):** Tạo CLAUDE.md. Tạo common/buyer fragments. Tạo buyer home page skeleton.

**May 14 (Wed):** Deal management: xóa FREESHIP, auto-fill theo type, banner upload (MD5 dedup), validate SP overlap.

**May 15 (Thu):** Finance: KPI cards, chart 7 ngày, TransactionRepository 14 queries, Export CSV, Run Payout, Wallet popup.

**May 16 (Fri):** Reconciliation modal (per-store confirm + undo). Dispute management (PENDING→REVIEWING→RESOLVED). Analytics dashboard (KPI % change, top 10 stores, 8 recommendations). Profile BCrypt verify + strength meter.

### Week 4: Seller Panel (May 17 – 23)

**May 17 (Sat):** @JsonIgnore toàn bộ @OneToMany. Fix #numbers.formatDecimal (11 occurrences). Fix Map.of()→HashMap. Fix Deal Card UI (stopPropagation, pipe syntax). Fix session loop, DB role inconsistency.

**May 18 (Sun):** Seller Dashboard rewrite (KPIs, tier badge). Partner Tier System (BRONZE/SILVER/GOLD/DIAMOND). StoreService.upgradePartnerTiers() @Scheduled. common/seller/bottom-nav. Fix getCurrentUser().

**May 19 (Mon):** Deal Banner Fallback. Deal Edit (Admin). Deal Pause/Resume (Admin + Seller). Seller Deal Detail Popup. Deal Tabs fixed.

**May 20 (Tue):** Seller Product CRUD (List/Create/Detail/Edit/Toggle). Seller Bottom Nav. Fix JS product.price→originalPrice.

**May 21 (Wed):** Seller Finance + Wallet (balance, APIs: wallet/transactions/request-payout/edit-payout/cancel-payout). TransactionRepository 5 store queries.

**May 22 (Thu):** Seller Profile (inline edit, logo, bank, password). Seller Analytics (6 KPI cards, bar+donut charts, 6 recommendations). Bottom nav thêm "Phân tích" (7 tabs).

**May 23 (Fri):** Store Page Builder (7 draggable sections, toggle, reorder, preview). Dynamic buyer store page `/store/{storeId}`. Logout redirect fix.

### Week 5: Buyer Flow & Payment (May 24 – 30)

**May 24 (Sat):** Product: rename BLIND_BOX→COMBO, multi-select, auto-calc. Quick Push (auto-bundle 3 SP, 35% off). currentPrice + ExpiryCountdownService @Scheduled. Delete PENDING Products.

**May 25 (Sun):** Staff pages: dashboard (slot-based 3 queues), orders (status update + QR), products (create + detail), profile (avatar, password, weakPassword popup).

**May 26 (Mon):** Product.createdBy (FK + migration V7). Weak Password System (flag + migration V8). Staff product: image validation, combo. Order detail popup (items + deadline timer).

**May 27 (Tue):** Buyer Home rewrite (product-centric): hero video + stats DB, Flash Sale countdown + grid, Deal Nổi Bật banner, Combo section, categories grid, trust section.

**May 28 (Wed):** Category Page redesign (Shopee-style): partner banners 6 màu, search debounce, store chips, price chips 5 mức, compact auto-fill grid.

**May 29 (Thu):** Deal Detail + Product Detail pages. Product.isAvailable() thêm approvalStatus. ProductRepository thêm filter vào 4 queries.

**May 30 (Fri):** Cart System (HttpSession): 5 API endpoints, cart page (+/-/remove/checkout/voucher). Checkout page (group by store, stepper). Payment page. Order Complete (confetti + pickup guide).

### Week 6: PayOS, Map & Polish (May 31 – Jun 5)

**May 31 (Sat):** Order tracking (6 tabs, JS filter). Buyer Profile (green header, stats, recent orders). Search page. Store Profile page.

**Jun 1 (Sun):** PayOSService (createPaymentLink, checkPaymentStatus, confirmPaymentAndDeductStock exactly-once, processWebhook, calculatePlatformFee). PaymentController (create/webhook/status). QR pickup flow.

**Jun 2 (Mon):** CartService (migrate session→DB, add/remove/update/getData/getCheckout/clear). Cart entity DB (Cart, CartItem, repos, migration V8). Cart page: stock validation, unavailable→grey out. FIFO lock "CART".

**Jun 3 (Tue):** Payment UX (xóa Cash, Credit Card disabled, Bank Transfer default). Order Complete (payment badge + QR + OTP). Session JDBC persistence + ResourceLockManager fair mode + single-session enforcement.

**Jun 4 (Wed):** Deal Map: Leaflet + OpenStreetMap, custom markers (urgency colors), bottom sheet, filter panel, category pills. Auto-locate GPS, dynamic position, distance, radius circle. Floating controls. Shared header + mobile-nav.

**Jun 5 (Thu):** Notification System (Service + 4 API + bell popup + auto-refresh). Search Overlay Shopee-style (suggest API + dropdown). Search Page (filter bar + compact cards). Profile edit (info, password, avatar, address book). UserAddress entity + migration V9. Vietnamese fixes (search.html 13 lỗi, store-profile.html 8 lỗi). BA Docs + Dev Timeline.

### Module Summary

| Module | Started | Completed |
|--------|---------|-----------|
| Foundation | Apr 26 | Apr 28 |
| Auth | Apr 28 | May 5 |
| Admin | May 7 | May 16 |
| Moderator | May 8 | May 8 |
| Seller | May 18 | May 23 |
| Staff | May 25 | May 26 |
| Buyer | May 6 | Jun 4 |
| Payment | Jun 1 | Jun 1 |
| Cart DB | Jun 2 | Jun 2 |
| Map | Jun 4 | Jun 4 |
| Polish + Docs | Jun 5 | Jun 5 |

---

*Timeline tổng hợp từ git log (41 commits) + CLAUDE.md (80+ mục thay đổi) + phân tích toàn bộ codebase (200+ files).*
