# DealXanh Project - Deep Dive Analysis

## 🎯 Project Overview

**DealXanh** là một O2O (Online-to-Offline) Marketplace application được thiết kế để "cứu cứu" đồ ăn cận date, giúp người dùng tiết kiệm đến 70% chi phí ăn uống hàng ngày với các món ăn chất lượng cao từ nhà hàng, quán ăn uy tín.

### 📊 Project Metadata
- **Tên dự án**: DealXanh
- **Mô hình**: O2O Marketplace for Near-Expiry Food
- **Version**: 0.0.1-SNAPSHOT
- **Current Branch**: PhanAnh
- **Main Branch**: main
- **Framework**: Spring Boot 3.2.3

---

## 🛠️ Tech Stack Analysis

### Backend Technologies
```yaml
Core Framework: Spring Boot 3.2.3
Language: Java 17
Build Tool: Maven
Database: MySQL (MySQL Connector J)
ORM: JPA/Hibernate
```

### Security & Authentication
```yaml
Authentication: Spring Security
OAuth2: Google Login integration
JWT: JSON Web Token (jjwt 0.11.5)
Password Encoder: BCrypt
```

### Frontend Technologies
```yaml
Template Engine: Thymeleaf
CSS: Custom CSS with CSS Variables
JavaScript: Vanilla JavaScript (ES6+)
Architecture: Component-based with fragments
```

### Additional Libraries
```yaml
Email: Spring Boot Starter Mail (Gmail SMTP)
Validation: Spring Boot Starter Validation
API Documentation: SpringDoc OpenAPI 3
Development: Spring Boot DevTools (hot reload)
Utilities: Lombok (boilerplate reduction)
```

---

## 🏗️ Architecture & Code Organization

### 1. Layer Architecture
```
├── Controller Layer (controllers/)
│   ├── api/ (REST API endpoints)
│   ├── auth/ (Authentication controllers)
│   ├── admin/ (Admin dashboard controllers)
│   └── util/ (Utility controllers)
├── Service Layer (service/)
│   ├── EmailService
│   └── OtpService
├── Repository Layer (repository/)
│   └── JPA Repositories for data access
├── Entity Layer (entity/)
│   └── Domain models with JPA annotations
├── Security Layer (security/)
│   ├── CustomUserDetailsService
│   ├── CustomAuthenticationSuccessHandler
│   ├── CustomAuthenticationFailureHandler
│   └── CustomOAuth2UserService
├── DTO Layer (dto/)
│   ├── request/ (Request DTOs)
│   └── response/ (Response DTOs)
└── Configuration Layer (config/)
    ├── SecurityConfig
    └── WebMvcConfig
```

### 2. Directory Structure Analysis
```
src/
├── main/
│   ├── java/com/dealxanh/app/
│   │   ├── controller/         # Request handlers
│   │   ├── service/            # Business logic
│   │   ├── repository/         # Data access
│   │   ├── entity/             # Domain models
│   │   ├── security/           # Security configuration
│   │   ├── config/             # App configuration
│   │   ├── dto/                # Data transfer objects
│   │   ├── exception/          # Custom exceptions
│   │   ├── constant/           # Application constants
│   │   └── util/               # Utility classes
│   └── resources/
│       ├── application.properties  # Configuration
│       ├── templates/             # Thymeleaf templates
│       │   ├── buyer/             # Buyer-facing pages
│       │   ├── seller/            # Seller-facing pages
│       │   ├── admin/             # Admin dashboard
│       │   ├── moderator/         # Moderator pages
│       │   ├── auth/              # Authentication pages
│       │   ├── common/            # Shared fragments
│       │   └── error/             # Error pages
│       └── static/                # Static resources
│           ├── css/               # Stylesheets
│           │   ├── variables.css  # CSS variables
│           │   ├── reset.css      # CSS reset
│           │   ├── components.css # Component styles
│           │   ├── layout.css     # Layout styles
│           │   └── pages/         # Page-specific styles
│           └── js/                # JavaScript files
│               ├── api.js         # API client with mock data
│               ├── component-loader.js  # Dynamic component loading
│               ├── i18n.js        # Internationalization
│               ├── utils.js       # Utility functions
│               └── pages/         # Page-specific scripts
└── test/                          # Test files
```

---

## 🎨 Frontend Architecture - YOUR APPROACH

### 🏗️ **Fragment-Based Architecture (Your Signature Style)**

Bạn đã thiết kế một hệ thống fragment-based architecture cực kỳ modular và reusable:

#### **Fragment Structure**
```
templates/common/admin/
├── admin-layout.html    # Layout wrapper with th:replace pattern
├── sidebar.html          # Reusable sidebar component
├── header.html           # Reusable header component  
├── css.html              # Shared CSS fragment
└── js.html               # Shared JS fragment
```

#### **Layout Pattern - YOUR INNOVATION**
**File**: `common/admin/admin-layout.html`
```html
<body th:fragment="layout(activeSidebar, pageTitle, showDatePicker)">
<div class="layout">
    <!-- Dynamic sidebar with active state -->
    <div th:replace="~{common/admin/sidebar :: sidebar(${activeSidebar})}"></div>
    
    <div class="main">
        <!-- Dynamic header with parameters -->
        <div th:replace="~{common/admin/header :: header(${pageTitle}, ${showDatePicker})}"></div>
        
        <!-- Page content gets inserted here -->
        <div th:replace="~{__*__}"></div>
    </div>
</div>
</body>
```

**Usage in Pages**:
```html
<div th:replace="~{common/admin/admin-layout :: layout(
    activeSidebar='users',
    pageTitle='Quản lý Người dùng',
    showDatePicker=false
)}">
    <!-- Your page content here -->
</div>
```

### 🎨 **Your CSS Architecture**

