# DealXanh - O2O Deal Marketplace Platform

## 📊 Project Status: **~85% Complete**

**Last Updated:** 2026-05-19 (end of day)
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

### **3. Seller Panel (~92%)**
- ✅ Dashboard với KPIs + order list + tier badge (full rewrite 2026-05-17)
- ✅ Shared bottom navigation bar (common/seller/bottom-nav fragment, 7 tabs: Dashboard, Deals, Đơn hàng, Kho, Tài chính, Phân tích, Tôi)
- ✅ Store Profile Management (edit info, bank info, logo upload, password change)
- ✅ Store Page Editor (drag-and-drop section builder, cover image, description, hours, categories, pickup settings)
- ✅ Deal Creation (full form with validation)
- ✅ Deal Product Assignment (multi-select products)
- ✅ Deal Management (pause/resume with expired validation)
- ✅ Deal Detail Popup with actions
- ✅ Product Management (CRUD: list, create, edit, toggle, detail popup, delete pending)
- ✅ Combo product type (select individual products, auto-calculate price, HSD validation)
- ✅ Quick Push (Đẩy hàng nhanh) - auto-bundle 3 products by category into discounted combos
- ✅ Finance & Wallet (balance, transaction history, payout requests, reconciliation)
- ✅ Analytics & Recommendations (KPI cards, revenue bar+donut charts, order status, top products, smart alerts)
- ✅ Partner tier system (BRONZE/SILVER/GOLD/DIAMOND with commission rates)
- ✅ Expiry countdown service (auto-reduce currentPrice daily, deactivate on expiry)
- ✅ Order detail popup (items with quantity, auto-cancel timer, pickup deadline)
- ⚠️ Order Fulfillment (UI exists, backend incomplete)
- ⚠️ Employee Management (placeholder)

### **4. Buyer Interface (~70%)**
- ✅ Home Page với deals
- ✅ Dynamic Store Page (/store/{storeId}) — cover, logo, categories, deals, products grid
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

### **6. Product System (95%)**
- ✅ Product Entity với approval workflow
- ✅ Category Management
- ✅ Product Approval (PENDING/APPROVED/REJECTED)
- ✅ ProductService với soft delete
- ✅ Advanced filtering & search
- ✅ COMBO product type (bundle individual products into one listing)
- ✅ currentPrice field (expiry countdown display price)
- ✅ ExpiryCountdownService (@Scheduled daily, reduces price 7 days before HSD)
- ✅ Quick Push auto-bundle for expiring products

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

### **7. LUÔN dùng #numbers.formatDecimal(val, 0, 0) cho số trong Thymeleaf**
- ❌ `#numbers.formatInteger(doubleValue)` — chỉ nhận Integer, lỗi với Double
- ❌ `#numbers.formatDecimal(val, 0, 'COMMA', 0, 'POINT')` — Thymeleaf 3.1 không hỗ trợ
- ✅ `#numbers.formatDecimal(val, 0, 0)` — nhận Number, hoạt động với cả Integer và Double

### **8. LUÔN @JsonIgnore @OneToMany trong Entity**
- ❌ Jackson serialize @OneToMany lazy → LazyInitializationException hoặc infinite recursion → 500
- ✅ Mọi `@OneToMany` phải có `@JsonIgnore`
- ✅ Helper getter trả về primitive (`double`, `int`) phải có `@JsonIgnore` + null-safe

### **9. LUÔN dùng th:classappend không dùng th:class**
- ❌ `th:class="..."` ghi đè class gốc → mất style
- ✅ `th:classappend="..."` thêm class vào class gốc

### **10. LUÔN dùng pipe syntax \|...\| cho th:onclick**
- ❌ `th:onclick="'func(' + ${id} + ')'"` — lỗi escape với quote/nested expression
- ✅ `th:onclick="\|func(${id})\|"` — sạch, không cần escape

### **11. LUÔN dùng HashMap khi Map.of() > 10 cặp**
- ❌ `Map.of(k1,v1, k2,v2, ..., k11,v11)` — compile error
- ✅ `new HashMap<>()` + `.put()` khi có >10 key-value pairs

### **12. LUÔN check null/empty string trước khi parse date**
- ❌ `LocalDateTime.parse("" + "T00:00:00")` → DateTimeParseException
- ✅ `(str != null && !str.isEmpty()) ? LocalDateTime.parse(str + "T00:00:00") : default`

