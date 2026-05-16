# DealXanh - O2O Deal Marketplace Platform

## 📊 Project Status: **~82% Complete**

**Last Updated:** 2026-05-16 (end of day)
**Version:** 1.0.0-alpha

---

## 🎯 Project Overview

**DealXanh KHÔNG PHẢI là E-commerce truyền thống** như Shopee/Lazada.

Đây là **O2O (Online-to-Offline) Deal Marketplace** với business model khác biệt hoàn toàn:

### **Business Model**

```
E-commerce truyền thống:
Product → Price → User mua

DealXanh O2O Deal Model:
DEAL (Chương trình giảm giá)
  ↓
DealProduct / DealCategory (Gán vào deal)
  ↓  
Product / Category
  ↓
User mua với giá DEAL
```

---

## ✅ Implemented Features (Completed)

### **1. Authentication & Authorization (100%)**
- ✅ Google OAuth2 Login
- ✅ Email/Password Login
- ✅ Role-based Access Control (RBAC)
  - `ROLE_ADMIN` - Full access
  - `ROLE_MODERATOR` - Limited access
  - `ROLE_STORE_OWNER` - Store management
  - `ROLE_STORE_STAFF` - Staff operations
  - `ROLE_USER` - Buyer
- ✅ Registration (Buyer/Seller)
- ✅ Seller Onboarding (3-step process)

### **2. Admin Panel (~85%)**
- ✅ Dashboard với KPIs (GMV, Orders, Users, Stores)
- ✅ User Management (view, toggle active, delete)
- ✅ Store Verification (approve/reject sellers)
- ✅ Product Approval Workflow (approve/reject/toggle/delete)
- ✅ Deal Management (create, view, filter)
- ✅ Deal Category Assignment (platform deals)
- ✅ Deal Product Assignment (store deals)
- ✅ Order Management (view by store)
- ⚠️ Finance/Dispute (placeholder UI only)

### **3. Seller Panel (~70%)**
- ✅ Dashboard với deal statistics
- ✅ Store Profile Management
- ✅ Deal Creation (full form with validation)
- ✅ Deal Product Assignment (multi-select products)
- ✅ Deal Management (pause/resume)
- ⚠️ Order Fulfillment (UI exists, backend incomplete)
- ⚠️ Employee Management (placeholder)

### **4. Buyer Interface (~60%)**
- ✅ Home Page với deals
- ✅ Store Profile
- ✅ Deal Detail
- ✅ Cart
- ✅ Order Confirmation
- ✅ Payment Page
- ⚠️ Order Tracking (incomplete)
- ⚠️ User Profile (incomplete)

### **5. Deal System (100%)**

**Backend Core:**
- ✅ Deal Entity với full fields
- ✅ DealProduct Entity (gán products vào deals)
- ✅ DealCategory Entity (gán categories vào deals)
- ✅ DealService với comprehensive validation
- ✅ DealRepository với advanced queries

**Deal Types Supported:**
- `FLASH_SALE` - Flash Sale (tối đa 6 giờ)
- `VOUCHER` - Phiếu giảm giá (tối đa 30 ngày)
- `FREESHIP` - Miễn phí vận chuyển (tối đa 30 ngày)
- `COMBO` - Combo giảm giá (tối đa 15 ngày)
- `SEASONAL` - Sale theo mùa (tối đa 30 ngày)

**Apply Methods:**
- `CODE_REQUIRED` - User cần nhập mã
- `AUTO_APPLY` - Giảm giá tự động

**Discount Types:**
- `PERCENT` - Giảm theo % (1% - 85%)
- `FIXED` - Giảm tiền cố định (1,000đ - 500,000đ)

### **6. Product System (90%)**
- ✅ Product Entity với approval workflow
- ✅ Category Management
- ✅ Product Approval (PENDING/APPROVED/REJECTED)
- ✅ ProductService với soft delete
- ✅ Advanced filtering & search

### **7. Store System (85%)**
- ✅ Store Entity với verification workflow
- ✅ Store Status (PENDING/ACTIVE/REJECTED)
- ✅ Store Profile (logo, banner, address)
- ✅ Owner + Staff relationship

### **8. Order System (~50%)**
- ✅ Order Entity
- ✅ OrderItem Entity
- ✅ Order Repository với statistics queries
- ⚠️ Order processing workflow incomplete
- ⚠️ Payment integration missing