#### **CSS Variables System**
Bạn định nghĩa tất cả design tokens trong `:root`:
```css
:root {
    /* Brand Colors */
    --primary: #0d5c2e;
    --primary-dark: #08421f;
    --primary-light: #1a7c40;
    --accent: #d4af37;
    
    /* Surface Colors */
    --surface: #fff;
    --bg: #f0f2f5;
    
    /* Text Colors */
    --text-primary: #1a1a1a;
    --text-secondary: #666;
    --text-tertiary: #999;
    
    /* Semantic Colors */
    --danger: #ef4444;
    --warning: #f59e0b;
    --success: #10b981;
    --info: #3b82f6;
    
    /* Layout */
    --sidebar: 240px;
    --radius-md: 10px;
    --tr: 200ms ease-out;
}
```

#### **Component Styling Approach**
- **KPI Cards**: `.kpi-card` với hover effects
- **Buttons**: `.btn-primary`, `.btn-outline`, `.btn-danger` với transitions
- **Badges**: `.badge-success`, `.badge-warning`, `.badge-danger` cho status indicators
- **Tables**: `.table` với hover states và responsive design

### 🔧 **Your JavaScript Patterns**

#### **Real-Time Search Pattern**
```javascript
// Debounce search with server redirect
let searchTimeout;
searchInput.addEventListener('input', (e) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        const url = new URL(window.location);
        url.searchParams.set('search', e.target.value.trim());
        window.location.href = url.toString(); // Server-side filtering
    }, 500);
});
```

#### **Dynamic Form Handling**
```javascript
// Preserve filters across form submissions
const approveInputs = [
    {name: 'currentStatus', value: currentFilters.status},
    {name: 'search', value: currentFilters.search},
    {name: 'city', value: currentFilters.city},
    {name: 'sort', value: currentFilters.sort},
    {name: 'page', value: currentPage.toString()}
];
```

#### **Modal Pattern**
```javascript
// User detail modal with dynamic content
function showUserDetail(userId) {
    const user = usersData.find(u => u.userId === userId);
    // Populate modal fields
    // Show modal with display: flex
    // Add overflow: hidden to body
}
```

### 📱 **Your Layout Structure**

#### **Admin Layout Grid**
```
┌─────────────────────────────────────────────────────────┐
│  Sidebar (240px, fixed)  │  Main Content (auto)         │
│  ┌───────────────────── │  ┌────────────────────────┐ │
│  │ Logo & Brand         │  │  Topbar (sticky)        │ │
│  ├───────────────────── │  ├────────────────────────┤ │
│  │ Navigation Menu      │  │  Page Content           │ │
│  │ - Dashboard          │  │  - Statistics Cards    │ │
│  │ - Seller Verify      │  │  - Tables/Filters      │ │
│  │ - Users              │  │  - Detail Panels       │ │
│  │ - Deals              │  │  - Forms               │ │
│  │ - Orders             │  └────────────────────────┘ │
│  │ - Finance             │                            │
│  │ - Dispute            │                            │
│  ├───────────────────── │                            │
│  │ User Profile         │                            │
│  └───────────────────── │                            │
└─────────────────────────────────────────────────────────┘
```

### 🎯 **Your Development Philosophy**

#### **Code Organization Priorities**
1. **Fragments First** → Build reusable components before pages
2. **CSS Variables** → Consistent theming across all pages
3. **Dynamic Parameters** → Flexible fragment composition
4. **Filter Preservation** → Maintain user context across navigation
5. **Real-Time Feedback** → Debounced search and live updates

#### **Naming Conventions**
- **Fragments**: `{page}-{purpose}.html` (e.g., `admin-layout.html`)
- **CSS Classes**: BEM-like pattern (`.kpi-card__title`, `.btn-primary`)
- **JavaScript Functions**: camelCase with descriptive names (`selectStore()`, `toggleReject()`)
- **Thymeleaf Variables**: lowercase with underscores (`current_status`, `search_query`)

### 💡 **Your Smart Implementation Choices**

#### **1. Fragment Parameters Over Hardcoding**
❌ **Instead of**: Hardcoding sidebar in every page
✅ **You chose**: `<div th:replace="~{common/admin/sidebar :: sidebar('dashboard')}"></div>`

#### **2. Server-Side Filtering Over Client-Side**
❌ **Instead of**: Complex JavaScript filtering
✅ **You chose**: Simple debounce + server redirect with URL parameters

#### **3. CSS Variables Over Repeated Colors**
❌ **Instead of**: `color: #0d5c2e;` everywhere
✅ **You chose**: `color: var(--primary);` with consistent theming

#### **4. Hidden Data Storage**
```html
<!-- Store complete data objects in DOM for JavaScript access -->
<div id="storesData" style="display:none;">
    <div th:each="store : ${stores}" 
         th:data-store-id="${store.storeId}"
         th:data-logo="${store.logoUrl}"
         th:data-status="${store.status}">
    </div>
</div>
```

#### **5. Dynamic Form Generation**
```javascript
// Generate forms dynamically with preserved filters
const approveForm = document.getElementById('approveForm');
approveInputs.forEach(input => {
    if (input.value) {
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = input.name;
        hiddenInput.value = input.value;
        approveForm.appendChild(hiddenInput);
    }
});
```

### 🔄 **YOUR WORKFLOW - How You Code**

#### **Phase 1: Foundation** ✅ (COMPLETED)
1. Set up CSS fragments (`css.html`)
2. Create layout wrapper (`admin-layout.html`)
3. Build individual components (`sidebar.html`, `header.html`)
4. Implement JS loader (`js.html`)

#### **Phase 2: Core Pages** ✅ (COMPLETED)
1. **Dashboard** → Statistics + charts + date picker
2. **Users** → Table + filters + search + pagination
3. **Seller Verify** → List + detail panel + approve/reject

#### **Phase 3: Advanced Features** 🚧 (IN PROGRESS)
1. **Deals Management** → CRUD operations (database ready)
2. **Orders Management** → Monitoring + fulfillment (database ready)
3. **Finance** → Revenue tracking (UI ready)

### 📊 **Your Admin Pages - Current Status**