### **13. LUÔN thêm event.stopPropagation() vào button trong card**
- ❌ Click nút trong card → event bubble lên card → trigger sai handler
- ✅ Mọi button trong clickable card phải có `event.stopPropagation()`

### **14. LUÔN kiểm tra Jackson serialization trước khi tạo popup/API mới**
- ❌ Tạo API `@ResponseBody` trả entity → Jackson serialize → `Infinite recursion` hoặc `LazyInitializationException` → popup "không thể tải"
- ✅ Trước khi viết popup: trace toàn bộ path serialize từ entity gốc → tất cả `@OneToMany` (LAZY) phải có `@JsonIgnore`
- ✅ Check circular reference: `A → B → A` phải bị chặn bởi `@JsonIgnore` ít nhất 1 đầu
- ✅ Check helper getter trả về primitive (`double`, `int`): phải null-safe + `@JsonIgnore`
- ✅ Test popup ngay sau khi viết để phát hiện lỗi Jackson sớm

### **15. KHÔNG dùng nested single quotes trong th:text (Thymeleaf parse error)**
- ❌ `th:text="'Prefix: ' + #temporals.format(date, 'dd/MM')"` — single quote trong `'dd/MM'` đóng string `'Prefix: '` sớm → parse error
- ❌ `th:text="'Giá: ' + #numbers.formatDecimal(val, 0, 0) + 'đ'"` — tương tự với `'đ'`
- ✅ Dùng nested span: `<span>Prefix: <span th:text="${#temporals.format(date, 'dd/MM')}"></span></span>`
- ✅ Hoặc dùng pipe syntax cho toàn bộ: `th:text="|Prefix: ${#temporals.format(date, 'dd/MM')}|"`
- ✅ Pattern cần tránh: bất kỳ `th:text` nào có string concatenation (`+`) với string literal chứa single quote

### **16. LUÔN dùng debounce search real-time (không cần nút Tìm)**

**Pattern cho mọi trang seller/admin cần tìm kiếm:**

**Frontend (Thymeleaf + JS):**
```html
<!-- Search form: KHÔNG có nút submit, search tự động khi gõ -->
<form method="get" th:action="@{/seller/page}" class="search-form">
    <input type="hidden" name="status" th:value="${selectedStatus}">
    <input type="text" name="search" placeholder="Tìm kiếm..." th:value="${searchQuery}" oninput="debounceSearch()">
</form>

<script>
let searchTimer;
function debounceSearch() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
        document.querySelector('.search-form').submit();
    }, 300); // 300ms debounce — đủ nhanh để cảm giác real-time, đủ chậm để không spam request
}
</script>
```

**Quy tắc:**
- ✅ LUÔN dùng `<input oninput="debounceSearch()">` — tìm kiếm ngay khi gõ (kể cả 1 ký tự)
- ✅ Debounce 300ms để tránh spam request quá nhiều
- ✅ Form vẫn dùng method="GET" để giữ nguyên Thymeleaf server-side rendering
- ✅ Backend nhận `@RequestParam(required = false) String search` và filter kết quả
- ✅ KHÔNG cần nút "Tìm" submit — input tự động submit form sau 300ms
- ✅ Select/dropdown filter vẫn dùng `onchange="this.form.submit()"` để filter ngay khi chọn
- ✅ Link phân trang phải giữ nguyên tham số `search=${searchQuery}`

**Backend (Controller):**
```java
@GetMapping("/page")
public String page(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String status, ...) {
    // ... fetch data ...
    // Filter by search term
    if (search != null && !search.isEmpty()) {
        String s = search.toLowerCase().trim();
        results = results.stream()
            .filter(item -> /* match fields: name, code, ID, etc. */)
            .toList();
    }
    model.addAttribute("searchQuery", search);
    // ... pagination, return view ...
}
```

- ✅ Search filter áp dụng TRƯỚC khi phân trang (filter trên toàn bộ data rồi mới paginate)
- ✅ Khi có search query, nên fetch tối đa dữ liệu (hoặc `Pageable.unpaged()`) trước khi filter để đảm bảo kết quả đầy đủ
- ✅ `model.addAttribute("searchQuery", search)` để giữ lại giá trị search trong input sau khi submit