### **9. Database & Migrations (100%)**
- ✅ Flyway migrations (V1 - V4)
- ✅ Soft delete pattern
- ✅ Proper indexing
- ✅ Foreign key constraints

---

## 🏗️ Architecture

### **Entity Relationships**

```
Deal (1) ←→ (N) DealCategory ←→ (1) Category
Deal (1) ←→ (N) DealProduct ←→ (1) Product
Deal (N) ←→ (1) Store
Deal (N) ←→ (1) User (createdBy)

Product (N) ←→ (1) Store
Product (N) ←→ (1) Category

Order (N) ←→ (1) Store
Order (N) ←→ (1) User (buyer)

Store (1) ←→ (1) User (owner)
Store (1) ←→ (N) User (staff)
```

### **Tech Stack**

**Backend:**
- Spring Boot 3.2.3
- Java 17
- Spring Security + OAuth2
- JPA/Hibernate with MySQL
- Thymeleaf template engine

**Frontend:**
- Thymeleaf fragments
- CSS variables design system
- Real-time search với debounce
- AJAX form submission

**Database:**
- MySQL (dealxanh_db)
- Flyway migrations

---

## 📐 Coding Rules & Patterns

### **1. Fragment Architecture**

**✅ LUÔN LUÔN SỬ DỤNG:**
```html
<div th:replace="~{common/admin/admin-layout :: layout(
    activeSidebar='page-name',
    pageTitle='Page Title',
    showDatePicker=false
)}">
    <!-- Page content -->
</div>
```

**❌ KHÔNG BAO GIỜ:**
- Hardcoded sidebar
- Hardcoded CSS
- Duplicate layout structure

### **2. No Code Duplication**

**✅ LUÔN LUÔN:**
- Dùng CSS fragment: `<div th:replace="~{common/admin/css :: css}"></div>`
- Dùng sidebar fragment: `<div th:replace="~{common/admin/sidebar :: sidebar('active')}"></div>`

### **3. Service Layer Pattern**

**✅ GOOD:**
```java
service.addCategoryToDeal(dealId, categoryId, priority)
service.removeCategoryFromDeal(dealId, categoryId)
service.addProductToDeal(dealId, productId, originalPrice, salePrice, maxQuantity, priority)
```

**❌ BAD:**
```java
repository.save(new DealCategory(...))
```

### **4. Repository Naming Convention**

```java
// ✅ GOOD
findByDealAndCategory(Deal deal, Category category)
findByStoreStoreIdAndDeletedFalse(Long storeId)
countByDeletedFalseAndActive(Boolean active)

// ❌ BAD
getByDealIdAndCategoryId(Long dealId, Long categoryId)
findActiveAndNotDeleted()
```

### **5. REST API Response Pattern**

```java
// ✅ GOOD
return Map.of(
    "success", true,
    "message", "Đã gán danh mục",
    "data", result
);

// ❌ BAD
return result; // No metadata
```

---

## 🚫 Quy tắc QUAN TRỌNG

### **1. KHÔNG hardcode sidebar**
- ❌ `<aside class="sidebar">...</aside>`
- ✅ `<div th:replace="~{common/admin/sidebar :: sidebar('active')}"></div>`

### **2. KHÔNG duplicate CSS**
- ❌ `<style>:root{...}</style>` trong mỗi file
- ✅ `<div th:replace="~{common/admin/css :: css}"></div>`

### **3. KHÔNG xóa dữ liệu thật**
- ❌ `DELETE FROM products`
- ✅ `UPDATE products SET deleted = true`

### **4. KHÔNG bypass service layer**
- ❌ Controller gọi repository trực tiếp
- ✅ Controller → Service → Repository

### **5. KHÔNG dùng @ManyToMany trực tiếp**
- ❌ `@ManyToMany List<Category> categories`
- ✅ Entity trung gian (`DealCategory`, `DealProduct`)

### **6. LUÔN check CSRF trước khi thêm/triển khai tính năng mới**
- ❌ Không check `SecurityConfig.java` → endpoint mới bị CSRF chặn → lỗi 500/403 khó debug
- ✅ Trước khi code: mở `SecurityConfig.java` → kiểm tra `csrf.ignoringRequestMatchers()` xem endpoint mới có cần thêm vào không
- ✅ Nếu endpoint cần POST/PUT/DELETE: thêm vào danh sách ignore HOẶC gửi đúng CSRF token (`_csrf.parameterName` = `_csrf`, không phải `_csrf.headerName`)
- ✅ Pattern trong ignore list dùng AntPathMatcher: `/admin/path/*/action`

