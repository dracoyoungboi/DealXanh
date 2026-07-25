# 📘 DEALXANH PROJECT GUIDELINES
**Project Type:** Logistics SaaS Platform (Subscription-based)
**Architecture:** Pure Spring MVC Monolithic (Server-Side Rendering)

---

## 1. TECH STACK

### Backend Technologies
*   **Core Framework:** Spring Boot 3.2.3
*   **Language:** Java 17
*   **Build Tool:** Maven
*   **Database:** MySQL (MySQL Connector J)
*   **ORM:** JPA/Hibernate

### Security & Authentication
*   **Authentication:** Spring Security (Session/Cookie-based cho Web MVC)
*   **OAuth2:** Google Login integration
*   **Password Encoder:** BCrypt

### Frontend Technologies
*   **Template Engine:** Thymeleaf
*   **CSS:** Custom CSS with CSS Variables
*   **JavaScript:** Vanilla JavaScript (ES6+)
*   **Architecture:** Component-based (Thymeleaf fragments) & Mobile-First cho app Shipper

### Additional Libraries
*   **Email:** Spring Boot Starter Mail (Gmail SMTP)
*   **Validation:** Spring Boot Starter Validation
*   **Development:** Spring Boot DevTools (hot reload)
*   **Utilities:** Lombok (boilerplate reduction)
*   **Real-time:** Server-Sent Events (SSE) của Spring WebMVC

---

## 2. DIRECTORY STRUCTURE
Cấu trúc thư mục chuẩn Spring MVC, phân tách theo luồng giao diện thay vì chia API/Web. Sử dụng `application.properties` để cấu hình.

