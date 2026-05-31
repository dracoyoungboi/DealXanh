# DealXanh - O2O Deal Marketplace Platform

## 📊 Project Status: **~94% Complete**

**Last Updated:** 2026-06-01 (Session: Buyer Prepay, Pickup Time, Cart Checkboxes, Deal FIXED/PERCENT Logic Fix, Location-Based Sorting, Deal System Audit)
**Version:** 1.0.0-beta

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

### **4. Buyer Interface (~95%)**
- ✅ Home Page — Hero + Flash Sale countdown + Deal nổi bật banner + Combo banner + Categories + Trust (product-centric data)
- ✅ "Thêm vào giỏ" + "Mua ngay" buttons on every deal card (home + category + deal-detail)
- ✅ Deal Detail (`/deals/{id}`) — Deal info + discount card + store card + product list + add-to-cart
- ✅ Category Page (`/category/{id}`) — Shopee-style: partner gradient banners (6 colors), price filter chips (All/<50k/50-100k/100-200k/>200k), compact product grid (auto-fill minmax 150px), gold discount badges, storefront fallback SVG icon, debounce search
- ✅ Cart Page (`/buyer/cart`) — Session-based ±/remove/checkout, "Bạn chưa đăng nhập" state with login CTA when not authenticated
- ✅ Cart API — add/remove/update/count (HttpSession) with storeId support
- ✅ Profile Page (`/buyer/profile` + `/profile`) — Green gradient header, 3 stat cards in flexbox row with dividers, recent 5 orders with color-coded status badges, account menu, logout
- ✅ Dynamic Store Page (`/store/{storeId}`)
- ✅ Store Profile (`/buyer/store/{storeId}/profile`) — Store info, tier badge, products grid
- ✅ Checkout Page (`/buyer/checkout`) — Order summary grouped by store, price breakdown, stepper (4 steps)
- ✅ Payment Page (`/buyer/payment`) — Cash selected default, MoMo/bank disabled (sắp ra mắt)
- ✅ Order Complete (`/buyer/order-complete`) — Success hero, confetti animation, 3-step pickup instructions
- ✅ Order Tracking (`/buyer/orders`) — 6 status tabs (Tất cả/Chờ xác nhận/Đang xử lý/Sẵn sàng nhận/Hoàn thành/Đã hủy), client-side JS filter, color-coded badges, full Vietnamese diacritics
- ✅ Search Page (`/buyer/search?q=`) — Real-time debounce search, compact product grid with deal pricing
- ✅ Deal Map (`/buyer/deal-map`) — Active stores list
- ✅ Login-aware UI — Cart shows login prompt when unauthenticated; Orders redirects to login
- ⚠️ Order detail page — redirects to list (no detail template yet)
- ⚠️ POST /buyer/checkout/confirm — creates empty Orders without OrderItems (incomplete)

### **5. Deal System (100%)**

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

### **8. Order System (~90%)**
- ✅ Order Entity + OrderItem Entity
- ✅ Order Repository với statistics queries + findByIdWithItems
- ✅ Order processing workflow (PENDING→CONFIRMED→READY→COMPLETED)
- ✅ Buyer order tracking with 6 tabs + detail page
- ✅ Seller/Staff order management with status transitions
- ✅ CANCELLED tab + cancelledCount on seller/staff orders
- ✅ Staff status update validation (parity with seller)
- ✅ QR pickup confirmation flow
- ⚠️ PayOS webhook (cần public URL, hiện dùng polling)

### **9. Cart System (~98%)**
- ✅ Session-based cart (HttpSession)
- ✅ Cart APIs: GET /api/cart, POST add/remove/update, GET count
- ✅ Cart page (`/buyer/cart`) with ±/remove/checkout
- ✅ Deal discount calculation (original vs sale price)
- ✅ Deal type/name badges on cart items
- ✅ Voucher system: selectable voucher list modal + apply/remove
- ✅ Cart badge on mobile-nav with live count
- ✅ Checkout → order flow complete with OrderItems + amounts

### **10. Payment System (~85%)**
- ✅ PayOSService: payment link creation, webhook processing, status polling
- ✅ PaymentController: create payment, webhook (public), status check
- ✅ Transaction entity with transactionRef + paymentMethod fields
- ✅ PayOS VietQR QR code generation + display in seller/staff modal
- ✅ Payment flow: buyer checkout → seller creates PayOS QR → buyer pays → system polls/confirms
- ✅ 3-case handling: đủ tiền (COMPLETED), thiếu (partial + mã mới), thừa (COMPLETED + cảnh báo)
- ✅ Auto stock deduction + Transaction creation on successful payment
- ✅ Admin finance: GMV, commission, payout, reconciliation
- ✅ Seller wallet: balance, earned, transactions, payout requests
- ⚠️ Webhook needs public URL (localhost: polling nút "Kiểm tra trạng thái")

### **10. Database & Migrations (100%)**
- ✅ Flyway migrations (V1 - V7)
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

### **15. KHÔNG dùng @{...} trong ${...} SpEL expression**
- ❌ `th:href="${dealInfo != null ? @{/deals/{id}(id=dealInfo.dealId)} : '#'}"` — `@{` trong SpEL bị parse thành bean reference → `EL1059E: @ or & can only be followed by an identifier`
- ✅ Dùng string concatenation: `th:href="${dealInfo != null ? '/deals/' + dealInfo.dealId : '#'}"`
- ❌ Tương tự: không dùng `#{}`, `~{}`, `@{}` bên trong `${...}`
- ✅ Nếu cần link Thymeleaf phức tạp, dùng `th:with` để tính toán trước

### **16. LUÔN dùng pipe syntax \|...\| cho th:text với tiếng Việt**
- ❌ `th:text="'Được tin dùng bởi ' + #numbers.formatDecimal(n, 0, 0) + ' người'"` — nested single quotes + tiếng Việt → parse error
- ❌ `th:text="'Giảm ' + #numbers.formatDecimal(n, 0, 0) + 'đ'"` — tương tự
- ❌ `th:text="'Giảm ' + deal.discountValue.intValue() + '%'"` — tương tự
- ✅ Pipe syntax: `th:text="|Được tin dùng bởi ${#numbers.formatDecimal(n, 0, 0)}+ người|"`
- ✅ Pipe syntax: `th:text="|Giảm ${#numbers.formatDecimal(n, 0, 0)}đ|"`
- ✅ Pipe syntax: `th:text="|Giảm ${deal.discountValue.intValue()}%|"`
- ✅ Pipe syntax với ternary: `th:text="|Giảm đến ${fs.discountValue.intValue()}${fs.discountType == 'PERCENT' ? '%' : 'đ'}|"`
- ✅ **LUÔN dùng pipe syntax \|...\| cho MỌI th:text có chứa tiếng Việt hoặc ký tự đặc biệt**

### **17. LUÔN dùng debounce search real-time (không cần nút Tìm)**

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

### **18. LUÔN validate file upload (loại file + kích thước) trước khi lưu**

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

### **19. Phong cách coding — quy tắc chung**

**Fragment:**
- ✅ LUÔN dùng `th:replace` không parameter khi fragment dùng chung model — tránh conflict tên biến
  ```html
  <!-- ĐÚNG: fragment tự đọc từ model -->
  <div th:replace="~{common/staff/header :: header}"></div>
  <!-- SAI: parameter trùng tên model attribute gây lỗi -->
  <div th:replace="~{common/staff/header :: header(user, store, ...)}"></div>
  ```
- ✅ Tách header + bottom-nav + css thành fragment riêng trong `common/[role]/` cho mỗi role

**Controller:**
- ✅ Controller riêng cho từng role: AdminController (`/admin/**`), SellerController (`/seller/**`), StaffController (`/staff/**`)
- ✅ Mỗi controller có `@PreAuthorize` ở class level
- ✅ Dùng `getCurrentUser(Principal)` helper để load user fresh từ DB mỗi request
- ✅ Dùng `findRoleByName("ROLE_XXX", "XXX")` thử cả 2 format (DB có thể thiếu prefix `ROLE_`)