---

## 🔐 Security & Permissions

### **Role-based Access:**

| Role | Admin Panel | Seller Panel | Buyer Panel |
|------|-------------|--------------|-------------|
| ADMIN | ✅ Full | ❌ | ✅ |
| MODERATOR | ✅ Limited | ❌ | ✅ |
| STORE_OWNER | ❌ | ✅ Full | ✅ |
| STORE_STAFF | ❌ | ✅ Limited | ✅ |
| USER | ❌ | ❌ | ✅ |

### **Always use:**
```java
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
@PreAuthorize("hasRole('STORE_OWNER')")
```

---

## 🎨 Design System

### **CSS Variables:**
```css
--primary: #0d5c2e
--accent: #d4af37
--surface: #fff
--bg: #f0f2f5
--text-primary: #1a1a1a
--text-secondary: #666
--border: #e0e0e0
--danger: #ef4444
--warning: #f59e0b
--success: #10b981
--info: #3b82f6
--radius-md: 10px
```

---

## 📁 File Structure

```
src/main/java/com/dealxanh/app/
├── entity/ (15 entities)
│   ├── Deal.java ✅
│   ├── DealProduct.java ✅
│   ├── DealCategory.java ✅
│   ├── Product.java ✅
│   ├── Category.java ✅
│   ├── Store.java ✅
│   ├── User.java ✅
│   ├── Order.java ✅
│   └── ... (7 more)
├── repository/ (13 repositories)
│   ├── DealRepository.java ✅
│   ├── DealProductRepository.java ✅
│   ├── DealCategoryRepository.java ✅
│   └── ... (10 more)
├── service/ (5 services)
│   ├── DealService.java ✅ (FULL VALIDATION)
│   ├── ProductService.java ✅
│   └── ... (3 more)
└── controller/
    ├── admin/
    │   └── AdminController.java ✅ (1500+ lines)
    ├── SellerController.java ✅
    └── ... (4 more)

src/main/resources/templates/
├── common/admin/
│   ├── admin-layout.html ✅
│   ├── sidebar.html ✅
│   ├── header.html ✅
│   └── css.html ✅
├── admin/ (10 pages)
│   ├── dashboard.html ✅
│   ├── deals.html ✅ (UPDATED - Product Assignment)
│   ├── products.html ✅
│   ├── users.html ✅
│   ├── seller-verify.html ✅
│   └── ... (5 more)
├── seller/ (4 pages)
│   ├── dashboard.html ✅
│   ├── deals.html ✅
│   ├── create-deal.html ✅
│   └── ... (1 more)
└── buyer/ (15+ pages)
    ├── home.html ✅
    └── ... (14 more)
```

---

## 🔄 Workflow

### **Admin Workflow (Platform Deals):**

```
1. Admin tạo Deal
   - Chọn Deal Type (Flash Sale, Voucher, etc.)
   - Set discount (type, value, max amount)
   - Set time range
   - Choose scope (ALL_STORES or SPECIFIC_STORES)
   - Choose apply method (CODE/AUTO)

2. Admin gán CATEGORIES vào Deal (Platform Deals)
   - Multi-select categories
   - Set priority
   - TẤT CẢ products trong categories được áp dụng deal

3. Hoặc: Admin gán PRODUCTS vào Deal (Store Deals)
   - Chọn deal có store
   - Multi-select products từ store đó
   - Set deal price per product
   - Set max quantity

4. Deal active → Products hiển thị giá deal
```

### **Seller Workflow (Store Deals):**

```
1. Seller tạo Deal cho store
   - Không cần approval
   - Tạo xong là SCHEDULED

2. Seller gán PRODUCTS vào Deal
   - Multi-select products từ store
   - Set deal price per product
   - Set max quantity per product
   - Priority sorting

3. Deal active → Chỉ selected products hiển thị giá deal
```

---

## ⚠️ Known Issues & Fixes

### **Recent Fixes (2026-05-16):**

1. **Admin User Management - Edit & Quality Rating**
   - ✅ Added: Edit user modal với form sửa thông tin + hiển thị store info nếu là seller
   - ✅ Added: Cột "Chất lượng" trong bảng users hiển thị rating + badge (Kém/Ổn/Tốt)
   - ✅ Added: Auto-disable seller nếu store có >= 40 đánh giá & rating <= 2.5
   - ✅ Removed: Nút xoá tài khoản + backend delete endpoint