### **17. LUÔN validate file upload (loại file + kích thước) trước khi lưu**

**Backend: Tạo method `validateDocumentFile()` hoặc dùng chung helper:**

```java
private String validateDocumentFile(MultipartFile file, String docName) {
    // Check file size (max 5MB)
    long maxSize = 5 * 1024 * 1024;
    if (file.getSize() > maxSize) {
        return docName + ": File quá lớn (tối đa 5MB). Kích thước: "
            + String.format("%.1f", file.getSize() / (1024.0 * 1024.0)) + "MB";
    }
    // Check file type
    String contentType = file.getContentType();
    if (contentType == null) return docName + ": Không thể xác định loại file";
    boolean valid = contentType.equals("image/jpeg") || contentType.equals("image/png")
            || contentType.equals("image/webp") || contentType.equals("image/gif")
            || contentType.equals("application/pdf");
    if (!valid) return docName + ": Chỉ chấp nhận ảnh (JPG/PNG/WEBP) hoặc PDF. Loại hiện tại: " + contentType;
    return null; // OK
}

// Gọi trước khi save:
if (file != null && !file.isEmpty()) {
    String error = validateDocumentFile(file, "Tên giấy tờ");
    if (error != null) { redirectAttributes.addFlashAttribute("error", error); return "redirect:/..."; }
    store.setXxxUrl(saveUploadedFile(file));
}
```

**Quy tắc:**
- ✅ LUÔN validate `file.getSize() > maxSize` — từ chối file > 5MB
- ✅ LUÔN validate `contentType` — chỉ chấp nhận ảnh (JPEG, PNG, WEBP, GIF) hoặc PDF
- ✅ LUÔN validate TRƯỚC khi gọi `saveUploadedFile()` — không lưu file không hợp lệ
- ✅ LUÔN trả về thông báo lỗi tiếng Việt cụ thể (tên giấy tờ + lý do + thông số thực tế)
- ✅ `saveUploadedFile()` dùng MD5 hash làm tên file để tự động dedup (cùng nội dung = cùng file, không ghi đè)

**Frontend hints:**
```html
<input type="file" name="xxxFile" accept="image/*,.pdf">
<div class="pw-hint">JPG, PNG, WEBP hoặc PDF · Tối đa 5MB</div>
```

**Seller profile script mẫu cho document preview (dùng `data-url` thay vì `th:onclick`):**

```javascript
// ===== DOCUMENT PREVIEW =====
function previewDoc(url) {
    const ext = (url || '').split('.').pop().toLowerCase();
    const imgExts = ['jpg','jpeg','png','gif','webp','bmp','svg'];
    if (imgExts.includes(ext)) {
        document.getElementById('docPreviewImg').src = url;
        document.getElementById('docPreviewImg').style.display = 'block';
        document.getElementById('docPreviewPdf').style.display = 'none';
    } else {
        document.getElementById('docPreviewImg').style.display = 'none';
        document.getElementById('docPreviewPdf').style.display = 'block';
        document.getElementById('docPreviewLink').href = url;
    }
    document.getElementById('docPreviewModal').style.display = 'flex';
}
function closeDocPreview(e) {
    if (e && e.target !== e.currentTarget) return;
    document.getElementById('docPreviewModal').style.display = 'none';
    document.getElementById('docPreviewImg').src = '';
}
```

```html
<!-- Nút xem trong view mode: dùng data-url + onclick, không dùng th:onclick -->
<button type="button" th:data-url="${store.cccdUrl}" onclick="previewDoc(this.dataset.url)">📄 Xem</button>

<!-- Modal preview -->
<div id="docPreviewModal" class="modal-overlay" style="display:none;" onclick="closeDocPreview(event)">
    <div class="modal-box" style="max-width:90vw;padding:12px;text-align:center;" onclick="event.stopPropagation()">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px;">
            <span style="font-size:14px;font-weight:700;">Xem tài liệu</span>
            <button onclick="closeDocPreview()" style="width:28px;height:28px;border-radius:50%;background:var(--bg);border:none;cursor:pointer;font-size:16px;">✕</button>
        </div>
        <img id="docPreviewImg" src="" style="max-width:100%;max-height:70vh;border-radius:var(--radius-md);display:none;" onerror="this.style.display='none';document.getElementById('docPreviewPdf').style.display='block';">
        <div id="docPreviewPdf" style="display:none;padding:40px 20px;text-align:center;">
            <p style="font-size:14px;font-weight:600;margin-bottom:8px;">Không thể xem trước file này</p>
            <a id="docPreviewLink" href="#" target="_blank" class="btn btn-primary" style="display:inline-flex;">Mở trong tab mới</a>
        </div>
    </div>
</div>
```