**Trang mới:**
- ✅ Tạo đủ endpoint GET + xử lý model trước khi viết template
- ✅ Thêm CSRF ignore trong `SecurityConfig` cho POST endpoint mới
- ✅ Dùng fragment chung cho header/bottom-nav/css
- ✅ Set `activePage` trong model để bottom-nav highlight đúng tab
- ✅ Set `pendingToday` cho staff pages để hiển thị badge số đơn chờ

**Modal / Popup:**
- ✅ Dùng pattern `data-url` + `onclick` cho button mở modal, không dùng `th:onclick` với string
- ✅ Image preview: phân biệt ảnh (hiển thị `<img>`) vs PDF (mở tab mới)
- ✅ Modal close: click overlay background hoặc nút ✕

**Back button / Navigation:**
- ✅ Trang con (category, deal-detail, cart) dùng `href="javascript:history.back()"` để giữ scroll position
- ❌ KHÔNG dùng `href="/"` cho back button — load lại từ đầu trang, mất vị trí cuộn

**Database:**
- ✅ Boolean trong MySQL: dùng `TINYINT(1) DEFAULT 0` thay vì `BIT` — tương thích MySQL Workbench, Hibernate Boolean mapping

**File upload:**
- ✅ LUÔN validate `contentType` + `fileSize` TRƯỚC khi `saveUploadedFile()`
- ✅ `saveUploadedFile()` dùng MD5 hash — tự động dedup
- ✅ Thêm hint frontend về định dạng + kích thước cho phép

**QR / Pickup Flow:**
- ✅ QR code format: `DX-{orderId}-{6 random chars}`
- ✅ Generate QR khi order chuyển sang `READY_FOR_PICKUP`
- ✅ Buyer confirm tại `/pickup/{qrCode}` → POST confirm → COMPLETED

### **20. Layout chuẩn cho mọi trang Buyer**

Mọi trang buyer PHẢI có đủ 5 fragment chung từ `common/buyer/`. Đây là pattern cố định cho toàn bộ buyer interface:

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tiêu đề trang — DealXanh</title>
    <meta name="description" content="Mô tả trang">

    <!-- CSS Fragment -->
    <div th:replace="~{common/buyer/css :: css}"></div>
</head>
<body>

<!-- Header & Search -->
<div th:replace="~{common/buyer/header :: header}"></div>

<!-- Nội dung chính của trang -->
<!-- ... -->

<!-- Footer (máy tính) -->
<div th:replace="~{common/buyer/footer :: footer}"></div>

<!-- Mobile Nav (điện thoại) -->
<div th:replace="~{common/buyer/mobile-nav :: mobileNav}"></div>

<!-- JS Fragment -->
<div th:replace="~{common/buyer/js :: js}"></div>

</body>
</html>
```

**Quy tắc:**
- ✅ LUÔN dùng `~{}` syntax cho fragment expression (Thymeleaf 3.1+)
- ✅ LUÔN có đủ 4 fragment: css, header, mobile-nav, js
- ✅ Footer (`common/buyer/footer`) CHỈ dùng ở trang chủ (`/`). Các trang con (category, profile, store, deal-detail, cart...) KHÔNG có footer
- ✅ Header + mobile-nav cho mobile, footer cho desktop (chỉ ở trang chủ)
- ✅ CSS fragment ở `<head>`, các fragment còn lại ở cuối `<body>` (trước `</body>`)
- ✅ Link `home.css` trong `<head>` để có style cho `.deal-card`, `.flash-sale`, `.blind-box-section`, `.mobile-nav`, `.category-card`, `.trust-section`...
- ✅ Mỗi trang buyer chỉ khác nhau phần nội dung chính giữa header và footer/mobile-nav
- ❌ KHÔNG được thiếu mobile-nav
- ❌ KHÔNG dùng inline `style="..."` — dùng CSS class từ các file CSS có sẵn
- ❌ KHÔNG dùng `th:text` với string concatenation có single quote — dùng pipe syntax `|...|`
- ❌ KHÔNG dùng `@{...}` trong `${...}` SpEL — dùng string concat `'/path/' + id`

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

## 🔐 Quy tắc CRITICAL (added 2026-05-22)

### **21. KHÔNG dùng `th:onclick` với string variable trong Thymeleaf 3.1+**

Thymeleaf 3.1+ blocks string variables in event handler attributes (`th:onclick`, `th:onload`, etc.) with error:
> "Only variable expressions returning numbers or booleans are allowed in this context"

**❌ SAI:**
```html
<button th:onclick="|addToCart(${productId}, '${productName}', ${price})|">
<button th:onclick="|window.location.href='/orders/' + ${orderId}|">
```

**✅ ĐÚNG — dùng `data-*` attributes + plain `onclick`:**
```html
<button th:attr="data-pid=${productId},data-pname=${productName},data-price=${price}"
        onclick="addToCart(Number(this.dataset.pid), this.dataset.pname, Number(this.dataset.price))">
```
```html
<div th:attr="data-oid=${orderId}"
     onclick="window.location.href='/orders/' + this.dataset.oid">
```

### **22. LUÔN thử cả username VÀ email khi lookup user từ Principal**

`CustomUserDetailsService.loadUserByUsername()` sets the principal name to `user.getUsername()` (NOT email). Nhưng người dùng có thể login bằng email.

**✅ LUÔN dùng pattern này trong controller:**
```java
private User getCurrentUser(Principal principal) {
    if (principal == null || principal.getName() == null) return null;
    String name = principal.getName();
    User user = userRepository.findByUsername(name).orElse(null);
    if (user == null) user = userRepository.findByEmail(name).orElse(null);
    return user;
}
```

### **23. KHÔNG dùng `hasRole()` cho role không có prefix `ROLE_`**

DB store role là `USER` (không có `ROLE_` prefix). `hasRole("USER")` → Spring thêm prefix → tìm `ROLE_USER`. Nếu DB có `ROLE_USER` thì OK, nếu chỉ `USER` thì fail.

**✅ Dùng `hasAnyAuthority("ROLE_USER", "USER")`** hoặc **`.authenticated()`** để accept mọi authenticated user.

---

## 🔐 Quy tắc CRITICAL (added 2026-05-24)

### **24. KHÔNG dùng JavaScript template literal `` `...${...}` `` trong file Thymeleaf**

Thymeleaf parse `${...}` trong template literal thành SpEL expression → code JS bị hỏng hoặc crash.

**❌ SAI:**
```javascript
document.getElementById('el').innerHTML = `
    <div>${data.name}</div>
    <img src="${data.url}">
`;
```

**✅ ĐÚNG — dùng string concatenation:**
```javascript
document.getElementById('el').innerHTML =
    '<div>' + data.name + '</div>' +
    '<img src="' + data.url + '">';