2. **Seller Verification - Reset to Pending**
   - ✅ Added: Nút "Quay lại chờ xét duyệt" cho store REJECTED trong detail panel
   - ✅ Added: Popup confirm với lý do từ chối fill sẵn (có thể sửa)
   - ✅ Added: Gửi email thông báo cho seller khi store được xét duyệt lại
   - ✅ Added: Endpoint `POST /admin/seller-verify/{id}/reset-to-pending`
   - ✅ Fixed: CSRF ignore cho endpoint mới trong SecurityConfig
   - ✅ Fixed: Doc link không có ảnh → disabled (pointer-events:none, opacity 0.4)

3. **Deal Management - Major Improvements**
   - ✅ Removed: FREESHIP deal type (mô hình pickup không cần freeship)
   - ✅ Added: Auto-fill fields theo deal type (Flash Sale→AUTO_APPLY+20%, Voucher→FIXED+20k, Combo→PERCENT+15%, Seasonal→PERCENT+25%)
   - ✅ Added: Banner upload (file ảnh) thay vì URL text input, có preview + validate JPG/PNG/WEBP max 5MB
   - ✅ Added: Validate sản phẩm không được nằm trong 2 deal chồng lấn thời gian (`validateProductNotInOverlappingDeal`)
   - ✅ Added: Scope hint hướng dẫn gán danh mục/sản phẩm sau khi tạo deal

4. **CSRF Bug Fix**
   - ❌ `reset-to-pending` endpoint bị lỗi 500 vì chưa được thêm vào CSRF ignore list
   - ✅ Fixed: Thêm pattern `/admin/seller-verify/*/reset-to-pending` vào `SecurityConfig.ignoringRequestMatchers`

5. **Remove Coupon Entity (Dead Code)**
   - ✅ Deleted: `Coupon.java` entity + `CouponRepository.java` (không template nào dùng, Deal VOUCHER thay thế hoàn toàn)
   - ✅ `Order.couponCode` giữ lại dưới dạng String (lưu mã deal đã áp dụng)

6. **Buyer Home Page - Display Deal Categories & Products**
   - ✅ HomeController cung cấp real data (`featuredDeals`) từ DB thay vì dummy
   - ✅ Deal card hiển thị: danh mục áp dụng (cột dọc, chấm tròn vàng), sản phẩm áp dụng (hàng ngang, max 2 + ", +N sản phẩm khác...")
   - ✅ Mỗi deal card có thêm deal type badge (Flash Sale/Voucher/Combo/Seasonal)

7. **Max 65% Discount Validation**
   - ✅ `MIN_SALE_PRICE_PERCENT` = 0.35 (giá sale >= 35% giá gốc, tương đương giảm tối đa 65%)
   - ✅ `validateCumulativeDiscount()`: Kiểm tra nếu sản phẩm thuộc danh mục có platform deal → cộng dồn discount, nếu vượt 65% → báo lỗi "Sản phẩm đã giảm giá kịch sàn, không thể giảm hơn"

8. **Admin Deal Cards - Categories & Products Panel**
   - ✅ Card 2 cột: trái (main info) + phải 140px (danh mục dọc + sản phẩm ngang max 2 + ", +N SP")
   - ✅ AdminController build `dealCategoriesMap` + `dealProductsMap` từ DB
   - ✅ Fix grid min-width 320px → 370px để vừa panel phải
   - ✅ Fix modal Xem DM/SP: empty state thay vì auto-redirect
   - ✅ Fix modal Thêm DM/SP: empty state thay vì spinner vô hạn

9. **Finance & Reconciliation (Tài chính & Đối soát) — Xây dựng hoàn chỉnh**
   - ✅ `TransactionRepository` với 14 query methods (sumGMV, sumCommission, sumPaidPayouts, sumGMVByDate, findPendingPayoutsWithStore...)
   - ✅ Trang `/admin/finance`: KPI cards động, chart 7 ngày, bảng chờ payout, bảng giao dịch gần đây
   - ✅ Date range filter (2 ô input[date]), Xuất CSV (BOM UTF-8), Chạy Payout (modal confirm → API POST)
   - ✅ **Ví nền tảng**: popup số dư = Commission - PaidOut - Pending
   - ✅ **Đối soát**: modal từng dòng store, xác nhận riêng + nút undo ↩, counter (3/5), commit tổng
   - ✅ Dùng chung header fragment như các trang khác