- ❌ KHÔNG dùng `th:onclick="|func('${var}')|"` với string variable — Thymeleaf 3.1 chặn
- ✅ Dùng `th:data-url="${var}" onclick="func(this.dataset.url)"` — an toàn, tương thích

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
├── config/
│   └── SecurityConfig.java ✅
├── entity/ (15 entities)
│   ├── Deal.java ✅
│   ├── DealProduct.java ✅
│   ├── DealCategory.java ✅
│   ├── Product.java ✅ (currentPrice, COMBO type)
│   ├── Category.java ✅
│   ├── Store.java ✅
│   ├── User.java ✅
│   ├── Order.java ✅
│   ├── OrderItem.java ✅
│   ├── Transaction.java ✅
│   └── ... (5 more)
├── repository/ (15 repositories)
│   ├── DealRepository.java ✅
│   ├── DealProductRepository.java ✅
│   ├── DealCategoryRepository.java ✅
│   ├── ProductRepository.java ✅ (store-level wallet queries)
│   ├── TransactionRepository.java ✅ (store aggregate queries + combo)
│   └── ... (10 more)
├── service/ (6 services)
│   ├── DealService.java ✅ (FULL VALIDATION)
│   ├── ProductService.java ✅
│   ├── StoreService.java ✅
│   ├── ExpiryCountdownService.java ✅ (NEW - daily price reduction)
│   └── ... (2 more)
└── controller/
    ├── admin/
    │   └── AdminController.java ✅ (1500+ lines)
    ├── auth/
    │   └── AuthController.java ✅ (logout role-based redirect)
    ├── SellerController.java ✅ (1800+ lines - analytics, store-page, quick-push)
    ├── HomeController.java ✅ (dynamic /store/{storeId})
    └── ... (2 more)

src/main/resources/
├── db/migration/
│   ├── V2__add_product_approval.sql
│   ├── V3__create_deal_categories.sql
│   ├── V3__add_apply_method_to_deals.sql
│   ├── V4__add_deal_validations.sql
│   ├── V5__add_partner_tier.sql
│   └── V6__add_current_price.sql ✅ (NEW)
├── templates/
│   ├── common/admin/
│   │   ├── admin-layout.html ✅
│   │   ├── sidebar.html ✅
│   │   ├── header.html ✅
│   │   └── css.html ✅
│   ├── common/seller/
│   │   └── bottom-nav.html ✅ (7 tabs)
│   ├── admin/ (10 pages)
│   │   ├── dashboard.html ✅
│   │   ├── deals.html ✅
│   │   ├── products.html ✅
│   │   ├── users.html ✅
│   │   ├── seller-verify.html ✅
│   │   ├── finance.html ✅
│   │   ├── analytics.html ✅
│   │   └── ... (3 more)
│   ├── seller/ (8 pages)
│   │   ├── dashboard.html ✅
│   │   ├── deals.html ✅
│   │   ├── create-deal.html ✅
│   │   ├── orders.html ✅ (detail popup)
│   │   ├── products.html ✅ (quick-push, delete PENDING)
│   │   ├── create-product.html ✅ (COMBO type)
│   │   ├── finance.html ✅ (wallet + reconciliation)
│   │   ├── analytics.html ✅ (multi-chart, recommendations)
│   │   ├── profile.html ✅ (store page link, password change)
│   │   └── store-page.html ✅ (drag-drop builder)
│   └── buyer/ (16+ pages)
│       ├── home.html ✅
│       ├── store.html ✅ (NEW - dynamic store page)
│       └── ... (14 more)
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

### **Recent Fixes (2026-05-19):**