```text
src/
├── main/
│   ├── java/com/shiphola/        
│   │   ├── controller/               # Spring MVC Controllers (@Controller)
│   │   │   ├── AdminController.java  # Quản lý Sàn, cấu hình Subscription
│   │   │   ├── DispatcherController.java # Giao diện lên đơn
│   │   │   ├── ShipperController.java    # Giao diện nhận đơn (Mobile-view)
│   │   │   └── AuthController.java   # Đăng nhập, đăng ký
│   │   │
│   │   ├── service/                  # Business logic (Interfaces)
│   │   │   └── impl/                 # Service Implementations
│   │   ├── repository/               # Spring Data JPA Repositories
│   │   ├── entity/                   # Domain models (User, Order, Package)
│   │   ├── security/                 # Cấu hình Spring Security
│   │   ├── config/                   # WebMvcConfig, InterceptorConfig
│   │   ├── dto/                      # Data Transfer Objects (Form Submit)
│   │   ├── exception/                # @ControllerAdvice xử lý lỗi tập trung
│   │   ├── scheduler/                # Cron Jobs (VD: Auto-expired package)
│   │   ├── interceptor/              # Chặn/Kiểm tra quyền truy cập (Subscription check)
│   │   ├── constant/                 # Enums (OrderStatus, Role, etc.)
│   │   └── util/                     # Utility classes
│   │
│   └── resources/
│       ├── application.properties    # Cấu hình hệ thống duy nhất
│       │
│       ├── templates/                # 100% UI Render từ Server
│       │   ├── admin/                # Views cho Admin
│       │   ├── dispatcher/           # Views cho Tổng đài viên
│       │   ├── shipper/              # Views Mobile-first cho Tài xế
│       │   ├── auth/                 # Views cho Đăng nhập/Đăng ký
│       │   ├── common/               # Views dùng chung (head, navbar, sidebar, alerts)
│       │   └── error/                # Views báo lỗi (403, 404, 500)
│       │
│       └── static/                   
│           ├── css/                  
│           │   ├── variables.css     
│           │   ├── reset.css         
│           │   ├── layout-desktop.css 
│           │   ├── layout-mobile.css  
│           │   └── components.css     
│           │
│           ├── js/                   
│           │   ├── sse-client.js      # Script nhận Event real-time
│           │   ├── form-validation.js # Vanilla JS validate form
│           │   └── ui-interactions.js # UI events
│           │
│           └── assets/               # Hình ảnh, Logo, Icons
│
└── test/                             # Unit & Integration Tests

3. GLOBAL RULES (QUY CHUẨN MÃ NGUỒN)
3.1. Quy tắc Cấu hình Hệ thống
Chỉ sử dụng file .properties: Dùng application.properties làm file cấu hình chính. Nếu cần phân chia môi trường, tạo thêm application-dev.properties và application-prod.properties, file gốc sẽ dùng để kích hoạt profile tương ứng (vd: spring.profiles.active=dev).

3.2. Quy tắc Phân tách Logic theo Role (Vai trò)
Mọi xử lý nghiệp vụ và điều hướng bắt buộc nằm trong file Controller và Service của role tương ứng, tuyệt đối không Cross-talk (Controller này gọi Controller kia).

Role Admin: Viết tại AdminController ➔ Gọi AdminService ➔ Render HTML tại templates/admin/.

Role Dispatcher: Viết tại DispatcherController ➔ Gọi DispatcherService ➔ Render HTML tại templates/dispatcher/.

Role Shipper: Viết tại ShipperController ➔ Gọi ShipperService ➔ Render HTML tại templates/shipper/.

Role Guest (Public): Viết tại AuthController ➔ Gọi AuthService ➔ Render HTML tại templates/auth/.

3.3. Quy tắc Tầng Database (Repository)
Phân chia theo Entity, KHÔNG theo Role: Repository giao tiếp trực tiếp với bảng trong CSDL.

Chỉ tồn tại các Repository đại diện cho Model (vd: UserRepository, OrderRepository, PackageRepository).

Ví dụ: Cả AdminService và ShipperService đều có thể gọi chung vào OrderRepository để thao tác với dữ liệu đơn hàng.

3.4. Cấm (Red Flags)
Cấm viết Logic trong Controller: Controller CHỈ làm nhiệm vụ: (1) Nhận DTO từ Form ➔ (2) Gọi phương thức của Service ➔ (3) Trả về Model và tên file View (String). Các lệnh if/else nghiệp vụ phải nằm ở Service.

Cấm nhúng JS/CSS Role-specific vào file Global: Không import file shipper.js hoặc admin.css vào file common/head.html. Màn hình của role nào thì import riêng tài nguyên tĩnh của role đó ở cuối file HTML để tối ưu hiệu suất và tránh xung đột Event.

Không bỏ qua tầng DTO: Dữ liệu từ form web đẩy lên Controller phải được hứng bằng các class DTO (có @Valid), tuyệt đối không dùng trực tiếp Entity (vd: Order, User) để hứng dữ liệu submit từ client.

---

## 4. DTO STRUCTURE (CẤU TRÚC DTO)

### 4.1. Phân chia DTO
```
dto/
├── common/               # DTO dùng chung cho cả request và response
│   ├── BaseResponse<T>        # Wrapper chuẩn cho REST API response
│   ├── PageResponse<T>        # Wrapper cho danh sách phân trang
│   ├── PaginationDTO          # Tham số phân trang (page, size, sort)
│   ├── PackageFilterDTO       # Filter đơn hàng
│   └── UserFilterDTO          # Filter người dùng
│
├── request/              # DTO nhận dữ liệu từ client → server
│   ├── RegisterDTO             # Đăng ký
│   ├── LoginDTO                # Đăng nhập
│   ├── CreatePackageDTO        # Tạo đơn hàng
│   ├── AssignPackageDTO        # Giao đơn cho shipper
│   ├── UpdatePackageStatusDTO  # Cập nhật trạng thái đơn
│   ├── SubscriptionDTO         # Tạo/cập nhật subscription
│   └── UserDTO                 # Tạo/cập nhật user
│
└── response/             # DTO trả dữ liệu từ server → client
    ├── UserResponse            # Thông tin user (không có password)
    └── DashboardStats          # Thống kê dashboard