| Page | Route | Status | Features |
|------|-------|--------|----------|
| Dashboard | `/admin/dashboard` | ✅ 100% | KPI cards, charts, date picker, real-time updates |
| Users | `/admin/users` | ✅ 100% | Filtering, search, pagination, modals, CRUD |
| Seller Verify | `/admin/seller-verify` | ✅ 100% | Document viewer, approve/reject, history |
| Deals | `/admin/deals` | 🚧 20% | Basic structure only |
| Orders | `/admin/orders` | 🚧 30% | Statistics + store listing done |
| Finance | `/admin/finance` | 🚧 10% | UI placeholder only |
| Profile | `/admin/profile` | ✅ 80% | Update profile, change password |

### 🎨 **Your UI/UX Design Principles**

#### **Color Strategy**
- **Primary Green** (`#0d5c2e`) - Brand color, main actions
- **Success Green** (`#10b981`) - Active status, approvals
- **Warning Orange** (`#f59e0b`) - Pending status, alerts
- **Danger Red** (`#ef4444`) - Rejected status, destructive actions
- **Info Blue** (`#3b82f6`) - User role, information

#### **Typography**
- **Font**: Inter (Google Fonts)
- **Weights**: 400 (regular), 500 (medium), 600 (semibold), 700 (bold), 800 (extra bold)
- **Hierarchy**: Clear distinction between headings and body text

#### **Spacing System**
- **Card Padding**: 20px
- **Content Padding**: 24px  
- **Gap Between Elements**: 8px, 12px, 16px, 20px (progressive scale)
- **Border Radius**: 6px, 10px, 16px, 20px (progressive scale)

### ⚡ **Performance Optimizations You've Made**

#### **1. CSS Optimization**
- All CSS defined inline in page `<style>` tags
- No external CSS file requests (except Google Fonts)
- CSS variables for consistent theming without repeated values

#### **2. JavaScript Optimization**  
- Debounced search (500ms) to reduce server requests
- Auto-refresh only for dashboard (30-second intervals)
- Event delegation for dynamic elements

#### **3. DOM Manipulation**
- Hidden data storage for JavaScript access
- Modal reuse instead of multiple modal instances
- Dynamic form generation to reduce HTML duplication

### 🔒 **Security Implementation**

#### **CSRF Protection**
```html
<meta name="_csrf" th:content="${_csrf.token}"/>
<meta name="_csrf_header" th:content="${_csrf.headerName}"/>

<script>
const csrfInput = document.createElement('input');
csrfInput.name = csrfHeader.getAttribute('content');
csrfInput.value = csrfToken.getAttribute('content');
form.appendChild(csrfInput);
</script>
```

#### **Role-Based Access Control**
```java
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public String sellerVerify() { ... }
```

#### **Input Validation**
- Server-side validation in controllers
- Client-side validation with HTML5 attributes
- SQL injection prevention with parameterized queries

---

## 🔐 Security Architecture

### 1. Role-Based Access Control (RBAC)
```java
ROLE_USER          → Buyers (Người mua)
ROLE_STORE_OWNER   → Store Owners (Chủ cửa hàng)
ROLE_STORE_STAFF   → Store Staff (Nhân viên)
ROLE_MODERATOR     → Moderators (Kiểm duyệt viên)
ROLE_ADMIN         → Administrators (Quản trị viên)
```

### 2. Authentication Flow
```
1. User Login Request
   ↓
2. CustomUserDetailsService.loadUserByUsername()
   ↓
3. Password Validation (BCrypt)
   ↓
4. CustomAuthenticationSuccessHandler/FailureHandler
   ↓
5. Session Management & JWT Token
   ↓
6. Authorization Check
```

### 3. OAuth2 Integration
- **Provider**: Google
- **Scope**: email, profile
- **Handler**: CustomOAuth2UserService

### 4. Security Configuration
```java
- Form Login with custom handlers
- Session Management (1 session per user)
- Remember Me (7 days)
- CSRF Protection (disabled for API endpoints)
- Logout with session invalidation
```

---

## 💾 Database Schema & Data Model

### Core Entities

#### 1. **User Entity**
```java
- userId (Long, PK)
- username, email, password
- fullName, phone, address
- avatarUrl
- active (Boolean)
- provider, providerId (OAuth2)
- resetToken, resetTokenExpiry
- createdAt, updatedAt
- role_id (FK → Role)
- work_store_id (FK → Store, for staff)
```

#### 2. **Role Entity**
```java
- roleId (Long, PK)
- name (ROLE_USER, ROLE_STORE_OWNER, etc.)
- description
```

#### 3. **Store Entity**
```java
- storeId (Long, PK)
- storeName, description, address
- phone, logoUrl, coverImageUrl
- latitude, longitude
- openTime, closeTime
- averageRating, totalReviews
- status (PENDING, ACTIVE, SUSPENDED, REJECTED)
- approvedBy, rejectedBy (FK → User)
- rejectionReason
- businessType, categories
- city, district
- cccdUrl, businessLicenseUrl, vsattpUrl
- bankName, bankAccountNumber, bankAccountOwner
- operatingDays, pickupSlots
- maxSlotsPerTime, pickupDurationMinutes
- approvalMode
- owner_id (FK → User)
```

#### 4. **Product Entity**
```java
- productId (Long, PK)
- name, description, imageUrl
- originalPrice, dealPrice
- stockQuantity
- productType (SPECIFIC_DEAL, BLIND_BOX)
- expiryDate
- dealStartTime, dealEndTime
- pickupDeadline
- active, deleted
- createdAt, updatedAt
- store_id (FK → Store)
- category_id (FK → Category)
```

#### 5. **Order Entity**
```java
- orderId (Long, PK)
- totalAmount, discountAmount, finalAmount
- couponCode
- status (PENDING, CONFIRMED, READY_FOR_PICKUP, COMPLETED, CANCELLED)
- paymentStatus (UNPAID, PAID)
- paymentMethod (BANK_TRANSFER, CASH, MOMO, ZALOPAY)
- scheduledPickupTime, actualPickupTime
- pickupQrCode
- note, cancellationReason
- createdAt, updatedAt
- user_id (FK → User)
- store_id (FK → Store)
```