```

**Quy tắc:** Mọi file `.html` trong `templates/` đều bị Thymeleaf parse. KHÔNG dùng backtick template literal nếu có `${}` bên trong.

### **25. LUÔN kiểm tra key trong HashMap cart item trước khi dùng trong Thymeleaf**

Cart item là `HashMap<String, Object>` — key sai (`imageUrl` thay vì `productImage`) gây lỗi Thymeleaf parse.

**❌ SAI:** `item.imageUrl` — HashMap không có key này, lỗi `PropertyOrFieldReference`
**✅ ĐÚNG:** `item.productImage` — đúng key lưu trong session cart

### **26. Sticky bottom bar LUÔN dùng `bottom:72px` + `padding-bottom:20px`**

Mobile nav cao ~70px. Sticky bar với `bottom:60px` sẽ bị nav che.

```css
.cart-summary{position:fixed;bottom:72px;...;padding:14px 16px 20px}
.sticky-bottom{position:fixed;bottom:72px;...;padding:14px 16px 20px}
```

### **27. PayOS trả về VietQR string, KHÔNG phải ảnh base64**

PayOS API v2 trả `qrCode` là chuỗi VietQR NAPAS (dạng `000201010212...`). Phải generate QR ảnh từ chuỗi này:
- Dùng third-party: `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=` + encodeURIComponent(qrString)
- Hoặc tự generate bằng thư viện Java (ZXing)

### **28. PayOS signature: thứ tự CỐ ĐỊNH, không sort alphabetically**

PayOS yêu cầu signature format cụ thể (theo official SDK):
```
amount={amount}&cancelUrl={cancelUrl}&description={description}&orderCode={orderCode}&returnUrl={returnUrl}
```
KHÔNG include `expiredAt`, KHÔNG sort key alphabetically. Dùng HMAC-SHA256 với checksum key.

### **29. PayOS localhost: webhook không tới được, phải polling**

Khi chạy localhost, PayOS không thể gửi webhook IPN. Phải thêm endpoint `GET /api/payment/status/{orderId}` poll `GET /v2/payment-requests/{orderCode}` từ PayOS API để kiểm tra trạng thái thanh toán thực tế.

### **30. LUÔN dùng `th:if` cho fragment điều kiện theo page**

Slot filter trong header chỉ hiển thị ở dashboard:
```html
<div class="slot-strip" th:if="${activePage == 'dashboard'}">
```
Controller phải set `model.addAttribute("activePage", "dashboard")` cho trang tương ứng.

---

## 🔐 Quy tắc CRITICAL (added 2026-05-24 — Cart & Product)

### **31. LUÔN dùng `Product.isAvailable()` để filter sản phẩm cho buyer**

**❌ SAI:** Tự check từng field riêng lẻ `product.getActive()`, `product.getDeleted()`, v.v.
**✅ ĐÚNG:** Gọi `product.isAvailable()` — method này là single source of truth, check đủ: deleted, active, approvalStatus, stockQuantity, deal time window.

```java
// Trong controller
if (prod == null || !prod.isAvailable()) continue;
```

`isAvailable()` đã được update để check cả `approvalStatus = 'APPROVED'`. Nếu thêm điều kiện mới về availability → chỉ cần sửa 1 chỗ trong `Product.isAvailable()`.

### **32. LUÔN dùng `findAvailable*()` repository queries cho buyer-facing pages**

ProductRepository có sẵn 4 queries đã filter active + approved + in-stock + not deleted + deal time window:
- `findAvailableProducts(now, pageable)`
- `findAvailableByCategory(categoryId, now, pageable)`
- `searchAvailable(keyword, now, pageable)`
- `findAvailableByStore(storeId, now)`

**❌ SAI:** `productRepository.findAll().stream().filter(...)` — load toàn bộ DB về memory rồi mới filter  
**✅ ĐÚNG:** Dùng dedicated `@Query` method tương ứng — filter ở DB level, performance tốt hơn nhiều

### **33. Cart LUÔN persist vào DB, KHÔNG dùng HttpSession**

Cart đã được migrate từ session sang DB thông qua `CartService`. Entity: `Cart` (1-1 với User) + `CartItem` (N-1 với Cart, N-1 với Product).
- `CartService.getOrCreateCart(user)` — lấy hoặc tạo cart
- `CartService.migrateSessionCart(session, user)` — gọi trước khi đọc cart để merge session cũ vào DB
- Mọi cart API endpoint cần `Principal` (yêu cầu login)

**❌ SAI:** `session.getAttribute("cart")` — session hết hạn → mất giỏ hàng  
**✅ ĐÚNG:** `cartService.getCartData(user)` — cart persist qua mọi lần login/logout

### **34. LUÔN validate cart item khi load — check `product.isAvailable()`**

`CartService.getCartData()` tự động kiểm tra từng CartItem:
- Nếu product không còn available: set `unavailable=true`, `quantity=0`, `maxStock=0`, `unavailableReason="Sản phẩm đã hết hàng hoặc hết hạn"`
- Item vẫn hiển thị trong cart nhưng bị grey out, không thể tăng/giảm số lượng, chỉ có thể xóa
- Frontend (`cart.html`) xử lý `item.unavailable` → hiển thị cảnh báo đỏ + "Kho: 0 sản phẩm"

### **35. CartService là layer bắt buộc cho mọi thao tác giỏ hàng**

Tất cả thao tác cart PHẢI qua `CartService`:
- `addItem()` — validate stock, check duplicate, thêm vào DB
- `removeItem()` — xóa khỏi DB
- `updateItemQuantity()` — validate stock cap, cập nhật quantity
- `getCartData()` — trả về items + count với validation
- `getCartItemsForCheckout()` — lọc bỏ unavailable items trước khi checkout
- `clearCart()` — xóa cart sau khi đặt hàng thành công

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
    ├── StaffController.java ✅ (300+ lines - orders, products CRUD, profile)
    ├── HomeController.java ✅ (dynamic /store/{storeId})
    └── ... (2 more)

src/main/resources/
├── db/migration/
│   ├── V2__add_product_approval.sql
│   ├── V3__create_deal_categories.sql
│   ├── V3__add_apply_method_to_deals.sql
│   ├── V4__add_deal_validations.sql
│   ├── V5__add_partner_tier.sql
│   ├── V6__add_current_price.sql ✅ (NEW)
│   └── V7__add_created_by_to_products.sql ✅ (NEW)
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
│   ├── seller/ (9 pages)
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
│   ├── common/staff/
│   │   ├── css.html ✅
│   │   ├── header.html ✅
│   │   └── bottom-nav.html ✅ (4 tabs)
│   ├── staff/ (5 pages)
│   │   ├── dashboard.html ✅
│   │   ├── orders.html ✅ (detail popup, status update)
│   │   ├── products.html ✅ (create button, detail modal)
│   │   ├── create-product.html ✅ (NEW)
│   │   └── profile.html ✅ (avatar, info, password)
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

### **Recent Fixes (2026-05-21) — Mega Update:**

1. **Buyer Home Page — Complete Rewrite (Product-Centric)**
   - ✅ Hero section: video background + stats từ DB (totalStores, totalUsers, totalDeals)
   - ✅ Quick filter chips: Tất cả / Flash Sale / Combo / Voucher / Seasonal (filter JS hoạt động)
   - ✅ Flash Sale banner: background ảnh SP đầu tiên, overlay, countdown timer dựa trên `endTime` sớm nhất, CTA "Săn ngay" + grid SP bên dưới
   - ✅ Deal nổi bật banner: overlay xanh lá, tag "🔥 DEAL NỔI BẬT", tổng SP + grid dùng `.deal-card` CSS từ home.css
   - ✅ Combo giảm giá section: `.blind-box-section` banner vàng gradient + grid SP bên dưới
   - ✅ Categories grid: `.category-card` overlay hover (fix: `cat.iconUrl` thay vì `cat.imageUrl`)
   - ✅ Trust section: 3 cards an toàn/chất lượng/hỗ trợ 24/7
   - ✅ Tất cả `th:text` dùng pipe syntax `|...|` để tránh lỗi nested single quote với tiếng Việt
   - ✅ Tất cả fragment dùng `~{}` syntax (không còn deprecated warning)
   - ✅ Link `home.css` để có style `.deal-card`, `.flash-sale`, `.blind-box-section`, `.mobile-nav`, v.v.

2. **Buyer Category Page — Full Redesign**
   - ✅ Back bar "← Quay lại" dùng `history.back()` (giữ scroll position)
   - ✅ Header: tên danh mục + mô tả + số lượng SP
   - ✅ Search bar debounce 300ms (pattern từ rule #16)
   - ✅ Store filter chips: lọc theo cửa hàng, giữ search query khi chuyển store
   - ✅ Product grid: card có ảnh, badge giảm giá, loại deal, store avatar + tên, giá sale + gốc
   - ✅ Empty state riêng cho search vs không có SP
   - ✅ HomeController.categoryPage() hỗ trợ `@RequestParam search` + `storeId`

3. **Deal Detail Page (`/deals/{id}`)**
   - ✅ Deal header: badge loại deal, tên, mã, thời gian còn lại
   - ✅ Discount card: % hoặc số tiền giảm + điều kiện đơn tối thiểu
   - ✅ Store card: logo + tên + rating
   - ✅ Product list: ảnh, tên, mô tả, giá sale + gốc + % giảm, nút "Thêm vào giỏ"
   - ✅ Toast thông báo khi thêm vào giỏ

4. **Cart System (HttpSession)**
   - ✅ `GET /api/cart` — lấy toàn bộ giỏ hàng (items + count)
   - ✅ `POST /api/cart/add` — thêm SP (hoặc tăng SL nếu đã có)
   - ✅ `POST /api/cart/remove` — xoá SP
   - ✅ `POST /api/cart/update` — cập nhật số lượng (± / xoá nếu qty ≤ 0)
   - ✅ `GET /api/cart/count` — số lượng cho badge
   - ✅ Cart page (`/buyer/cart`): danh sách SP, nút +/−, xoá, tổng tiền, nút Thanh toán
   - ✅ Cart badge mobile-nav hiển thị số thật từ session
   - ✅ CSRF: `/api/**` đã có trong ignore list

5. **Buyer Profile Page — Fixed**
   - ✅ Link `home.css` → header/mobile-nav có style
   - ✅ CSS variables fix: `--bg-light` → `--background`, `white` → `var(--surface)`, hardcoded hex → design tokens
   - ✅ `html{height:auto;min-height:100%}` + `body{padding-bottom:80px}` pattern
   - ✅ `<style>` moved to `<head>`
   - ✅ No footer (footer chỉ có ở trang chủ)

6. **Staff Create Product — Parity with Seller**
   - ✅ Product type selector (SPECIFIC_DEAL / COMBO)
   - ✅ Combo section: multi-select products, HSD warning (≤3 ngày), confirmation checkbox
   - ✅ Combo price auto-calculation = average
   - ✅ `GET /staff/api/products/combo-available`
   - ✅ StaffController.createProduct() hỗ trợ `comboProductIds` + set expiry earliest

7. **Weak Password System**
   - ✅ User entity: `weakPassword` boolean field
   - ✅ Migration V8: `TINYINT(1) DEFAULT 0` (dùng TINYINT thay vì BIT để tương thích MySQL Workbench)
   - ✅ Staff dashboard: popup "Mật khẩu của bạn quá yếu" → redirect `/staff/profile?changePassword=1`
   - ✅ Staff profile: auto-open password modal khi có `?changePassword=1`
   - ✅ Seller manage-staff: auto-fill random 6-digit password, set `weakPassword=true`
   - ✅ StaffController.changePassword(): set `weakPassword=false` sau khi đổi

8. **Thymeleaf — All Templates Cleaned**
   - ✅ Tất cả `th:text` với string concatenation + single quote → pipe syntax `|...|`
   - ✅ Tất cả fragment expression → `~{}` syntax (không deprecated warning)
   - ❌ `@{...}` KHÔNG ĐƯỢC dùng trong `${...}` SpEL — gây lỗi parse `@ or & can only be followed by an identifier`
   - ✅ Fix: dùng string concat `'/deals/' + dealId` thay vì `@{/deals/{id}(...)` trong SpEL

9. **Footer Rule**
   - ✅ Footer (`common/buyer/footer`) CHỈ dùng ở trang chủ (`/`)
   - ✅ Các trang con (category, profile, deal-detail, cart...) KHÔNG có footer
   - ✅ Rule #19 updated

10. **Back Button**
    - ✅ Dùng `history.back()` thay vì `href="/"` để giữ scroll position
    - ✅ Áp dụng cho category, deal-detail, cart pages

### **Recent Fixes (2026-05-20):**

1. **Staff Product Management — Create & Detail**
   - ✅ Staff can now create products: `GET /staff/products/create` (form) + `POST /staff/products/create` (submit)
   - ✅ Staff product detail popup: click product row → modal with full info (name, price, stock, category, HSD, status, creator)
   - ✅ API: `GET /staff/api/products/{id}/detail` returns product JSON with `createdByName`
   - ✅ Template: `staff/create-product.html` with staff header/bottom-nav fragments, image upload validation
   - ✅ Staff products page: added "+ Thêm sản phẩm" button, product rows clickable for detail
   - ✅ CSRF: `/staff/products/create` added to ignore list

2. **Product.createdBy — Track Who Created Product**
   - ✅ Product entity: added `@ManyToOne User createdBy` field (FK `created_by` → `users.user_id`)
   - ✅ Flyway migration `V7__add_created_by_to_products.sql`
   - ✅ SellerController: `setCreatedBy(user)` in both normal create and quick-push combo create
   - ✅ StaffController: `setCreatedBy(user)` when staff creates product
   - ✅ Seller products page: shows "👤 {creator name}" in product meta row
   - ✅ Staff product detail: shows "Người tạo" field
   - ✅ Admin products page: already shows "🏪 Cửa hàng" (store name) — no change needed

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

## 🔥 Recent Updates (2026-05-21/22) — Buyer Flow Mega Update

### **Tổng quan — Những gì đã hoàn thành**

Toàn bộ luồng buyer từ Home → Category → Deal Detail → Cart → Checkout → Payment → Order Complete → Order Tracking đã hoạt động end-to-end. 17 file được tạo/sửa trong hội thoại này.

### **Trang mới (7 templates mới + controller endpoints)**

| File | Route | Mô tả |
|---|---|---|
| `buyer/profile.html` | `/buyer/profile` + `/profile` | Green gradient header, avatar, 3 stat cards (flexbox + divider), recent orders, account menu, logout |
| `buyer/order-summary.html` | `/buyer/checkout` | Items grouped by store, stepper (4 bước), price breakdown, notes, sticky footer |
| `buyer/payment.html` | `/buyer/payment` | Payment method radio cards (cash/MoMo/bank), stepper, confirm → POST checkout |
| `buyer/order-complete.html` | `/buyer/order-complete` | Success hero + animated checkmark, confetti canvas, 3-step pickup guide |
| `buyer/order-tracking.html` | `/buyer/orders` | 6 status tabs, color-coded badges, JS client-side filter, full Vietnamese |
| `buyer/search.html` | `/buyer/search?q=` | Debounce search, compact product grid with deal pricing |
| `buyer/store-profile.html` | `/buyer/store/{id}/profile` | Store logo, address, tier badge, description, products grid |

### **Trang sửa lớn (3 trang)**

| File | Thay đổi |
|---|---|
| `buyer/category.html` | Shopee-style toàn bộ: partner gradient banners (6 màu), price filter chips (5 mức), compact auto-fill grid, gold discount badges, storefront fallback SVG, "Thêm vào giỏ" + "Mua ngay" buttons |
| `buyer/home.html` | "Thêm vào giỏ" + "Mua ngay" buttons dưới mỗi deal card (3 sections: Flash Sale, Deal Nổi Bật, Combo), addToCart/buyNow JS functions, showToast/updateCartBadge helpers |
| `buyer/deal-detail.html` | Updated addToCart to pass storeId, fixed th:onclick → data-* pattern |
| `buyer/cart.html` | Login-aware: "Bạn chưa đăng nhập" state với login CTA khi unauthenticated; fixed checkout URL |

### **Backend changes**

| File | Thay đổi |
|---|---|
| `HomeController.java` | 8 new endpoints: `/buyer/profile`, `/buyer/checkout`, `/buyer/payment`, `/buyer/order-complete`, `/buyer/orders`, `/buyer/search`, `/buyer/store/{id}/profile`, `/buyer/deal-map`; Added `storeId` to cart API; Added `priceRange` filter for category; Fixed `getCurrentUser()` to try username THEN email (matching AuthController); Added `pendingOrders` computed in Java |
| `AuthController.java` | Added `orderRepository` injection + order stats (totalOrders, completedOrders, pendingOrders, recentOrders) to `/profile` endpoint |
| `OrderRepository.java` | Added `findByUserUserIdOrderByCreatedAtDesc` |
| `SecurityConfig.java` | Refined buyer route permissions: orders now public (auth handled in controller); checkout/profile require `.authenticated()`; relax max sessions to 5; add `/buyer/deal-map` and `/buyer/checkout/confirm` to CSRF ignore |
| `CLAUDE.md` | Updated Buyer Interface section, added this Mega Update section |

### **Critical Bug Fixes**

| Bug | Root Cause | Fix |
|---|---|---|
| **Orders page redirects to login even when authenticated** | `getCurrentUser()` only used `findByEmail()`, but `CustomUserDetailsService` sets principal name to `username` (not email). AuthController tried both, so `/profile` worked. | `getCurrentUser()` now tries `findByUsername()` first, then `findByEmail()` |
| **Home page crashes with Thymeleaf error** | `th:onclick="...${productName}..."` — Thymeleaf 3.1 security blocks string variables in event handlers | ALL `th:onclick` with strings replaced by `th:attr="data-*="` + plain `onclick=""` reading `this.dataset.*` |
| **Category page @{...} in ${...} SpEL** | `@{/category/...}` inside `${...}` causes parse error `EL1059E` | Replaced with string concat `'/category/' + id` |
| **Profile stats crash** | `#numbers.formatDecimal(totalOrders - completedOrders, 0, 0)` — both null → SpEL subtraction fails | Added `pendingOrders` computed in controller; all 3 stats use null-safe `(val != null ? val : 0)` |
| **Order tracking template crash** | Same th:onclick security block as home page | Fixed `th:onclick` → `data-*` + `onclick`; Removed broken navigation to non-existent `order-detail` |
| **Price shows "d" not "đ"** | Pipe syntax used `d` instead of `đ` character | Fixed to use actual `đ` |
| **Partner banners no color** | Missing CSS for `.partner-banner__bg` positioning | Added `position:absolute; inset:0; border-radius:8px` |

### **Thymeleaf 3.1 Security Rule (CRITICAL)**

**❌ KHÔNG dùng `th:onclick` với bất kỳ string variable nào:**
```html
<!-- SAI: Thymeleaf 3.1 blocks string variables in event handlers -->
<button th:onclick="|func('${stringVar}', ${numVar})|">

<!-- ĐÚNG: dùng data-* attributes + plain onclick -->
<button th:attr="data-name=${stringVar},data-val=${numVar}"
        onclick="func(this.dataset.name, Number(this.dataset.val))">
```

### **Principal Name Lookup Rule (CRITICAL)**

`CustomUserDetailsService.loadUserByUsername()` returns a Spring Security `User` with `.username(entity.getUsername())`. Therefore `authentication.getName()` / `principal.getName()` returns the **username** (not email).

**✅ LUÔN thử cả username và email khi lookup user từ principal:**
```java
User user = userRepository.findByUsername(principal.getName()).orElse(null);
if (user == null) user = userRepository.findByEmail(principal.getName()).orElse(null);
```

### **Buyer Flow (Complete)**

```
Home/Category → Deal Detail
    ↓ "Thêm vào giỏ" hoặc "Mua ngay"
Cart (/buyer/cart)
    ↓ "Thanh toán"
Checkout (/buyer/checkout) — Xác nhận đơn hàng
    ↓ "Chọn phương thức thanh toán"
Payment (/buyer/payment) — Chọn chuyển khoản ngân hàng (PayOS)
    ↓ "Xác nhận đặt hàng" → POST /buyer/checkout/confirm
Order Complete (/buyer/order-complete) — Thành công + hướng dẫn pickup
    ↓
Orders (/buyer/orders) — Theo dõi trạng thái đơn
```

### **Những việc còn thiếu (TODO)**

1. **Order Detail page** (`/buyer/orders/{id}`) — Chưa có template, đang redirect về list
2. **Credit Card & MoMo Payment** — UI đã có nhưng đang disabled ("Tính năng đang được phát triển")
3. **Notification System** — Email/Push notifications chưa hoàn thiện

### **Milestones Mới**

- [x] **M6.18: Buyer Flow Complete + Category Redesign + Cart Buttons** (Completed 2026-05-21)
- [x] **M6.19: Order Tracking + Security Fixes + Thymeleaf 3.1 Migration** (Completed 2026-05-22)

---

## 🔥 Recent Updates (2026-05-24) — Cart DB + Payment UX + Product Detail

### **Tổng quan — 3 nhóm thay đổi lớn**

**A. Payment UX — Xóa tiền mặt, thêm thẻ tín dụng, sync PayOS**

| File | Thay đổi |
|---|---|
| `buyer/payment.html` | Xóa Cash, thêm Credit Card (disabled + note "Tính năng đang được phát triển"), MoMo disabled, Bank Transfer = default + "Khuyên dùng" |
| `buyer/order-complete.html` | Ticket hiển thị payment status badge (UNPAID/PAID) + payment method info (PayOS), QR nhận hàng + OTP, hướng dẫn 4 bước (bao gồm thanh toán PayOS tại cửa hàng) |
| `HomeController.java` | Default payment method = `BANK_TRANSFER` |
| `order-detail.html`, `seller/orders.html`, `staff/orders.html` | Thêm `CREDIT_CARD` label, cập nhật payLabels |

**B. Cart quantity validation — chặn vượt stock**

| File | Thay đổi |
|---|---|
| `HomeController.java` `POST /api/cart/add` | Check stock trước khi thêm/tăng SL, trả lỗi nếu hết hàng |
| `HomeController.java` `POST /api/cart/update` | Check `quantity <= maxStock`, từ chối nếu vượt |
| `HomeController.java` `GET /api/cart` | Backfill `maxStock` cho mỗi item |
| `cart.html` | Hiển thị "Kho: X sản phẩm", disable nút + khi đạt max, toast lỗi đỏ |
| `home.html`, `category.html` | `addToCart`/`buyNow` handle error response, toast đỏ khi lỗi, `buyNow` không redirect nếu thất bại |

**C. Cart DB persistence + product validation + product detail page**

| File | Thay đổi |
|---|---|
| `CartService.java` **(NEW)** | toàn bộ business logic: `getOrCreateCart`, `migrateSessionCart`, `addItem`, `removeItem`, `updateItemQuantity`, `getCartData` (validate product), `clearCart` |
| `CartRepository.java` **(NEW)** | `findByUser()`, `findByUserUserId()` |
| `CartItemRepository.java` **(NEW)** | `findByCart()`, `deleteByCart()` |
| `V8__create_cart_tables.sql` **(NEW)** | Migration tạo bảng `carts` và `cart_items` |
| `Cart.java` | Thêm `@JsonIgnore` trên `cartItems` |
| `CartItem.java` | Thêm `@JsonIgnore` trên `cart`, `product` |
| `HomeController.java` | 5 cart API endpoints viết lại sang DB; `home()` filter bằng `isAvailable()`; `categoryPage()` dùng `findAvailableByCategory()`; checkout đọc cart từ DB |
| `ProductRepository.java` | Thêm `AND p.approvalStatus = 'APPROVED'` vào 4 query buyer-facing |
| `Product.java` | `isAvailable()` thêm check `approvalStatus` |
| `buyer/product-detail.html` **(NEW)** | Ảnh, tên, mô tả, giá (có deal info nếu có), stock, HSD, store card (logo, tier, rating, address), nút Thêm vào giỏ / Mua ngay |
| `HomeController.java` `GET /products/{productId}` **(NEW)** | Endpoint chi tiết sản phẩm cho buyer |
| `SecurityConfig.java` | Thêm `/products/**` vào public routes |
| `cart.html` | Xử lý unavailable items: grey out, text cảnh báo đỏ, "Kho: 0 sản phẩm", chỉ có nút xóa |

---

## 🔥 Recent Updates (2026-05-28) — Home Quick Filter → Scroll Navigation

### **Thay đổi**

Quick filter chips trên trang chủ buyer trước đây dùng JS để filter/hiển thị/ẩn từng `.deal-card` dựa trên `data-type`. Cách này gây nhầm lẫn vì khi chọn "Flash Sale", toàn bộ card không phải Flash Sale trên trang đều bị ẩn (bao gồm cả section Deal Nổi Bật và Combo).

**Fix:** Chuyển từ filter JS sang scroll navigation — không thay đổi dữ liệu hiển thị, chỉ cuộn trang đến section tương ứng.

| File | Thay đổi |
|---|---|
| `buyer/home.html` | Xóa chip "Voucher" và "Seasonal". Chip "Tất cả" → `scrollTo('all')` cuộn lên đầu trang. Chip "Flash Sale" → `scrollTo('flash')` cuộn đến `#flashGrid`. Chip "Combo" → `scrollTo('combo')` cuộn đến `#comboGrid`. Xóa hàm `filterProducts()`, thay bằng `scrollToSection()`. |

### **Hành vi mới**
- **Tất cả**: giữ nguyên toàn bộ sản phẩm, cuộn lên đầu trang
- **Flash Sale**: cuộn xuống section Sản phẩm Flash Sale (`#flashGrid`)
- **Combo**: cuộn xuống section Sản phẩm Combo (`#comboGrid`)
- Controller/backend không thay đổi — dữ liệu vẫn load đầy đủ như "Tất cả"

---

## 🔥 Recent Updates (2026-05-28 → 2026-05-30) — Phiên Claude Code Lớn

### Tổng quan — 3 ngày, 30+ file changes

Phiên làm việc kéo dài tập trung vào: Notification System, Deal Map cải tiến, Search Overlay Shopee-style, Profile Edit + Address Book, Sửa lỗi JPA TransactionSystemException, PayOS QR, Auto-cancel orders, Store page redesign.

### A. Notification System
| File | Thay đổi |
|------|----------|
| `NotificationService.java` **(NEW)** | CRUD notifications, `notifyOrderStatusChange()`, `notifyNewDealNearby()` |
| `HomeController.java` | +4 API: list, read, read-all, count; `@ModelAttribute addNotifCount()` cho tất cả buyer pages |
| `common/buyer/header.html` | Bell icon + dropdown popup (20 TB, icon theo loại, chấm xanh, time ago), auto-refresh 60s |

### B. Deal Map Cải Tiến
| File | Thay đổi |
|------|----------|
| `buyer/deal-map.html` | **Rewrite**: bỏ custom map-header, dùng shared header + mobile-nav. Floating controls (left: back+locate, right: filter+zoom+radius). Auto-locate GPS 800ms. **Filter panel hoạt động**: sort (nearest/urgency/discount), radius (0.5-5km), time remaining, price range, discount % |
| `HomeController.java` | `activeNav="map"`, notifCount tự động |

### C. Search Overlay (Shopee-style)
| File | Thay đổi |
|------|----------|
| `common/buyer/search-overlay.html` | Dropdown gợi ý khi gõ ≥2 ký tự. Default: chips "Tìm kiếm phổ biến" |
| `common/buyer/header.html` | `fetchSuggestions()` → `GET /api/search/suggest?q=` → render SP + Store |
| `HomeController.java` | `GET /api/search/suggest`: 4 SP + 3 Store |
| `buyer/search.html` | **Rewrite**: Filter bar (deal type + price range + sort). Cards nhỏ hơn (auto-fill minmax 150px). Store results cùng padding |

### D. Profile Edit + Address Book
| File | Thay đổi |
|------|----------|
| `buyer/profile.html` | Thông tin cá nhân (expandable: edit + avatar upload). Đổi mật khẩu (BCrypt). **Sổ địa chỉ** (CRUD, set default, bottom sheet modal) |
| `HomeController.java` | +7 endpoints: update profile, change password, avatar upload, address CRUD |
| `UserAddress.java` **(NEW)** | Entity: name, phone, address, city, district, ward, isDefault |
| `UserAddressRepository.java` **(NEW)** | `findByUserUserIdOrderByIsDefaultDesc`, `clearDefault()` |
| `V9__create_user_addresses.sql` **(NEW)** | Migration |

### E. Sửa Lỗi JPA TransactionSystemException (Quan Trọng)
**Root cause:** `orderRepository.save(order)` và `productRepository.save(product)` gây JPA cascade flush → `TransactionSystemException` khi entity có `version=null` hoặc lazy collection lỗi.

**Fix:** Tất cả status update dùng `@Modifying` query UPDATE trực tiếp thay vì `save()`:

| File | Thay đổi |
|------|----------|
| `OrderRepository.java` | +4 `@Modifying(clearAutomatically=true)` queries: `updateOrderStatus`, `updateOrderQrCode`, `updateOrderPickupTime`, `cancelOrder` |
| `ProductRepository.java` | +1 `@Modifying updateProductFields()` |
| `SellerController.java` | `updateOrderStatus` + `editProduct` → dùng `@Modifying` query + `@Transactional` |
| `StaffController.java` | `updateOrderStatus` → dùng `@Modifying` query + `@Transactional` |

**Quy tắc mới:** Mọi POST endpoint cập nhật entity dùng `@Modifying` query + `@Transactional` thay vì JPA `save()`.

### F. Order Auto-Cancel Service
| File | Thay đổi |
|------|----------|
| `OrderAutoCancelService.java` **(NEW)** | `@Scheduled(fixedRate=30min)`: hủy PENDING >60 phút + READY >2 ngày + SP hết hạn. Hoàn stock. `@Modifying cancelOrder()` + `@Transactional` trên package-private method |
| `SellerController.java` | `cancelOrdersWithProduct()`: khi edit SP → PENDING, hủy đơn chứa SP + notify buyer |

### G. PayOS QR Improvements
| File | Thay đổi |
|------|----------|
| `PayOSService.java` | `cancelPaymentLink(orderId)`: gọi PayOS API cancel khi order bị hủy. `fetchPaymentInfo()`: check trạng thái payment cũ. **Chống trùng QR**: nếu đã có orderCode → check PAID → reuse hoặc báo "đã thanh toán" |
| `SellerController.java` | Gọi `payOSService.cancelPaymentLink()` khi order → CANCELLED |
| `StaffController.java` | Gọi `payOSService.cancelPaymentLink()` khi order → CANCELLED |

### H. Store Page Buyer Redesign
| File | Thay đổi |
|------|----------|
| `buyer/store.html` | **Rewrite products section**: card giống category page (`.prod-card` từ home.css), deal pricing, badge giảm giá, "+ Giỏ" + "Mua" buttons. Search + sort (Mặc định/Giá↑/Giá↓/Tên A-Z). Dùng shared header + mobile-nav |
| `HomeController.java` | `storePage()` build `dealProductMap` cho pricing display |
| `buyer/store.html` | Thêm `addToCartStore()`, `buyNowStore()`, `showStoreToast()`, `updateCartBadge()` JS |

### I. Fixes & Polish
| Issue | Fix |
|-------|-----|
| Staff dashboard không hiện PENDING orders | Sửa slot filter: `scheduledPickupTime == null` → hiện trong tất cả slot |
| Staff/Seller JS "not valid JSON" | Check `content-type` trước `r.json()`, báo "Phiên hết hạn" nếu HTML |
| Seller dashboard badge | Đồng bộ `.badge-pending/confirmed/ready/completed/cancelled` giống orders page |
| Staff nav badge bị trôi | `.nav-item` thêm `position:relative` |
| Store page thiếu header/nav | Thêm shared `common/buyer/header` + `mobile-nav` |
| Mobile nav không sáng tab | Set `activeNav` cho tất cả trang (home/map/cart/orders/account) |
| Tiếng Việt thiếu dấu | Fix search.html (13 lỗi), store-profile.html (8 lỗi) |
| Deal hết hạn vẫn hiển thị trong seller product detail | Thêm check `startTime <= now && endTime >= now` |
| Deals không tự ENDED khi quá hạn | `ExpiryCountdownService.endExpiredDeals()` @Scheduled 30 phút |

### J. Documentation
| File | Thay đổi |
|------|----------|
| `DEALXANH_BA_DOCS.md` **(NEW)** | Tài liệu đặc tả nghiệp vụ 23 sections (domain model, RBAC, state machines, luồng, API, validate, filters, DB, security, site map, dev timeline) |
| `CLAUDE.md` | Section này |

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
- [ ] Pipe syntax `|...|` cho `th:text` tiếng Việt — KHÔNG nested single quotes
- [ ] `event.stopPropagation()` trong button nằm trong clickable card
- [ ] `new HashMap<>()` khi cần >10 cặp (không dùng `Map.of()`)
- [ ] Null-safe + empty string check trước `LocalDateTime.parse()`
- [ ] **KHÔNG dùng `th:onclick` với string variable** — dùng `th:attr="data-*="` + `onclick` đọc `this.dataset.*` (Thymeleaf 3.1 rule)
- [ ] **`getCurrentUser()` phải thử cả `findByUsername` VÀ `findByEmail`** (principal name là username, không phải email)
- [ ] Run migration SQL (nếu có schema change)

---

## 📊 Completion Status by Module

| Module | Backend | Frontend | Testing | Overall |
|--------|---------|----------|---------|---------|
| Authentication | 100% | 100% | 90% | **95%** |
| Admin Panel | 92% | 88% | 70% | **87%** |
| Seller Panel | 82% | 72% | 50% | **72%** |
| Buyer Interface | 95% | 92% | 60% | **88%** |
| Deal System | 100% | 98% | 85% | **98%** |
| Product System | 98% | 90% | 70% | **88%** |
| Store System | 92% | 85% | 60% | **82%** |
| Order System | 70% | 60% | 35% | **58%** |
| Payment System | 40% | 45% | 15% | **35%** |
| Cart System | 100% | 100% | 60% | **95%** |
| Notification | 85% | 80% | 40% | **75%** |
| Address Book | 100% | 90% | 50% | **85%** |
| Auto-Cancel | 90% | 0% | 30% | **50%** |

**Overall Project Completion: ~94%**

---

---

## 🔥 Recent Updates (2026-05-31 → 2026-06-01) — Mega Session

### Tổng quan — 20+ file changes, 3 services mới, 2 tài liệu mới

Phiên làm việc tập trung vào: Buyer Prepay + Pickup Time, Cart Checkboxes, Deal FIXED/PERCENT Logic Fix, Location-Based Sorting, Distance Warning, Deal System Audit.

---

### A. Buyer Prepay + Pickup Time Slot (Phase 1-6)

**Flow mới:**
```
Buyer checkout → chọn giờ nhận hàng → đặt đơn → thấy QR PayOS
→ thanh toán ngay qua app bank → order auto READY_FOR_PICKUP
→ seller thấy "Hoàn thành" (chỉ trong khung giờ pickup)
```

| File | Thay đổi |
|------|----------|
| `payment.html` | Thêm input `datetime-local` chọn giờ pickup (min=tomorrow, max=+3 ngày). Style đồng bộ với `.pay-section` card |
| `order-complete.html` | Thêm section "Thanh toán trước qua PayOS" với QR code, countdown 30ph, auto-poll 5s |
| `HomeController.java` | `confirmCheckout()` nhận `scheduledPickupTime`, lưu vào Order. `checkout()` GET/POST tính distance + FIXED deal logic |
| `PaymentController.java` | Thêm `POST /api/payment/buyer-create/{orderId}` — buyer tự tạo QR không cần READY |
| `PayOSService.java` | Branch `confirmPaymentAndDeductStock()`: PENDING/CONFIRMED → READY (prepay), READY → COMPLETED (at-store). `createSaleTransaction()` → public |
| `seller/orders.html` | Button: CONFIRMED+PAID → Hoàn thành, READY+PAID → Hoàn thành (skip QR) |
| `staff/orders.html` | Tương tự seller |
| `SellerController.java` | +Pickup time validation (30ph before → 2h after). +`deductStockAndCreateTransaction()` |
| `StaffController.java` | Tương tự seller |

### B. Cart — Checkboxes & Selective Checkout

| File | Thay đổi |
|------|----------|
| `cart.html` | Thêm checkbox "Chọn tất cả" + checkbox từng item. `recalcCart()` tính lại tổng theo SP được chọn. `saveSelectedItems()` gọi API lưu selection trước checkout |
| `HomeController.java` | `POST /api/cart/checkout-selection` lưu selected IDs vào session. `checkout()` GET filter cart theo selection |

### C. Deal FIXED/PERCENT Logic — Fix Toàn Diện

**Vấn đề:** FIXED deal (giảm theo tiền) bị nhân theo số lượng SP → giảm 20k mua 2 SP thành giảm 40k.

**Fix:**
- FIXED deal: **giữ salePrice = originalPrice** trong cart, **áp 1 lần** khi checkout (gom unique dealId)
- PERCENT deal: áp theo từng SP như cũ
- CODE_REQUIRED (voucher): **không auto-apply** vào giá, hiển thị badge vàng "Cần nhập mã"
- AUTO_APPLY: auto-apply vào giá, hiển thị badge xanh

| File | Thay đổi |
|------|----------|
| `CartService.java` | DealProduct: check `applyMethod`, FIXED giữ originalPrice. DealCategory: AUTO_APPLY mới giảm giá, CODE_REQUIRED giữ nguyên. Thêm `applyMethod` vào item map |
| `HomeController.java` | Checkout GET/POST: gom unique FIXED dealId → cộng discountValue 1 lần. PERCENT vẫn per-item |

**Cart Breakdown mới (gom nhóm theo deal):**
```
┌─ Sale Hè 2026 ─────────── 🟢 Giảm 25% ─┐  ← AUTO_APPLY
│ Trà sữa (Shop A)       -6,000đ (15%)    │
│ Tổng giảm: -10,500đ                     │
└──────────────────────────────────────────┘
┌─ Voucher 20K Khai Trương ─ 🟡 Cần nhập mã ┐  ← CODE_REQUIRED
│ Cơm tấm (Store 3)           40,000đ/sp   │
│ Nhập mã để được giảm 20,000đ             │
└──────────────────────────────────────────┘
```

### D. Location-Based Features

| File | Thay đổi |
|------|----------|
| `LocationService.java` **(NEW)** | IP geolocation (ipapi.co) + Nominatim geocode + Haversine distance |
| `HomeController.java` | `home()` sort SP theo khoảng cách gần nhất. `checkout()` tính distance + cảnh báo >10km |
| `order-summary.html` | Cảnh báo vàng khi store cách >10km: "Lưu ý: Cửa hàng cách bạn ~X km..." |

### E. Scheduled Tasks — Bug Fixes

| File | Thay đổi |
|------|----------|
| `OrderAutoCancelService.java` | Fix self-invocation: self-inject `@Lazy`. Dùng `findByIdWithItems` + snapshot trước `@Modifying`. PENDING timeout: 60ph → **30ph**. Dùng `@Modifying restoreStock()` thay vì entity save |
| `ProductRepository.java` | Thêm `restoreStock()` — `@Modifying` query hoàn stock |
| `ExpiryCountdownService.java` | `endExpiredDeals()` dùng `@Modifying endExpiredActiveDeals()` thay vì entity save |
| `DealRepository.java` | Thêm `endExpiredActiveDeals()` — `@Modifying` UPDATE ACTIVE→ENDED |

### F. Seller Registration — Lock Province

| File | Thay đổi |
|------|----------|
| `register-seller.html` | Province dropdown: chỉ load "Hà Nội", disable, auto-select |
| `seller-onboarding.html` | Hardcode Hà Nội, disable select |

### G. Cart Voucher Bug Fix

| File | Thay đổi |
|------|----------|
| `HomeController.java` | `applyVoucher()` đọc cart từ DB (`CartService`) thay vì session → fix "giỏ hàng trống" |

### H. Security — Session Expiry Redirect

| File | Thay đổi |
|------|----------|
| `CustomInvalidSessionStrategy.java` | Thêm `&redirect=<original_url>` vào login URL |
| `CustomAuthenticationSuccessHandler.java` | Check tham số `redirect` → redirect về trang gốc sau login |

### I. Remove Original Price (Strikethrough)

| File | Thay đổi |
|------|----------|
| `home.html`, `category.html`, `search.html`, `store.html`, `deal-detail.html`, `product-detail.html`, `cart.html`, `order-summary.html`, `deal-map.html` | Xóa `<span class="deal-card__original">`, `<span class="prod-card__original">`, `<span class="ci-original">`, `<span class="os-item__original">`, v.v. |

### J. Documentation (New Files)

| File | Nội dung |
|------|----------|
| `DEAL_SYSTEM.md` **(NEW)** | Tài liệu toàn bộ hệ thống Deal: kiến trúc 2 tầng, entity, luồng hoạt động, trạng thái, validation rules, ảnh hưởng |
| `DEAL_AUDIT.md` **(NEW)** | Audit 12 deals trong DB: phát hiện bug (deal hết hạn vẫn ACTIVE, FIXED pricing sai, SCHEDULED không tự ACTIVE), đề xuất sửa |

---

## 🔐 Quy tắc CRITICAL (added 2026-06-01)

### **36. LUÔN phân biệt AUTO_APPLY vs CODE_REQUIRED trong CartService**

```java
// ✅ ĐÚNG: AUTO_APPLY mới auto-giảm giá. CODE_REQUIRED giữ originalPrice
if ("AUTO_APPLY".equals(deal.getApplyMethod())) {
    salePrice = calculateDiscountPrice(deal, originalPrice);
} else {
    salePrice = originalPrice; // Voucher phải do buyer chọn thủ công
}
item.put("applyMethod", deal.getApplyMethod()); // Luôn truyền để frontend hiển thị đúng
```

### **37. FIXED deal — áp 1 LẦN, không nhân theo số lượng SP**

```java
// ✅ ĐÚNG: Gom unique dealId, cộng discountValue 1 lần
Set<Long> appliedFixedDealIds = new HashSet<>();
double fixedDealDiscount = 0;
for (item : items) {
    if ("FIXED".equals(item.discountType) && !appliedFixedDealIds.contains(item.dealId)) {
        fixedDealDiscount += item.discountValue;
        appliedFixedDealIds.add(item.dealId);
    } else if (!"FIXED".equals(item.discountType)) {
        discountAmount += (originalPrice - salePrice) * quantity;
    }
}
// ❌ SAI: discountAmount += (originalPrice - salePrice) * quantity cho mọi item
```

### **38. @Modifying query LUÔN cần @Transactional — tránh self-invocation**

```java
// ✅ ĐÚNG: Self-inject + gọi qua proxy
@Autowired @Lazy private MyService self;
self.transactionalMethod(); // Đi qua AOP proxy

// ❌ SAI: Gọi nội bộ → @Transactional bị bypass
this.transactionalMethod(); // KHÔNG qua proxy!
```

### **39. Cart page — th:id cho dynamic IDs trong Thymeleaf**

```html
<!-- ✅ ĐÚNG: -->
<div th:id="'qrBox_' + ${order.orderId}">
<!-- ❌ SAI: Thymeleaf không parse ${} trong attribute thường -->
<div id="qrBox_${order.orderId}">
```

### **40. Deal breakdown trong cart — gom nhóm theo dealId**

Luôn hiển thị breakdown gom nhóm theo deal:
- AUTO_APPLY: badge xanh, hiện % và số tiền giảm
- CODE_REQUIRED: badge vàng "Cần nhập mã", hiện giá gốc, gợi ý nhập mã

### **41. Khóa tỉnh Hà Nội cho seller registration**

```javascript
// register-seller.html: chỉ load Hà Nội, disable select
const haNoi = data.find(p => p.name === 'Hà Nội');
citySelect.appendChild(opt);
citySelect.value = haNoi.name;
citySelect.disabled = true;
```

### **42. Seller chỉ hoàn thành đơn trong khung giờ pickup**

```java
if ("COMPLETED".equals(newStatus) && order.getScheduledPickupTime() != null) {
    LocalDateTime windowStart = pickupTime.minusMinutes(30);
    LocalDateTime windowEnd = pickupTime.plusHours(2);
    if (now.isBefore(windowStart)) return error("Chưa đến giờ nhận hàng");
    if (now.isAfter(windowEnd)) return error("Đã quá giờ nhận hàng");
}
```

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
- [x] **M6.16: Buyer Home Page Full Rewrite + Product-Centric Data** (Completed 2026-05-21)
- [x] **M6.17: Deal Detail + Cart System + Category Redesign + Weak Password** (Completed 2026-05-21)
- [x] **M7: Order Processing** (Completed 2026-05-24)
- [x] **M8: PayOS Payment Integration** (Completed 2026-05-24)
- [x] **M8.1: Payment UX — Remove Cash + Credit Card UI + PayOS Sync** (Completed 2026-05-24)
- [x] **M8.2: Cart DB Persistence + Stock Validation + Product Validation** (Completed 2026-05-24)
- [x] **M8.3: Buyer Product Detail Page + Home/Category Filter Fix** (Completed 2026-05-24)
- [x] **M9: Notification System + Search Overlay + Profile Edit** (Completed 2026-05-28)
- [x] **M10: Order Auto-Cancel + PayOS Fixes + Store Page Redesign** (Completed 2026-05-30)
- [x] **M11: Buyer Prepay + Pickup Time + Deal Logic Fix** (Completed 2026-06-01)
- [x] **M12: Location-Based Sorting + Cart Checkboxes + System Audit** (Completed 2026-06-01)
- [ ] **M13: Deal System Hardening** (Pending — auto-activate SCHEDULED, usageCount tracking, platform overlap warning)
- [ ] **M14: Analytics & Reports** (Not Started)

---

## 🐛 Bug Tracking

### **Fixed:**
1. ✅ Thymeleaf `#temporals.duration()` error
2. ✅ Repository method naming inconsistency
3. ✅ User `getStore()` vs `getWorkStore()` confusion
4. ✅ Missing repository methods
5. ✅ POST /buyer/checkout/confirm tạo Order rỗng không OrderItem → đã fix tạo OrderItem + tính amounts
6. ✅ Cart NaNđ + scroll bị mobile-nav che → fix padding + bottom:72px cho sticky bars
7. ✅ Thymeleaf `${}` trong JS template literal xung đột → dùng string concat thay vì backtick
8. ✅ BUG: Thymeleaf JS inline URL backslash — dùng forward slash `/`
9. ✅ FIXED deal discount bị nhân theo số lượng SP → gom unique dealId, áp 1 lần
10. ✅ Voucher (CODE_REQUIRED) bị auto-apply vào cart → chỉ AUTO_APPLY mới auto-giảm
11. ✅ Cart voucher "giỏ hàng trống" → đọc cart từ DB thay vì session
12. ✅ Auto-cancel scheduler lỗi version=null + LazyInitializationException → dùng @Modifying query
13. ✅ ExpiryCountdownService lỗi detached entity → dùng @Modifying endExpiredActiveDeals()
14. ✅ Cart QR IDs không render → dùng th:id thay vì plain id
15. ✅ Seller registration province → khóa Hà Nội, disable select
16. ✅ Session expiry redirect → lưu original URL, redirect về đúng trang sau login

### **Known Issues:**
1. ⚠️ PayOS webhook không tới được localhost — dùng polling qua nút "Kiểm tra trạng thái"
2. ⚠️ QR code dùng api.qrserver.com (third-party) — nên tự generate QR bằng thư viện Java
3. ⚠️ No SCHEDULED→ACTIVE auto-transition (deals stay SCHEDULED past startTime)
4. ⚠️ usageCount không tự động tăng khi checkout (maxUsageCount, usagePerUser vô dụng)
5. ⚠️ Platform deals chồng lấn danh mục không có cảnh báo khi admin tạo
6. ⚠️ Mobile responsive needs improvement

---

## 📞 Support

**Project:** DealXanh O2O Deal Marketplace
**Tech Lead:** [Your Name]
**Documentation:** CLAUDE.md (this file)
**Last Updated:** 2026-06-01

---

## 🔗 Related Documentation

- `CODE_DUPLICATION_ANALYSIS.md` - Sidebar duplication analysis
- `PROJECT_DEEP_DIVE.md` - Comprehensive project docs
- Memory System: `C:\Users\ACER\.claude\projects\d--exe201-deal-xanh\memory\`