```

### 4.2. Quy tắc DTO
- **Request DTO**: Có validation (@NotNull, @NotBlank, @Email...), dùng @Valid trong Controller
- **Response DTO**: Không chứa field nhạy cảm (password, token), format dữ liệu trước khi trả
- **Common DTO**: Chia sẻ cho cả request và response (pagination, filter, base response)

---

## 5. PROJECT HISTORY (LỊCH SỬ DỰ ÁN)

### Ngày 2026-05-12 - Khởi tạo dự án ShipHola
- **Framework**: Spring Boot 3.2.3 + Java 17 + MySQL
- **Architecture**: Pure Spring MVC Monolithic (Server-Side Rendering với Thymeleaf)
- **Security**: Spring Security (Session/Cookie-based) + OAuth2 Google Login
- **Đã hoàn thành**:
  - Backend: 35 files (Entities, Repositories, Services, Controllers, Security, Config)
  - Frontend: 15 templates (Admin, Dispatcher, Shipper, Auth, Common, Error)
  - Static: 19 files (CSS variables/reset/layout/components, JS SSE/validation/UI)
  - Total: 70 files
- **Cấu trúc DTO**: 14 files (Common: 5, Request: 7, Response: 2)

### Ngày 2026-05-12 - Tích hợp Landing Page Public
- **Mục tiêu**: Tích hợp frontend landing page vào Spring Boot, cho phép truy cập mà không cần đăng nhập
- **Các vấn đề đã fix**:
  1. **Thymeleaf Template Parsing Exception**: Sửa namespace declaration từ `https://` thành `http://` cho cả `th:` và `sec:`
  2. **Spring Security Extras không parse được**: Thay thế `sec:authorize` bằng `th:if` với standard Thymeleaf conditionals
  3. **HTML Structure Issues**: Loại bỏ hoàn toàn `<video>` elements (boolean attributes gây parse error)
  4. **Application Properties Typo (CRITICAL)**: Sửa `spring.thymymeleaf.prefix` thành `spring.thymeleaf.prefix`
- **File được tạo/sửa**:
  - `src/main/resources/templates/public/landing.html` - Viết lại hoàn toàn, đơn giản hóa cấu trúc
  - `src/main/resources/templates/public/search-result.html` - Sửa namespace
  - `src/main/resources/templates/public/navbar.html` - Fragment navbar dùng chung
  - `src/main/resources/templates/public/footer.html` - Fragment footer dùng chung
  - `src/main/resources/static/css/public-landing.css` - CSS cho landing page
  - `src/main/resources/static/js/public-landing.js` - JavaScript cho landing page
  - `src/main/java/com/shiphola/controller/PublicController.java` - Controller cho public pages
  - `src/main/java/com/shiphola/service/PublicService.java` - Service cho public logic
  - `src/main/java/com/shiphola/dto/PublicOrderDTO.java` - DTO cho form đặt hàng
- **Security Config**: Cập nhật `SecurityConfig.java` để allow public access: `.requestMatchers("/", "/home", "/public/**", ...).permitAll()`
- **Lưu ý quan trọng**: Landing page giữ nguyên HTML trong một file, chỉ tách CSS và JS ra file riêng theo yêu cầu

### Ngày 2026-05-12 - Nâng cao Landing Page Public
- **Mục tiêu**: Thiết kế và nâng cao giao diện landing page với các tính năng tương tác
- **Các tính năng đã thêm**:
  1. **Hero Section Background**: Thêm hình ảnh nền liên quan đến dịch vụ ship với overlay để đảm bảo độ đọc được của văn bản
  2. **Contact CTA Section**: Thiết kế lại phần liên hệ tập trung vào đặt hàng (không chỉ đăng ký shipper)
     - Main CTA: Đặt hàng qua Hotline/Zalo/Messenger
     - Secondary CTA: Đăng ký làm Shipper (thiết kế mini card)
  3. **Footer Contact Icons**: Thêm SVG icons cho Hotline, Zalo, Messenger
  4. **Stats Counter Animation**: Hiệu ứng đếm số từ 0 đến giá trị cuối khi scroll vào vùng nhìn thấy
     - Sử dụng IntersectionObserver API để trigger animation
     - Easing function easeOutQuart cho animation mượt mà
     - Format số theo phong cách Việt Nam (toLocaleString 'vi-VN')
     - Animation chạy trong 2 giây với 60 FPS
- **Sửa lỗi**:
  - Thay chữ Trung Quốc "工作时间灵活" thành "thời gian làm việc linh hoạt"
  - Loại bỏ màu xanh, giữ đúng màu chủ đạo (vàng, đen, amber)
- **File được sửa**:
  - `src/main/resources/templates/public/landing.html` - Thêm data attributes cho stats, SVG icons
  - `src/main/resources/static/css/public-landing.css` - Hero background, Contact CTA styling
  - `src/main/resources/static/js/public-landing.js` - Thêm `initStatsCounter()` function