10. **Dispute Management (Khiếu nại & Tranh chấp) — Xây dựng hoàn chỉnh**
    - ✅ `DisputeRepository` với queries JOIN FETCH complainant + order
    - ✅ Trang `/admin/dispute`: KPI cards lọc theo status, danh sách disputes, panel chi tiết bên phải
    - ✅ Flow: PENDING → "Bắt đầu xem xét" → REVIEWING → "Phê duyệt hoàn tiền" / "Từ chối" (có prompt nhập ghi chú)
    - ✅ Đã xử lý → ẩn action buttons, hiển thị admin note

11. **Analytics & Reports (Báo cáo & Phân tích) — Enterprise Dashboard**
    - ✅ `OrderRepository` thêm 8 analytics queries (sumRevenueByDate, topStoresByRevenue, orderStatusDistribution...)
    - ✅ Trang `/admin/analytics`: 4 KPI cards (% change vs kỳ trước), revenue trend chart 30 ngày (bar), top 10 stores
    - ✅ **Hệ thống Đề xuất thông minh**: 8 loại cảnh báo (dispute pending, huỷ đơn cao, store chờ duyệt, deal hết hạn, GMV giảm, rating thấp...) → mỗi đề xuất có nút hành động
    - ✅ Metrics: tỷ lệ pickup (vòng tròn), tỷ lệ huỷ, sức khoẻ nền tảng (progress bars)

12. **Profile Security - Password Validation**
    - ✅ **Backend fix**: BCrypt `matches()` để validate mật khẩu hiện tại + `encode()` để lưu mật khẩu mới (trước đó so sánh plain text → luôn fail!)
    - ✅ **Frontend**: strength meter 4 thanh (đỏ→vàng→cam→xanh), real-time, check confirm match, chặn submit nếu không khớp/trùng

13. **Session Expiration — Role-based Redirect**
    - ✅ `CustomInvalidSessionStrategy`: `/admin/**` → `/admin/login?expired=true`, `/seller/**` → `/seller/login?expired=true`, còn lại → `/login?expired=true`
    - ✅ AJAX request → 401 JSON
    - ✅ Cả 3 trang login hiển thị message "Phiên đăng nhập đã hết hạn"
    - ✅ Session timeout: 30 phút

### **Recent Fixes (2026-05-13):**

1. **Thymeleaf Template Error**
   - ❌ `#temporals.duration()` không tồn tại
   - ✅ Fixed: Use `#temporals.day()` instead

2. **Repository Method Naming**
   - ❌ `findByStore_StoreId` (underscore)
   - ✅ Fixed: `findByStoreStoreId` (no underscore)

3. **User Entity Method**
   - ❌ `user.getStore()` không tồn tại
   - ✅ Fixed: `user.getWorkStore()`

4. **Missing Repository Methods**
   - ✅ Added: `findByStoreStoreId(Long storeId)` in DealRepository
   - ✅ Added: `findByStoreStoreIdAndDeletedFalse(Long storeId)` in ProductRepository

---

## 🚀 Pending Features (TODO)

### **High Priority:**

1. **Order Processing Workflow** (~40%)
   - ✅ Order Entity exists
   - ⚠️ Order status transitions incomplete
   - ⚠️ Payment integration missing
   - ⚠️ Shipping/tracking incomplete

2. **Finance, Dispute & Analytics** (~90%)
   - ✅ Finance: KPI, chart, transactions, CSV export, payout, wallet, reconciliation
   - ✅ Dispute: list, detail panel, review flow, approve/reject
   - ✅ Analytics: enterprise dashboard, recommendations engine, top stores
   - ⚠️ Payout chưa tích hợp cổng thanh toán thật

3. **Seller Order Fulfillment** (~30%)
   - ⚠️ UI exists
   - ❌ Backend incomplete
   - ❌ QR code pickup incomplete

### **Medium Priority:**

4. **Buyer Order Tracking** (~40%)
   - ✅ Order detail pages exist
   - ⚠️ Real-time status updates missing
   - ⚠️ Driver tracking incomplete

5. **Notification System** (~20%)
   - ✅ Notification Entity exists
   - ❌ Email notifications incomplete
   - ❌ Push notifications missing

6. **Review & Rating System** (~30%)
   - ✅ Review Entity exists
   - ⚠ UI incomplete
   - ❌ Backend incomplete