1. **Seller Finance & Wallet — Full Implementation**
   - ✅ Rewrote /seller/finance page: wallet card (balance, earned, commission, paid out, pending)
   - ✅ Added APIs: GET wallet, GET transactions (type/date filter), POST request-payout, POST edit-payout, POST cancel-payout
   - ✅ TransactionRepository: 5 new store-level aggregate queries (sumEarnedByStore, sumCommissionByStore, sumPaidOutByStore, sumPendingPayoutByStore, findByStoreAndType)
   - ✅ CSRF: /seller/api/finance/** added to ignore list

2. **Seller Profile — Edit & Password Change**
   - ✅ Inline edit forms for store info (name, address, bank, logo upload) and bank info
   - ✅ Password change modal with BCrypt validation + strength meter (4 bars)
   - ✅ Added "Trang cửa hàng" card linking to store page builder
   - ✅ CSRF: /seller/profile/update-store, /seller/profile/change-password

3. **Seller Analytics — Full Dashboard**
   - ✅ New page: /seller/analytics with 6 KPI cards, dual bar chart (revenue + orders), donut chart (order status), horizontal bar (top products)
   - ✅ Smart recommendations: low stock, expiring deals, pending products, inactive products, tier upgrade, no active deals
   - ✅ APIs: overview, chart (14 days), top-products, order-stats, recommendations
   - ✅ Bottom nav: added "Phân tích" tab (7 tabs total)

4. **Seller Store Page Builder — Drag & Drop**
   - ✅ New page: /seller/store-page with 7 draggable sections (cover, info, description, categories, deals, products, hours)
   - ✅ Each section: toggle on/off, expand to edit, mini preview
   - ✅ HTML5 drag-and-drop reordering, cover image upload with preview
   - ✅ Dynamic buyer store page: /store/{storeId} (HomeController) → templates/buyer/store.html

5. **Seller Order Detail Popup — Items & Timer**
   - ✅ API: GET /seller/api/orders/{orderId}/detail returns items with quantity, prices, pickup deadline, auto-cancel timer
   - ✅ Order card shows "📋 Chi tiết" button → modal with item breakdown + deadline bar (green/yellow/red)

6. **Logout Redirect Fix**
   - ✅ AuthController GET /logout now checks Referer header: /admin → /admin/login, /seller → /seller/login, default → /login

7. **Product: Blind Box → Combo + Quick Push**
   - ✅ Renamed BLIND_BOX → COMBO in Product entity, create-product form, ProductRepository queries
   - ✅ Combo creation: multi-select products, auto-calculate price = average, HSD warning (≤3 days), confirmation checkbox
   - ✅ API: GET /seller/api/products/combo-available, POST /seller/api/products/quick-push
   - ✅ Quick Push banner on products page: auto-detect expiring products, group by category, one-click bundle 3 products into discounted combo (35% off)
   - ✅ Combo price auto-fills from average of selected products, HSD takes earliest among bundled items

8. **Product: currentPrice + Expiry Countdown**
   - ✅ Added currentPrice field to Product entity + V6 Flyway migration
   - ✅ ExpiryCountdownService: @Scheduled daily at 1AM, reduces currentPrice linearly over 7 days before HSD
   - ✅ On expiry day: currentPrice = 0, active = false. No conflict with deals (uses separate field)

9. **Product: Delete PENDING Products**
   - ✅ API: POST /seller/api/products/{productId}/delete (soft-delete, only PENDING status)
   - ✅ Delete button in product detail popup (red, appears only for PENDING)
   - ✅ Confirm dialog before deletion → page reloads

10. **Scroll & Layout Fixes**
    - ✅ Unified padding-bottom: 80px + html{height:auto;min-height:100%} across all 6 seller pages
    - ✅ Added missing .bottom-nav CSS to create-product.html
    - ✅ Root cause: shared css.html sets html,body{height:100%} — overridden in all seller pages

11. **Thymeleaf Quote Bug Fix**
    - ❌ `th:text="'HSD: ' + #temporals.format(product.expiryDate, 'dd/MM')"` — nested single quotes break parser
    - ✅ Use nested span: `<span>HSD: <span th:text="${#temporals.format(...)}"></span></span>`

12. **CLAUDE.md Rule: Layout Checklist**
    - ✅ Added rule #0: before coding any new page, verify bottom-nav/sidebar fragment + CSS + header + footer are included

### **Recent Fixes (2026-05-18):**

1. **Deal Banner Fallback Image**
   - ✅ Tạo `/img/default-banner.svg` làm ảnh mặc định khi deal chưa có banner
   - ✅ Thêm `onerror="this.src='/img/default-banner.svg'"` cho tất cả thẻ `<img>` trong admin/seller deals

2. **Deal Edit (Admin)**
   - ✅ Thêm `GET /admin/api/deals/{id}` API trả JSON thông tin deal
   - ✅ Thêm `POST /admin/deals/{id}/update` endpoint cập nhật deal
   - ✅ Modal edit deal với form pre-filled từ API, hỗ trợ upload banner mới

3. **Deal Pause/Resume (Admin)**
   - ✅ `POST /admin/deals/{id}/pause`: ACTIVE → PAUSED
   - ✅ `POST /admin/deals/{id}/resume`: PAUSED → ACTIVE, validate `endTime` chưa qua
   - ✅ Nút "⏸ Dừng" / "▶ Kích hoạt" trên deal card admin

4. **Deal Pause/Resume (Seller)**
   - ✅ Nút "Tạm dừng" / "Kích hoạt" trên deal card seller
   - ✅ Validate khi resume: deal hết hạn → báo lỗi không cho kích hoạt lại

5. **Seller Deal Detail Popup**
   - ✅ Click deal card → popup hiển thị tên, trạng thái, loại, giảm giá, ngày hết hạn
   - ✅ Nút Tạm dừng/Kích hoạt/Gán SP ngay trong popup

6. **Seller Deal Tabs Fixed**
   - ❌ Tab "Live"/"Lên lịch"/"Tạm dừng" không active sau khi filter
   - ✅ Sửa dùng `th:classappend` với `selectedStatus` thay vì `class="active"` cứng

7. **Seller Deal "Failed to fetch" — CSRF**
   - ❌ Seller POST endpoints (assign-products, pause, resume) không có CSRF ignore
   - ✅ Thêm `/seller/deals/*/assign-products`, `/*/remove-product/*`, `/*/pause`, `/*/resume` vào ignore list

8. **Seller Product Management (CRUD)**
   - ✅ `GET /seller/products`: danh sách SP với KPI cards + filter search/status/category
   - ✅ `GET /seller/products/create`: form tạo SP (tên, mô tả, giá, tồn kho, danh mục, hạn dùng, ảnh)
   - ✅ `POST /seller/products/create`: tạo SP với `approvalStatus = PENDING`
   - ✅ `GET /seller/products/{id}/detail`: API trả JSON chi tiết SP
   - ✅ `POST /seller/products/{id}/edit`: sửa SP (form pre-filled)
   - ✅ `POST /seller/products/{id}/toggle`: toggle Ngừng bán/Kích hoạt
   - ✅ Detail popup + Edit modal với CSS đẹp, badge trạng thái pastel
   - ✅ Nút ✏️ Sửa + ⏸ Toggle ngay trên product card
   - ✅ Thêm tab "Sản phẩm" vào bottom nav seller

9. **Seller Product — Image Fallback**
   - ✅ Thêm `onerror` cho tất cả ảnh trong seller pages (deal banner, product image, store logo)

10. **Seller Bottom Nav — Shared Fragment**
    - ✅ `common/seller/bottom-nav.html`: fragment dùng chung cho tất cả seller pages
    - ✅ 4 tabs: Dashboard, Deals, Sản phẩm, Pickup

11. **Deal Card CSS — Store Info Alignment**
    - ✅ Sửa padding store info sát lề trái 20px, thẳng hàng tên deal
    - ✅ Thêm `margin-top:4px` cách divider

12. **Seller Dashboard — Fix Store Name Display**
    - ✅ `th:text` dùng `(store.storeName)` thay vì `${store.storeName}` → hiển thị text literal
    - ✅ Logout button: đổi `background:transparent` thay vì `var(--surface)` (trắng)

13. **Seller API Fix: `product.price` → `product.originalPrice`**
    - ❌ JS dùng `product.price` không tồn tại trong Product entity
    - ✅ Sửa thành `product.originalPrice` trong tất cả seller JS

14. **Seller `getCurrentUser()` Fix**
    - ❌ Store owner có `work_store_id = null` (chỉ có `stores.owner_id`)
    - ✅ `getCurrentUser()` tự lookup `storeRepository.findByOwner(user)` nếu workStore null

### **Recent Fixes (2026-05-17):**

1. **Thymeleaf API Errors — formatDecimal & formatInteger**
   - ❌ `#numbers.formatDecimal(val, 0, 'COMMA', 0, 'POINT')` — Thymeleaf 3.1 KHÔNG hỗ trợ tham số 'COMMA'/'POINT'
   - ❌ `#numbers.formatInteger(doubleValue)` — chỉ nhận Integer, gây lỗi với Double
   - ✅ Luôn dùng `#numbers.formatDecimal(val, 0, 0)` cho mọi kiểu số (Number: Integer + Double)
   - Fixed 11 occurrences across `home.html`, `finance.html`, `products.html`

2. **Jackson Serialization — @JsonIgnore toàn bộ @OneToMany**
   - ❌ Jackson serialize entity → chạm @OneToMany lazy → LazyInitializationException hoặc circular reference vô hạn → 500
   - ✅ Thêm `@JsonIgnore` trên TẤT CẢ @OneToMany trong 6 entity: Category, Product, Deal, Store, User, DealProduct
   - ✅ Thêm `@JsonIgnore` trên helper getter gây NPE (`Product.getDiscountPercent()`, `DealProduct.getDiscountPercentage()`)

3. **Map.of() > 10 cặp → HashMap**
   - ❌ `Map.of()` chỉ hỗ trợ tối đa 10 cặp key-value
   - ✅ Dùng `new HashMap<>()` + `.put()` khi cần >10 cặp (`AdminController.getAnalyticsOverview`)

4. **Deal Card UI — Event Bubbling + th:onclick Pipe Syntax**
   - ❌ Click nút trong card trigger card's `onclick` → mở popup detail thay vì popup mong muốn
   - ✅ Thêm `event.stopPropagation()` vào mọi button trong card
   - ✅ Dùng pipe syntax `th:onclick="|func(${var})|"` thay vì `th:onclick="'func(' + ${var} + ')'"` tránh lỗi escape quote

5. **Deal Card — Buttons tràn sang panel phải**
   - ❌ 4 nút `flex:1` trong 1 hàng ~175px → mỗi nút ~44px
   - ✅ Đưa buttons ra ngoài flex row, dùng `grid-template-columns: repeat(2, 1fr)` full width

6. **Session Expiration Redirect Loop**
   - ❌ `CustomInvalidSessionStrategy` redirect loop: session hết hạn → redirect `/admin/login?expired=true` → vẫn gửi cookie JSESSIONID cũ → lại detect invalid → loop
   - ✅ Thêm check `requestUrl.contains("/login")` → return early
   - ✅ Xóa cookie JSESSIONID cũ trước khi redirect

7. **Role-Based Login Redirect**
   - ❌ Spring Security chỉ có 1 `loginPage("/login")` → admin vào `/admin/deals` (chưa login) redirect về `/login` (user) thay vì `/admin/login`
   - ✅ Tạo `CustomAuthenticationEntryPoint` kiểm tra URL prefix → redirect đúng `/admin/login`, `/seller/login`, `/login`

8. **DB Role Name Inconsistency**
   - ❌ DB có `USER`, `STORE_OWNER` (thiếu prefix `ROLE_`) nhưng code tìm `ROLE_USER`, `ROLE_STORE_OWNER`
   - ✅ `AdminController.findRoleByName()` thử cả 2 format
   - ✅ `fix_roles.sql` để sửa root cause trong DB

9. **Seller Dashboard — Full Rewrite**
   - ✅ `SellerController.dashboard()`: KPI cards (total orders, active deals, pending, products), recent orders, tier badge
   - ✅ `common/seller/bottom-nav.html`: shared bottom navigation fragment
   - ✅ `getCurrentUser()` tự lookup store qua `storeRepository.findByOwner()` nếu `workStore` null

10. **Partner Tier System (Đồng/Vàng/Bạch Kim/Kim Cương)**
    - ✅ `Store.partnerTier` + `getCommissionRate()` (BRONZE 10%, SILVER 8%, GOLD 6%, DIAMOND 4%)
    - ✅ Migration `V5__add_partner_tier.sql`
    - ✅ `StoreService.upgradePartnerTiers()` — @Scheduled job chạy đầu tháng
    - ✅ Tier badge hiển thị trên deal card + dashboard + reconciliation

11. **Payout Balance Check**
    - ✅ `processPayout()` validate `pendingAmount <= balance` trước khi xử lý

12. **Reconciliation Print**
    - ✅ Thêm nút "In biên bản" + `printReconciliation()` mở cửa sổ in với HTML đầy đủ

13. **Product Toggle Validation**
    - ✅ `ProductService.toggleActive()`: chỉ kích hoạt lại khi `stock > 0` và `expiryDate` chưa qua

14. **Product Detail Popup**
    - ✅ Thay nút xóa = nút "Chi tiết", thêm modal hiển thị toàn bộ thông tin SP

15. **Product Filter Working**
    - ✅ Filter bar trong `products.html` thành form thực sự với search/category/status

16. **Dispute Detail Panel Fix**
    - ❌ `selectDispute()` dùng `innerHTML` phá hủy structure panel → "stuck loading"
    - ✅ Set `textContent` từng field, giữ nguyên HTML structure

17. **Image Upload Dedup**
    - ✅ `saveUploadedFile()` check `Files.exists()` trước khi copy, nếu đã có file thì dùng lại link

18. **Finance Transaction Loading Fix**
    - ❌ JS gửi `startDate=""` (empty string) → `LocalDateTime.parse("" + "T00:00:00")` → exception
    - ✅ Thêm check `!startDate.isEmpty()` trước khi parse

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

- [ ] **Đã thêm đủ layout: bottom-nav/sidebar + header + CSS fragment trước khi code trang mới**
  - Seller page: `<div th:replace="~{common/seller/bottom-nav :: bottom-nav('pageName')}"></div>` + CSS `.bottom-nav{...}` trong `<style>`
  - Admin page: `<div th:replace="~{common/admin/admin-layout :: layout(...)}">` + sidebar fragment
  - CSS fragment: `<div th:replace="~{common/admin/css :: css}"></div>`
  - Header: `position:sticky` hoặc `position:fixed` với `z-index:10`
- [ ] Không hardcoded sidebar/header — dùng fragment `common/admin/` hoặc `common/seller/`
- [ ] Dùng fragment pattern đúng
- [ ] Không duplicate CSS
- [ ] Có service layer cho business logic
- [ ] Dùng soft delete
- [ ] Preserve URL parameters
- [ ] Test cả happy path và edge cases
- [ ] Check permissions (@PreAuthorize)
- [ ] Check CSRF ignore list (`SecurityConfig.java`) cho POST endpoints mới
- [ ] `#numbers.formatDecimal(val, 0, 0)` cho mọi số trong Thymeleaf (KHÔNG dùng formatInteger)
- [ ] `@JsonIgnore` trên tất cả `@OneToMany` trong Entity mới
- [ ] `th:classappend` không dùng `th:class` (tránh mất class gốc)
- [ ] Pipe syntax `|...|` cho `th:onclick` phức tạp
- [ ] `event.stopPropagation()` trong button nằm trong clickable card
- [ ] `new HashMap<>()` khi cần >10 cặp (không dùng `Map.of()`)
- [ ] Null-safe + empty string check trước `LocalDateTime.parse()`
- [ ] KHÔNG nested single quotes trong `th:text` string concatenation — dùng nested span hoặc pipe syntax
- [ ] Run migration SQL (nếu có schema change)

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
- [x] **M6.8: Jackson Serialization Fixes + UI Overhaul** (Completed 2026-05-17)
- [x] **M6.9: Seller Dashboard + Partner Tier System** (Completed 2026-05-17)
- [x] **M6.10: Session Management + Auth Redirect Fixes** (Completed 2026-05-17)
- [x] **M6.11: Seller Product Management + Deal Edit/Pause** (Completed 2026-05-18)
- [x] **M6.12: Seller Finance & Wallet + Reconciliation** (Completed 2026-05-19)
- [x] **M6.13: Seller Analytics & Recommendations Dashboard** (Completed 2026-05-19)
- [x] **M6.14: Store Page Builder (Drag & Drop) + Dynamic Buyer Store** (Completed 2026-05-19)
- [x] **M6.15: Product Combo + Quick Push + Expiry Countdown** (Completed 2026-05-19)
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