### Ngày 2026-05-12 - Thiết kế lại flow đặt đơn (2-step + Map API)
- **Mục tiêu**: Redesign form đặt đơn thành 2 bước với DDOS protection, geolocation, và OpenStreetMap integration
- **Yêu cầu**:
  - Bước 1: Tính giá cước (AJAX) → Hiển thị phí, không lưu database
  - Bước 2: Xác nhận đặt đơn → DDOS protection → Lưu database → Trả tracking number
  - Không tạo account (khách hàng gọi tổng đài xác nhận)
  - Auto-fill vị trí hiện tại cho điểm lấy hàng (browser geolocation + server reverse geocoding)
  - Parse địa chỉ giao hàng + ước tính khoảng cách (OSRM API)
  - Hiển thị khoảng cách = trung bình(shortest + longest routes)
  - Rate limit: 1 request/5 phút/IP
  - Sau khi đặt: Hiển thị hotline + "Chúng tôi sẽ gọi trong 5 phút"
- **Tech Stack**:
  - OpenStreetMap Nominatim API (geocoding)
  - OSRM API (distance matrix with alternatives)
  - Rate limiting (server-side, in-memory ConcurrentHashMap hoặc Redis)
  - AJAX calls với @ResponseBody
- **Architecture**:
  ```
  PublicController (@Controller + @ResponseBody cho AJAX endpoints)
      ↓
  LandingPageService + MapService (interfaces trong service/, impl trong service/impl/)
      ↓
  PackageRepository (đã có)
  ```
- **Files cần tạo/sửa**:
  - `service/MapService.java` - Interface cho map operations
  - `service/impl/MapServiceImpl.java` - Nominatim/OSRM integration
  - `util/RateLimitUtil.java` - Rate limiting logic
  - `dto/request/CalculateFeeDTO.java` - Request DTO cho fee calculation
  - `dto/request/ConfirmOrderDTO.java` - Request DTO cho order confirmation
  - `dto/response/FeeCalculationResponse.java` - Response DTO với fee, distance, duration
  - `dto/response/OrderConfirmationResponse.java` - Response DTO với tracking number
  - `controller/PublicController.java` - Add AJAX endpoints: GET /public/calculate-fee, POST /public/confirm-order
  - `service/landing/LandingPageService.java` - Add confirmOrder method
  - `service/impl/LandingPageServiceImpl.java` - Implement confirmOrder with MapService
  - `templates/public/landing.html` - Redesign form với 2-step UI
  - `static/js/public-landing.js` - AJAX calls, geolocation logic
  - `application.properties` - Add OSM API endpoints config
- **2-Step Flow**:
  ```
  STEP 1: User điền form → Click "Tính giá cước"
          → AJAX GET /public/calculate-fee
          → MapService.calculateDistance() gọi OSRM API
          → OSRM trả về routes[0].distance (shortest) + routes[1].distance (alternative)
          → Calculate average: (shortest + alternative) / 2, round to 0.5km
          → Calculate fee based on distance
          → Return {fee, distance, duration}
          → Hiển thị phí + nút "Xác nhận đặt đơn"

  STEP 2: Click "Xác nhận đặt đơn"
          → RateLimitUtil.checkLimit(ip) → reject nếu < 5 phút
          → AJAX POST /public/confirm-order
          → LandingPageService.confirmOrder() lưu vào DB
          → Return tracking number
          → Show modal: "Mã tracking: SHxxx, Hotline: 0909 xxx xxx, Gọi trong 5 phút"
  ```
- **Map API Details**:
  - Nominatim Reverse Geocoding: `GET https://nominatim.openstreetmap.org/reverse?lat={lat}&lon={lon}&format=json`
  - Nominatim Forward Geocoding: `GET https://nominatim.openstreetmap.org/search?q={address}&format=json`
  - OSRM Route: `GET http://router.project-osrm.org/route/v1/driving/{lon1},{lat1};{lon2},{lat2}?alternatives=true`
- **DDOS Protection Layers**:
  1. Server-side rate limit (5 phút/IP) - RateLimitUtil với ConcurrentHashMap<IP, timestamp>
  2. Client-side disable button sau submit
  3. Optional: Google reCAPTCHA v3 (invisible)
  4. Optional: Honeypot hidden field