#### 6. **OrderItem Entity**
```java
- orderItemId (Long, PK)
- quantity, price
- order_id (FK → Order)
- product_id (FK → Product)
```

### Database Configuration
```properties
Database: MySQL (dealxanh_db)
DDL Mode: update
Timezone: Asia/Ho_Chi_Minh
Character Set: UTF-8
Show SQL: true (debug mode)
```

---

## 🔄 Business Logic & Workflows

### 1. Buyer Registration Flow
```
1. Access /register/buyer
   ↓
2. Fill registration form (fullName, email, phone, password)
   ↓
3. Click "Send OTP" → EmailService.sendOtpEmail()
   ↓
4. Enter received OTP → OtpService.verifyOtp()
   ↓
5. Submit form → AuthApiController.registerBuyer()
   ↓
6. Create User with ROLE_USER
   ↓
7. Redirect to /login
```

### 2. Seller Registration Flow
```
1. Access /seller/register
   ↓
2. Fill store information form:
   - Store name, business type, categories
   - Address, city, district
   - Bank details
   - Operating days, pickup slots
   ↓
3. Upload documents:
   - CCCD/CMND (1-2 files)
   - Business License
   - VSATTP (optional)
   - Logo
   ↓
4. Submit → AuthApiController.registerSeller()
   ↓
5. Create/Update User (ROLE_STORE_OWNER)
   ↓
6. Create/Update Store (status: PENDING)
   ↓
7. Redirect to /seller/onboarding-pending
```

### 3. Store Verification Flow
```
1. Seller submits registration
   ↓
2. Store status = PENDING
   ↓
3. Admin accesses /admin/seller-verify
   ↓
4. Review store documents & info
   ↓
5. Approve → Store.status = ACTIVE
   OR Reject → Store.status = REJECTED + reason
   ↓
6. Email notification (future)
   ↓
7. Seller can start creating deals
```

### 4. Deal Creation & Management Flow
```
1. Seller logs in → /seller/dashboard
   ↓
2. Create new deal:
   - Deal name, code, type
   - Discount type & value
   - Start time, end time
   - Scope (global/store-specific)
   ↓
3. Add products to deal:
   - Product name, description
   - Original price, deal price
   - Stock quantity
   - Expiry date, pickup deadline
   ↓
4. Activate deal → status = ACTIVE
   ↓
5. Deal appears on buyer homepage
```

### 5. Order Placement Flow
```
1. Buyer browses deals → /home
   ↓
2. Select deal → /deals/{id}
   ↓
3. Add to cart → /cart
   ↓
4. Checkout → /checkout
   ↓
5. Select pickup slot
   ↓
6. Apply coupon (optional)
   ↓
7. Confirm order → /order-confirm
   ↓
8. Payment → /payment
   ↓
9. Generate QR code
   ↓
10. Order created → status = PENDING
```

### 6. Order Fulfillment Flow
```
1. Store receives order
   ↓
2. Confirm order → status = CONFIRMED
   ↓
3. Prepare items
   ↓
4. Mark ready → status = READY_FOR_PICKUP
   ↓
5. Buyer arrives & shows QR code
   ↓
6. Store scans QR code
   ↓
7. Verify & handover items → status = COMPLETED
   ↓
8. actualPickupTime recorded
```

---

## 📱 User Interface & Experience

### 1. Buyer Interface
- **Homepage**: Hero section + Featured deals + Categories
- **Deal Detail**: Product info + Store info + Pickup slots
- **Cart**: Add/remove items + Apply coupon
- **Checkout**: Select pickup time + Payment method
- **Profile**: User info + Order history
- **Search/Filter**: By category, location, price range

### 2. Seller Interface
- **Dashboard**: Store overview + Revenue stats
- **Deals Management**: Create/edit/deactivate deals
- **Order Management**: View/confirm/fulfill orders
- **QR Scanner**: Scan pickup QR codes
- **Employee Interface**: Limited access for staff

### 3. Admin/Moderator Interface
- **Dashboard**: Platform statistics + Revenue
- **Seller Verification**: Review/approve/reject stores
- **User Management**: View/activate/deactivate users
- **Order Management**: Monitor all orders
- **Deal Management**: Platform-wide deals
- **Dispute Resolution**: Handle disputes
- **Finance**: Revenue tracking

---

## 🔧 Configuration & Settings

### 1. Application Configuration
```properties
# Server
server.port=8084

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/dealxanh_db
spring.datasource.username=root
spring.datasource.password=*******

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# File Upload
spring.servlet.multipart.max-file-size=10MB

# Email (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=nguyenxuanphananh@gmail.com

# OAuth2 (Google)
spring.security.oauth2.client.registration.google.client-id=*******
spring.security.oauth2.client.registration.google.client-secret=*******
```

### 2. Security Configuration
```java
- Role-based authorization
- Form login with custom handlers
- OAuth2 social login
- Session management (1 session/user)
- Remember Me (7 days)
- CSRF protection (disabled for APIs)
```

---

## 🚀 Development Progress & Features

### ✅ **COMPLETED FEATURES (~40-50% - Focused on Admin)**

#### Authentication & Authorization System ✅
- [x] Multi-role authentication (Buyer, Seller, Admin, Moderator)
- [x] User registration (Buyer & Seller) with OTP verification
- [x] Login with username/email
- [x] OAuth2 Google Login integration
- [x] Password reset flow via email OTP
- [x] Role-based access control (RBAC)
- [x] Session management (1 session per user)
- [x] Remember Me functionality (7 days)
- [x] Custom authentication success/failure handlers
- [x] User profile management with avatar upload

#### Admin Backend - FULLY IMPLEMENTED ✅
- [x] **Admin Dashboard** (`/admin/dashboard`)
  - KPI cards (GMV, Orders, Stores, Buyers)
  - 7-day GMV chart with interactive tabs
  - Real-time statistics with auto-refresh
  - Date picker with presets (Today, Yesterday, 7 days)
  - Recent pending sellers panel
  - Top sellers by rating
  - System alerts