### **Low Priority:**

7. **Analytics Dashboard** (~90%)
   - ✅ Enterprise dashboard: KPIs, revenue chart, top stores, metrics
   - ✅ AI Recommendations engine: 8 loại cảnh báo thông minh
   - ✅ Export CSV cho finance
   - ⚠️ Chưa có export PDF/Excel report

8. **Mobile App** (Not started)

---

## 📝 Validation Rules (Implemented)

### **DealService Validation:**

| Rule | Value |
|------|-------|
| Max discount PERCENT | 85% |
| Min discount PERCENT | 1% |
| Min discount FIXED | 1,000đ |
| Max discount FIXED | 500,000đ |
| Min sale price | 15% of original |
| Max deal duration | 30 days |
| Flash Sale max | 6 hours |
| Voucher max | 30 days |
| Freeship max | 30 days |
| Combo max | 15 days |

### **Deal Code Validation:**
- Format: `[A-Z0-9_-]+` only
- Must be unique (excluding CANCELLED deals)

### **Time Overlap Detection:**
- ✅ Validates no overlapping deals for same store
- ✅ Returns list of overlapping deal names

### **Seller Eligibility:**
- ✅ Store must have approved products before creating deals

---

## ✅ Checklist trước khi commit code

- [ ] Không hardcoded sidebar/header
- [ ] Dùng fragment pattern đúng
- [ ] Không duplicate CSS
- [ ] Có service layer cho business logic
- [ ] Dùng soft delete
- [ ] Preserve URL parameters
- [ ] Test cả happy path và edge cases
- [ ] Check permissions (@PreAuthorize)
- [ ] Run migration SQL

---

## 📊 Completion Status by Module

| Module | Backend | Frontend | Testing | Overall |
|--------|---------|----------|---------|---------|
| Authentication | 100% | 100% | 90% | **95%** |
| Admin Panel | 92% | 88% | 70% | **87%** |
| Seller Panel | 82% | 72% | 50% | **72%** |
| Buyer Interface | 70% | 60% | 40% | **60%** |
| Deal System | 100% | 98% | 85% | **98%** |
| Product System | 95% | 85% | 70% | **85%** |
| Store System | 92% | 85% | 60% | **82%** |
| Order System | 60% | 50% | 30% | **50%** |
| Payment System | 20% | 30% | 10% | **20%** |
| Notification | 50% | 40% | 20% | **40%** |
| Buyer Interface | 75% | 65% | 40% | **65%** |

**Overall Project Completion: ~75%**

---

## 🎯 Milestones

- [x] **M1: Core Authentication** (Completed)
- [x] **M2: Admin Panel Basic** (Completed)
- [x] **M3: Deal System Core** (Completed)
- [x] **M4: Seller Dashboard** (Completed)
- [x] **M5: Product Approval** (Completed)
- [x] **M6: Deal Product Assignment** (Completed)
- [x] **M6.5: Admin User Management Enhancements** (Completed 2026-05-16)
- [x] **M6.6: Seller Verification Reset-to-Pending** (Completed 2026-05-16)
- [x] **M6.7: Deal Management Optimization** (Completed 2026-05-16)
- [ ] **M7: Order Processing** (In Progress - 50%)
- [ ] **M8: Payment Integration** (Not Started)
- [ ] **M9: Notification System** (Not Started)
- [ ] **M10: Analytics & Reports** (Not Started)

---

## 🐛 Bug Tracking

### **Fixed:**
1. ✅ Thymeleaf `#temporals.duration()` error
2. ✅ Repository method naming inconsistency
3. ✅ User `getStore()` vs `getWorkStore()` confusion
4. ✅ Missing repository methods

### **Known Issues:**
1. ⚠️ Order status transitions not fully implemented
2. ⚠️ Finance/dispute pages are placeholders
3. ⚠️ Mobile responsive needs improvement
4. ⚠️ Some modal close behaviors inconsistent

---

## 📞 Support

**Project:** DealXanh O2O Deal Marketplace
**Tech Lead:** [Your Name]
**Documentation:** CLAUDE.md (this file)
**Last Updated:** 2026-05-13

---

## 🔗 Related Documentation

- `CODE_DUPLICATION_ANALYSIS.md` - Sidebar duplication analysis
- `PROJECT_DEEP_DIVE.md` - Comprehensive project docs
- Memory System: `C:\Users\ACER\.claude\projects\d--exe201-deal-xanh\memory\`