- [x] **Seller Verification System** (`/admin/seller-verify`)
  - Seller list with filters (status, city, search, sort)
  - Status tabs (Pending, Approved, Rejected)
  - Detail panel with store information
  - Document viewer (CCCD, Business License, VSATTP, Logo)
  - Approve/Reject functionality with reason tracking
  - Approval history tracking
  - Real-time search with debounce
  - Pagination with custom URLs

- [x] **User Management** (`/admin/users`)
  - User statistics cards (Total, Buyers, Sellers, Admins, Inactive)
  - Role-based filtering (Buyer, Seller, Admin)
  - Status filtering (Active, Inactive)
  - Real-time search with debounce
  - User detail modal with full information
  - Toggle active/inactive functionality
  - User deletion with confirmation
  - Pagination with filter preservation
  - Display role determination (database role + store relationships)

#### Admin Frontend Architecture ✅
- [x] **Fragment-based Component System**
  - `common/admin/admin-layout.html` - Main layout wrapper
  - `common/admin/sidebar.html` - Dynamic sidebar with active states
  - `common/admin/header.html` - Header with title & optional date picker
  - `common/admin/css.html` - CSS fragment (variables, reset, components)
  - `common/admin/js.html` - JavaScript fragment loader

- [x] **Layout Structure**
  ```
  <div class="layout">
    <aside class="sidebar">           <!-- Fixed sidebar (240px) -->
    <div class="main">                 <!-- Main content (margin-left: 240px) -->
      <header class="topbar">          <!-- Sticky header -->
      <div class="content">            <!-- Page content -->
  ```

- [x] **Dynamic Fragment Parameters**
  - `activeSidebar` - Current page identifier for menu highlighting
  - `pageTitle` - Dynamic page title for header
  - `showDatePicker` - Boolean to show/hide date picker (dashboard only)

- [x] **CSS Architecture**
  - CSS Variables for theming (--primary, --surface, --bg, etc.)
  - Component-based styling (.kpi-card, .btn-primary, .badge-success)
  - Responsive layout (Flexbox + CSS Grid)
  - Transition animations (--tr: 200ms)

- [x] **JavaScript Features**
  - Real-time search with debounce (500ms)
  - Dynamic form submission with filter preservation
  - Modal windows (user detail, document preview)
  - Auto-refresh dashboard data (30 seconds)
  - Pagination with URL parameter preservation
  - CSRF token handling for form submissions

#### Admin Data Management ✅
- [x] **Statistics & Analytics**
  - Real-time GMV tracking with date filtering
  - Order count and revenue calculation
  - Commission tracking (10% platform fee)
  - User registration statistics
  - Store verification metrics
  - 7-day trend data with delta calculations

- [x] **Filtering & Search System**
  - Multi-parameter filtering (role, status, city, search, sort)
  - URL parameter preservation across page navigation
  - Real-time search with server-side redirect
  - Status-based tab filtering
  - City-based geographic filtering
  - Sort ordering (newest, oldest)

- [x] **Pagination System**
  - Custom page size configuration (default: 10-12 items)
  - Pagination with filter preservation
  - Page range calculation with edge case handling
  - Previous/Next navigation with state validation

#### Admin Workflow Management ✅
- [x] **Seller Verification Workflow**
  - Document upload tracking (CCCD, License, VSATTP, Logo)
  - Approval/rejection with reason tracking
  - Reviewer information (approvedBy, rejectedBy)
  - Review timestamp tracking
  - Status transitions (PENDING → ACTIVE/REJECTED)

- [x] **User Management Workflow**
  - Role display logic (DB role + store relationship detection)
  - Active/Inactive status toggle
  - Safe user deletion (checks for existing orders)
  - Self-action prevention (admin cannot delete/disable themselves)

#### Database & Repository Layer ✅
- [x] **Custom Query Methods**
  - `countByRoleName()`, `countByStatus()`
  - `sumRevenueByPeriod()`, `countOrdersByPeriod()`
  - `findByUsernameWithRole()`, `findAllByEmailWithRole()`
  - `findByStoreStoreIdAndStatus()`
  - `findTop5ByOrderByAverageRatingDesc()`

- [x] **Relationship Loading**
  - JOIN FETCH for eager loading
  - Pagination support (`Pageable`, `PageRequest`)
  - Custom queries with `@Query` annotation

### 🚧 **FEATURES IN PROGRESS**

#### Payment Integration
- [ ] MOMO payment gateway
- [ ] ZaloPay payment gateway
- [ ] Bank transfer integration
- [ ] Wallet system

#### Notification System
- [ ] Email notifications (SMTP configured, logic pending)
- [ ] SMS notifications
- [ ] In-app notifications
- [ ] Push notifications

#### Additional Admin Features
- [ ] Deal management interface (database schema ready)
- [ ] Order management interface (database schema ready)
- [ ] Finance management interface (UI ready, backend pending)
- [ ] Dispute resolution system (UI ready, backend pending)
- [ ] Analytics & reporting (basic UI ready)

#### Seller & Buyer Features
- [ ] Seller dashboard (database schema ready)
- [ ] Deal creation interface (database schema ready)
- [ ] Order fulfillment system (database schema ready)
- [ ] Buyer interface (basic templates exist)

### 🚧 Features in Progress

#### Payment Integration
- [ ] MOMO payment gateway
- [ ] ZaloPay payment gateway
- [ ] Bank transfer integration
- [ ] Wallet system

#### Notification System
- [ ] Email notifications
- [ ] SMS notifications
- [ ] In-app notifications
- [ ] Push notifications

#### Review & Rating System
- [ ] Product reviews
- [ ] Store ratings
- [ ] Feedback system

#### Advanced Features
- [ ] Real-time inventory tracking
- [ ] Geolocation-based deal discovery
- [ ] Advanced search & filtering
- [ ] Recommendation engine
- [ ] Loyalty program
- [ ] Referral system

---

## 📊 Code Quality & Best Practices

### 1. Code Style
- **Naming Conventions**: Java standards
- **Comments**: Minimal inline comments
- **Package Organization**: Layer-based
- **Error Handling**: Custom exceptions + Global handler

### 2. Design Patterns
- **MVC Pattern**: Clear separation of concerns
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Request/Response objects
- **Builder Pattern**: Entity construction
- **Singleton Pattern**: Service instances

### 3. Security Best Practices
- **Password Encryption**: BCrypt
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Thymeleaf auto-escaping
- **CSRF Protection**: Enabled (disabled for APIs)
- **Session Management**: Secure configuration

### 4. Performance Optimization
- **Lazy Loading**: JPA relationships
- **Pagination**: Large datasets
- **Caching**: OTP service (in-memory)
- **Connection Pooling**: HikariCP (default)

---

## 🎯 Business Model & Workflows

### 1. Revenue Model
```
Commission: 10% of GMV (Gross Merchandise Value)
Payment Methods: Bank transfer, Cash, MOMO, ZaloPay
```

### 2. User Roles & Permissions
```
ROLE_USER:
- Browse deals
- Place orders
- Manage profile
- Order history

ROLE_STORE_OWNER:
- Create/manage deals
- Fulfill orders
- View store analytics
- Manage staff

ROLE_STORE_STAFF:
- View assigned store orders
- Fulfill orders
- Scan QR codes

ROLE_MODERATOR:
- Verify stores
- Monitor disputes
- Platform analytics

ROLE_ADMIN:
- Full system access
- User management
- System configuration
- Revenue tracking
```

### 3. Order Status Flow
```
PENDING → CONFIRMED → READY_FOR_PICKUP → COMPLETED
                     ↓
                 CANCELLED
```

### 4. Store Status Flow
```
PENDING → ACTIVE (approved)
         → REJECTED (with reason)
         → SUSPENDED (violation)
```

---

## 🔍 Code Duplication & Refactoring Opportunities

### 1. Identified Duplications
- **Controller Logic**: Similar pagination logic across admin controllers
- **Form Validation**: Repeated validation patterns
- **Error Handling**: Similar try-catch blocks
- **Database Queries**: Repeated query patterns

### 2. Refactoring Suggestions
- **Pagination Utility**: Extract common pagination logic
- **Validation Framework**: Implement Bean Validation
- **Service Layer**: Move business logic from controllers
- **Query Optimization**: Create reusable @Query methods

---

## 🚨 Potential Issues & Improvements

### 1. Security Concerns
- **OTP Storage**: In-memory (should use Redis)
- **Password Storage**: Plain text in some places
- **File Upload**: No virus scanning
- **Session Management**: No session fixation protection

### 2. Performance Issues
- **N+1 Queries**: Lazy loading issues
- **File Storage**: Local filesystem (should use cloud)
- **Database**: No connection pool tuning
- **Caching**: No caching layer

### 3. Code Quality
- **Error Messages**: Hard-coded strings
- **Magic Numbers**: Hard-coded values
- **Logging**: Excessive debug logging
- **Testing**: No unit tests

---

## 📈 Deployment & Scalability

### 1. Current Deployment
```
Environment: Development
Database: MySQL (localhost)
File Storage: Local filesystem
Email: Gmail SMTP
```

### 2. Production Recommendations
```
Database: MySQL Cluster / PostgreSQL
File Storage: AWS S3 / CloudFlare R2
Email: SendGrid / AWS SES
Caching: Redis
Load Balancer: Nginx / AWS ALB
Container: Docker + Kubernetes
```

---

## 🎓 Learning & Development Notes

### 1. Strong Points
- **Modern Tech Stack**: Spring Boot 3 + Java 17
- **Clean Architecture**: Well-organized layers
- **Security**: Comprehensive authentication
- **Responsive UI**: Mobile-first design
- **Business Logic**: Complete workflows

### 2. Areas for Improvement
- **Testing**: Add unit & integration tests
- **Documentation**: API documentation
- **Error Handling**: Centralized error handling
- **Logging**: Structured logging
- **Performance**: Query optimization
- **Scalability**: Microservices architecture

---

## 📝 Conclusion

**DealXanh** là một dự án O2O Marketplace hoàn chỉnh và well-structured với:
- ✅ **Backend**: Spring Boot REST API
- ✅ **Frontend**: Thymeleaf + JavaScript
- ✅ **Security**: Spring Security + OAuth2
- ✅ **Database**: MySQL với JPA
- ✅ **Business Logic**: Complete workflows
- ✅ **Admin Panel**: Comprehensive dashboard

Dự án đã đạt được **~70-80%** chức năng chính và đang trong giai đoạn development. Cần bổ sung thêm testing, optimization, và deployment setup để production-ready.

---

## 🧠 **PERSISTENT MEMORY SYSTEM - CLAUDE CONTEXT**

### 📋 **HOW TO USE THIS DOCUMENT AS MEMORY**

#### **🔄 Every Time You Code:**
1. **READ THIS FILE FIRST** → Load into context before starting work
2. **UPDATE AS YOU GO** → Add new discoveries, completed features, changes
3. **REFERENCE OFTEN** → Check before making decisions to stay consistent

#### **🎯 What This Document Remembers:**
- ✅ **Complete Tech Stack** → All libraries, frameworks, versions
- ✅ **Code Architecture** → How you organize files and components  
- ✅ **Your Coding Style** → Fragment-based approach, CSS variables, real-time search
- ✅ **Business Logic** → All workflows, validations, database relationships
- ✅ **Project Status** → What's done (40-50% admin), what's pending
- ✅ **Your Choices** → Why you chose certain approaches over alternatives
- ✅ **File Locations** → Where everything lives in the project

### 🚨 **CRITICAL RULES FOR CODING**

#### **1. ALWAYS Follow Your Fragment Pattern**
```html
<!-- ✅ DO THIS - Your established pattern -->
<div th:replace="~{common/admin/admin-layout :: layout(
    activeSidebar='new-page',
    pageTitle='New Page Title',
    showDatePicker=false
)}">
    <!-- Page content -->
</div>

<!-- ❌ NEVER DO THIS - Breaking your pattern -->
<!DOCTYPE html>
<html>
<head>...</head>
<body>
    <!-- Duplicated sidebar, header, etc. -->
</body>
</html>
```

#### **2. ALWAYS Use Your CSS Variables**
```css
/* ✅ DO THIS - Your theming system */
background: var(--primary);
color: var(--text-secondary);
border-radius: var(--radius-md);

/* ❌ NEVER DO THIS - Breaking consistency */
background: #0d5c2e;
color: #666;
border-radius: 10px;
```

#### **3. ALWAYS Preserve Filters in Navigation**
```javascript
// ✅ DO THIS - Your filter preservation pattern
const url = new URL(window.location);
url.searchParams.set('search', searchValue);
url.searchParams.set('page', '0'); // Reset to first page
window.location.href = url.toString();

// ❌ NEVER DO THIS - Losing user context
window.location.href = '/admin/users';
```

#### **4. ALWAYS Use Your Real-Time Search Pattern**
```javascript
// ✅ DO THIS - Your debounce + server redirect pattern
let searchTimeout;
searchInput.addEventListener('input', (e) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        // Redirect to server with search query
        window.location.href = buildUrl(0, e.target.value.trim());
    }, 500);
});

// ❌ NEVER DO THIS - Complex client-side filtering without server sync
// Complex JavaScript filtering that gets out of sync with database
```

### 🎯 **YOUR SIGNATURE PATTERNS - STICK TO THESE**

#### **Pattern 1: Fragment Composition**
```html
<!-- File: templates/admin/new-page.html -->
<div th:replace="~{common/admin/admin-layout :: layout(
    activeSidebar='new-page',
    pageTitle='Page Title',
    showDatePicker=false
)}">
    <div class="content">
        <!-- Your page-specific content here -->
    </div>
</div>
```

#### **Pattern 2: CSS Styling**
```css
/* Define in page <style> tag */
.stats-cards {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
    margin-bottom: 24px;
}

.stat-card {
    background: var(--surface);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);
    padding: 20px;
    box-shadow: var(--shadow-sm);
}
```

#### **Pattern 3: Filter Preservation**
```java
// In Controller
@GetMapping("/admin/seller-verify")
public String sellerVerify(
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String search,
    @RequestParam(required = false) String city,
    @RequestParam(required = false) String sort,
    @RequestParam(defaultValue = "0") int page,
    Model model) {
    
    // Filter logic...
    
    // Preserve filters for view
    model.addAttribute("currentStatus", status);
    model.addAttribute("searchQuery", search);
    model.addAttribute("selectedCity", city);
    model.addAttribute("selectedSort", sort);
    model.addAttribute("currentPage", page);
    
    return "admin/seller-verify";
}
```

#### **Pattern 4: Real-Time Search**
```javascript
// In page JavaScript
function buildUrl(page, searchValue) {
    const urlParams = new URLSearchParams(window.location.search);
    let params = [];
    
    const status = urlParams.get('status') || currentFilters.status;
    const search = searchValue || currentFilters.search;
    const city = urlParams.get('city') || currentFilters.city;
    
    if (status) params.push('status=' + encodeURIComponent(status));
    if (search) params.push('search=' + encodeURIComponent(search));
    if (city) params.push('city=' + encodeURIComponent(city));
    params.push('page=' + page);
    
    return '/admin/seller-verify?' + params.join('&');
}
```

### 📁 **YOUR FILE STRUCTURE - RESPECT THIS**

```
src/main/java/com/dealxanh/app/
├── controller/
│   ├── admin/
│   │   ├── AdminController.java          # Main admin logic
│   │   ├── AdminDashboardApiController.java  # Dashboard API
│   │   └── ModeratorController.java      # Moderator-specific
│   ├── auth/
│   │   └── AuthController.java           # Login/logout pages
│   └── api/
│       └── AuthApiController.java        # Registration API
├── service/
│   ├── EmailService.java                 # OTP emails
│   └── OtpService.java                   # OTP generation
├── repository/
│   ├── UserRepository.java
│   ├── StoreRepository.java
│   └── OrderRepository.java
├── entity/
│   ├── User.java
│   ├── Store.java
│   └── Order.java
├── security/
│   ├── CustomUserDetailsService.java     # User loading
│   └── SecurityConfig.java               # Security rules
└── config/
    └── WebMvcConfig.java

src/main/resources/
├── templates/
│   ├── common/admin/
│   │   ├── admin-layout.html            # ✅ USE THIS FOR NEW PAGES
│   │   ├── sidebar.html                 # ✅ REUSABLE COMPONENT
│   │   ├── header.html                  # ✅ REUSABLE COMPONENT
│   │   ├── css.html                     # ✅ INCLUDE IN ALL PAGES
│   │   └── js.html                      # ✅ INCLUDE IN ALL PAGES
│   ├── admin/
│   │   ├── dashboard.html               # ✅ COMPLETED
│   │   ├── users.html                   # ✅ COMPLETED
│   │   ├── seller-verify.html           # ✅ COMPLETED
│   │   ├── deals.html                   # 🚧 IN PROGRESS
│   │   ├── orders.html                  # 🚧 IN PROGRESS
│   │   └── finance.html                 # 🚧 PLANNED
│   ├── auth/
│   │   ├── register-buyer.html
│   │   └── register-seller.html
│   └── buyer/
│       └── home.html
└── static/
    ├── css/
    │   ├── variables.css               # ✅ DESIGN TOKENS
    │   ├── reset.css                   # ✅ CSS RESET
    │   └── components-layout.css      # ✅ COMPONENT STYLES
    └── js/
        ├── api.js                      # ✅ API CLIENT
        ├── component-loader.js         # ✅ COMPONENT LOADER
        └── pages/
            └── home.js
```

### 🎨 **YOUR DESIGN SYSTEM - USE CONSISTENTLY**

#### **Color Palette (CSS Variables)**
```css
--primary: #0d5c2e;         /* Main brand color - Use for primary actions */
--primary-dark: #08421f;    /* Darker shade - Hover states */
--primary-light: #1a7c40;   /* Lighter shade - Backgrounds */

--accent: #d4af37;         /* Gold accent - Special highlights */

--surface: #fff;           /* White - Card backgrounds */
--bg: #f0f2f5;             /* Light gray - Page background */

--text-primary: #1a1a1a;   /* Dark - Main text */
--text-secondary: #666;     /* Medium - Secondary text */
--text-tertiary: #999;      /* Light - Helper text */

--danger: #ef4444;         /* Red - Delete, reject, errors */
--warning: #f59e0b;        /* Orange - Pending, alerts */
--success: #10b981;        /* Green - Active, approved, success */
--info: #3b82f6;           /* Blue - Information, user role */
```

#### **Component Styles (Copy These for New Pages)**
```css
/* Button Styles */
.btn-sm {
    padding: 6px 12px;
    border-radius: var(--radius-md);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all var(--tr);
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

.btn-primary {
    background: var(--primary);
    color: #fff;
    border: none;
}

.btn-primary:hover {
    background: var(--primary-dark);
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(13,92,46,.3);
}

/* Badge Styles */
.badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: var(--radius-full);
    font-size: 11px;
    font-weight: 700;
}

.badge-success {
    background: var(--success-bg);
    color: var(--success);
}

.badge-warning {
    background: var(--warning-bg);
    color: var(--warning);
}

.badge-danger {
    background: var(--danger-bg);
    color: var(--danger);
}

.badge-info {
    background: var(--info-bg);
    color: var(--info);
}

/* Table Styles */
.table {
    width: 100%;
    border-collapse: collapse;
}

.table th {
    text-align: left;
    padding: 14px 20px;
    font-size: 11px;
    font-weight: 700;
    color: var(--text-secondary);
    text-transform: uppercase;
    letter-spacing: .8px;
    border-bottom: 2px solid var(--border-light);
    background: var(--bg);
}

.table td {
    padding: 18px 20px;
    font-size: 14px;
    border-bottom: 1px solid var(--border-light);
    vertical-align: middle;
}

.table tr:hover td {
    background: rgba(0,0,0,.02);
}
```

### 🎯 **YOUR BRANCH STATUS**

**Current Branch**: `PhanAnh`
**Main Branch**: `main`
**Status**: Active development on admin features

### 📊 **PROJECT COMPLETION STATUS**

```
███████████████████████░░░░ 50% COMPLETE

✅ ADMIN BACKEND (100%)
  ├─ Authentication & Authorization ✅
  ├─ User Management ✅
  ├─ Seller Verification ✅
  ├─ Dashboard Statistics ✅
  └─ Filter & Search System ✅

✅ ADMIN FRONTEND (100%)
  ├─ Fragment Architecture ✅
  ├─ Dashboard Page ✅
  ├─ Users Page ✅
  ├─ Seller Verify Page ✅
  └─ Real-time Search ✅

🚧 SELLER FEATURES (20%)
  ├─ Database Schema ✅
  ├─ Registration Flow ✅
  ├─ Dashboard Interface 🚧
  └─ Deal Management 🚧

🚧 BUYER FEATURES (10%)
  ├─ Database Schema ✅
  ├─ Registration Flow ✅
  ├─ Home Page ✅
  └─ Order Flow 🚧

❌ PAYMENT INTEGRATION (0%)
  ├─ MOMO Gateway ❌
  ├─ ZaloPay Gateway ❌
  └─ Bank Transfer ❌
```

### 🔧 **QUICK REFERENCE FOR COMMON TASKS**

#### **Creating a New Admin Page**
1. Create HTML file in `templates/admin/`
2. Use fragment pattern: `<div th:replace="~{common/admin/admin-layout :: layout(...)}">`
3. Copy CSS styles from existing pages
4. Add controller method in `AdminController.java`
5. Update `sidebar.html` with new menu item
6. Test filter preservation and pagination

#### **Adding Real-Time Search**
1. Add search input in HTML: `<input type="text" class="search-input">`
2. Add debounce JavaScript pattern (500ms)
3. Update controller to accept `@RequestParam String search`
4. Implement server-side filtering in repository
5. Preserve search parameter in pagination URLs

#### **Adding Filter Tabs**
1. Add tab buttons in HTML: `<a class="filter-btn" href="?status=active">`
2. Update controller to accept `@RequestParam String status`
3. Implement filtering logic in service/repository
4. Add active class to current tab: `th:classappend="${currentStatus == 'active' ? 'active' : ''}"`

---

## 🔗 Quick Links

### **Key Files to Reference**
- **Layout System**: `templates/common/admin/admin-layout.html`
- **Sidebar Component**: `templates/common/admin/sidebar.html`  
- **Header Component**: `templates/common/admin/header.html`
- **Complete Examples**: `templates/admin/dashboard.html`, `templates/admin/users.html`
- **Controller Logic**: `src/main/java/com/dealxanh/app/controller/admin/AdminController.java`
- **Security Config**: `src/main/java/com/dealxanh/app/config/SecurityConfig.java`
- **Database Entities**: `src/main/java/com/dealxanh/app/entity/`

### **What to Check Before Coding**
1. ✅ **Read this file** → Load all context and patterns
2. ✅ **Check existing pages** → See how similar features were implemented
3. ✅ **Follow fragment pattern** → Use `admin-layout.html` for new pages
4. ✅ **Use CSS variables** → Maintain consistent theming
5. ✅ **Preserve filters** → Keep user context across navigation
6. ✅ **Test real-time search** → Ensure debounce + server redirect works
7. ✅ **Update this file** → Add new patterns and discoveries

---

*📅 Last Updated: 2025-12-19*
*🔄 Version: 2.0 - Complete Admin System Documentation*
*📍 Branch: PhanAnh*
*👤 Developer: Context-Cached for Persistent Memory*
