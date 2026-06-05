-- ============================================
-- DEALXANH - MASTER DATABASE FILE
-- Schema + Base Data + Comprehensive Test Data
-- ============================================
-- Chạy trên MySQL 8.0+ database rỗng:
--   mysql -u root -p < dealxanh_master.sql
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

-- ============================================
-- PHASE 1: SCHEMA - DROP & CREATE ALL TABLES
-- ============================================

-- Drop theo thứ tự FK để tránh lỗi
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS disputes;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS deal_products;
DROP TABLE IF EXISTS deal_categories;
DROP TABLE IF EXISTS deals;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS stores;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- ============================================
-- 1. roles
-- ============================================
CREATE TABLE roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) DEFAULT NULL,
    name VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 2. users
-- ============================================
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    active BIT(1) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    avatar_url VARCHAR(255) DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    email VARCHAR(255) DEFAULT NULL,
    full_name VARCHAR(255) DEFAULT NULL,
    password VARCHAR(255) DEFAULT NULL,
    phone VARCHAR(255) DEFAULT NULL,
    provider VARCHAR(255) DEFAULT NULL,
    provider_id VARCHAR(255) DEFAULT NULL,
    reset_token VARCHAR(255) DEFAULT NULL,
    reset_token_expiry DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    username VARCHAR(255) DEFAULT NULL,
    role_id BIGINT DEFAULT NULL,
    work_store_id BIGINT DEFAULT NULL,
    weak_password TINYINT(1) DEFAULT 0,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 3. stores
-- ============================================
CREATE TABLE stores (
    store_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(255) DEFAULT NULL,
    average_rating DOUBLE DEFAULT NULL,
    close_time TIME(6) DEFAULT NULL,
    cover_image_url VARCHAR(255) DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    latitude DOUBLE DEFAULT NULL,
    logo_url VARCHAR(255) DEFAULT NULL,
    longitude DOUBLE DEFAULT NULL,
    open_time TIME(6) DEFAULT NULL,
    phone VARCHAR(255) DEFAULT NULL,
    status VARCHAR(255) DEFAULT NULL,
    store_name VARCHAR(255) DEFAULT NULL,
    total_reviews INT DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    owner_id BIGINT DEFAULT NULL,
    approval_mode VARCHAR(255) DEFAULT NULL,
    bank_account_number VARCHAR(255) DEFAULT NULL,
    bank_account_owner VARCHAR(255) DEFAULT NULL,
    bank_name VARCHAR(255) DEFAULT NULL,
    business_license_url VARCHAR(255) DEFAULT NULL,
    business_type VARCHAR(255) DEFAULT NULL,
    categories VARCHAR(255) DEFAULT NULL,
    cccd_url VARCHAR(1000) DEFAULT NULL,
    city VARCHAR(255) DEFAULT NULL,
    district VARCHAR(255) DEFAULT NULL,
    max_slots_per_time INT DEFAULT NULL,
    operating_days VARCHAR(255) DEFAULT NULL,
    pickup_duration_minutes INT DEFAULT NULL,
    pickup_slots VARCHAR(255) DEFAULT NULL,
    vsattp_url VARCHAR(255) DEFAULT NULL,
    rejection_reason VARCHAR(500) DEFAULT NULL,
    reviewed_at DATETIME(6) DEFAULT NULL,
    approved_by BIGINT DEFAULT NULL,
    rejected_by BIGINT DEFAULT NULL,
    partner_tier VARCHAR(20) DEFAULT 'BRONZE',
    FOREIGN KEY (owner_id) REFERENCES users(user_id),
    FOREIGN KEY (approved_by) REFERENCES users(user_id),
    FOREIGN KEY (rejected_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 4. categories
-- ============================================
CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) DEFAULT NULL,
    icon_url VARCHAR(255) DEFAULT NULL,
    name VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 5. products
-- ============================================
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    active BIT(1) DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    deal_end_time DATETIME(6) DEFAULT NULL,
    deal_price DOUBLE DEFAULT NULL,
    deal_start_time DATETIME(6) DEFAULT NULL,
    deleted BIT(1) DEFAULT NULL,
    description TEXT,
    expiry_date DATETIME(6) DEFAULT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    name VARCHAR(255) DEFAULT NULL,
    original_price DOUBLE DEFAULT NULL,
    pickup_deadline DATETIME(6) DEFAULT NULL,
    product_type VARCHAR(255) DEFAULT NULL,
    stock_quantity INT DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    category_id BIGINT DEFAULT NULL,
    store_id BIGINT DEFAULT NULL,
    approval_status VARCHAR(255) DEFAULT NULL,
    rejection_reason TEXT,
    current_price DOUBLE DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (store_id) REFERENCES stores(store_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 6. deals
-- ============================================
CREATE TABLE deals (
    deal_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    banner_url VARCHAR(255) DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    deal_code VARCHAR(255) DEFAULT NULL,
    deal_name VARCHAR(255) DEFAULT NULL,
    deal_type VARCHAR(255) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    discount_type VARCHAR(255) DEFAULT NULL,
    discount_value DOUBLE DEFAULT NULL,
    end_time DATETIME(6) DEFAULT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    max_discount_amount DOUBLE DEFAULT NULL,
    max_usage_count BIGINT DEFAULT NULL,
    min_order_amount DOUBLE DEFAULT NULL,
    priority INT DEFAULT NULL,
    scope VARCHAR(255) DEFAULT NULL,
    start_time DATETIME(6) DEFAULT NULL,
    status VARCHAR(255) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    usage_count BIGINT DEFAULT NULL,
    usage_per_user BIGINT DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    store_id BIGINT DEFAULT NULL,
    apply_method VARCHAR(255) DEFAULT NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    FOREIGN KEY (store_id) REFERENCES stores(store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 7. deal_categories
-- ============================================
CREATE TABLE deal_categories (
    deal_category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    deal_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    priority INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (deal_id) REFERENCES deals(deal_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE,
    INDEX idx_dc_deal (deal_id),
    INDEX idx_dc_category (category_id),
    INDEX idx_dc_deal_cat (deal_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 8. deal_products
-- ============================================
CREATE TABLE deal_products (
    deal_product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) DEFAULT NULL,
    max_quantity INT DEFAULT NULL,
    original_price DOUBLE DEFAULT NULL,
    priority INT DEFAULT NULL,
    sale_price DOUBLE DEFAULT NULL,
    sold_quantity INT DEFAULT NULL,
    deal_id BIGINT DEFAULT NULL,
    product_id BIGINT DEFAULT NULL,
    FOREIGN KEY (deal_id) REFERENCES deals(deal_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 9. orders
-- ============================================
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actual_pickup_time DATETIME(6) DEFAULT NULL,
    cancellation_reason VARCHAR(255) DEFAULT NULL,
    coupon_code VARCHAR(255) DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    discount_amount DOUBLE DEFAULT NULL,
    final_amount DOUBLE DEFAULT NULL,
    note VARCHAR(255) DEFAULT NULL,
    payment_method VARCHAR(255) DEFAULT NULL,
    payment_status VARCHAR(255) DEFAULT NULL,
    pickup_qr_code VARCHAR(255) DEFAULT NULL,
    scheduled_pickup_time DATETIME(6) DEFAULT NULL,
    status VARCHAR(255) DEFAULT NULL,
    total_amount DOUBLE DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    store_id BIGINT DEFAULT NULL,
    user_id BIGINT DEFAULT NULL,
    FOREIGN KEY (store_id) REFERENCES stores(store_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 10. order_items
-- ============================================
CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT DEFAULT NULL,
    unit_price DOUBLE DEFAULT NULL,
    order_id BIGINT DEFAULT NULL,
    product_id BIGINT DEFAULT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 11. transactions
-- ============================================
CREATE TABLE transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DOUBLE DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    net_amount DOUBLE DEFAULT NULL,
    platform_fee DOUBLE DEFAULT NULL,
    status VARCHAR(255) DEFAULT NULL,
    type VARCHAR(255) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    order_id BIGINT DEFAULT NULL,
    store_id BIGINT NOT NULL,
    transaction_ref VARCHAR(255) DEFAULT NULL,
    payment_method VARCHAR(50) DEFAULT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (store_id) REFERENCES stores(store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 12. carts
-- ============================================
CREATE TABLE carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 13. cart_items
-- ============================================
CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT DEFAULT 1,
    unit_price DOUBLE DEFAULT 0,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    UNIQUE KEY uq_cart_product (cart_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 14. disputes
-- ============================================
CREATE TABLE disputes (
    dispute_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_note VARCHAR(255) DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    description TEXT,
    evidence_url VARCHAR(255) DEFAULT NULL,
    reason VARCHAR(255) DEFAULT NULL,
    status VARCHAR(255) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 15. reviews
-- ============================================
CREATE TABLE reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment TEXT,
    created_at DATETIME(6) DEFAULT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    rating INT DEFAULT NULL,
    verified BIT(1) DEFAULT NULL,
    order_id BIGINT DEFAULT NULL,
    product_id BIGINT DEFAULT NULL,
    store_id BIGINT DEFAULT NULL,
    user_id BIGINT DEFAULT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (store_id) REFERENCES stores(store_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================
-- 16. notifications
-- ============================================
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) DEFAULT NULL,
    is_read BIT(1) DEFAULT NULL,
    link_url VARCHAR(255) DEFAULT NULL,
    message TEXT,
    title VARCHAR(255) DEFAULT NULL,
    type VARCHAR(255) DEFAULT NULL,
    user_id BIGINT DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;

-- ============================================
-- PHASE 2: BASE DATA (Roles + Users + Stores từ backup gốc)
-- ============================================

-- Roles
INSERT INTO roles (role_id, name, description) VALUES
(1, 'ROLE_STORE_OWNER', 'Chủ cửa hàng - Quản lý store, sản phẩm, deal, đơn hàng'),
(2, 'ROLE_USER', 'Người dùng thông thường - Buyer'),
(3, 'ROLE_ADMIN', 'Quản trị viên hệ thống với toàn quyền'),
(4, 'ROLE_MODERATOR', 'Kiểm duyệt viên với quyền quản lý nội dung'),
(5, 'ROLE_STORE_STAFF', 'Nhân viên cửa hàng với quyền hạn chế');

-- Users (giữ nguyên 7 user từ backup, password: 123456 cho tất cả)
INSERT INTO users (user_id, active, address, avatar_url, created_at, email, full_name, password, phone, provider, updated_at, username, role_id, work_store_id, weak_password) VALUES
(1, b'1', NULL, NULL, '2026-04-25 12:44:46.774245', 'store1@dealxanh.com', 'Nguyễn Phan Anh (Store 1 Owner)', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0777451107', 'local', NULL, 'anhphan', 1, NULL, 0),
(2, b'1', NULL, NULL, '2026-04-25 19:01:27.617957', 'store2@dealxanh.com', 'Trần Văn B (Store 2 Owner - Rejected)', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0902222222', 'local', NULL, 'store2_owner', 1, NULL, 0),
(3, b'1', 'Đường Quốc Lộ 21, Huyện Gia Bình, Tỉnh Bắc Ninh', NULL, '2026-04-25 21:20:02.484247', 'store3@dealxanh.com', 'Nguyễn Trung Anh (Store 3 Owner)', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0832132728', 'local', NULL, 'anhnphe186085', 1, NULL, 0),
(4, b'1', 'Nghi Xuân, Huyện Nghi Xuân, Tỉnh Hà Tĩnh', NULL, '2026-05-06 07:34:09.942043', 'store4@dealxanh.com', 'Phan Anh (Store 4 Owner)', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0777451107', 'local', NULL, 'trumphagamesteam', 1, NULL, 0),
(5, b'1', 'Hà Nội, Việt Nam', NULL, '2026-05-06 07:50:36.741126', 'buyer@dealxanh.com', 'Nguyễn Văn Buyer (Khách hàng chính)', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0832132728', 'local', NULL, 'nguyenxuanphananh', 2, NULL, 0),
(6, b'1', 'Hồ Chí Minh, Việt Nam', NULL, '2026-05-07 15:05:58.000000', 'admin@dealxanh.com', 'Quản Trị Viên Hệ Thống', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0123456789', 'local', '2026-05-07 15:05:58.000000', 'admin', 3, NULL, 0),
(7, b'1', 'Hà Nội, Việt Nam', NULL, '2026-05-07 15:05:58.000000', 'moderator@dealxanh.com', 'Moderator Hệ Thống', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0987654321', 'local', '2026-05-07 15:05:58.000000', 'moderator', 4, NULL, 0);

-- Stores (giữ nguyên 4 store từ backup)
INSERT INTO stores (store_id, store_name, owner_id, phone, address, city, district, status, approval_mode, bank_name, bank_account_owner, bank_account_number, business_type, categories, cccd_url, business_license_url, vsattp_url, open_time, close_time, operating_days, max_slots_per_time, pickup_duration_minutes, pickup_slots, partner_tier, description, created_at, updated_at, reviewed_at, approved_by) VALUES
(1, 'abcbcbcbc - Quán Ăn HN', 1, '0777451107', 'Đường Quốc Lộ 21', 'Hà Nội', 'Hoàn Kiếm', 'ACTIVE', 'manual', 'Vietcombank (VCB)', 'Nguyễn Phan Anh', '131313123123131', 'Nhà hàng / Quán ăn', 'Đồ ăn mặn, Thực phẩm đóng gói', '/uploads/cccd_store1.png', '/uploads/license_store1.png', '/uploads/vsattp_store1.png', '08:00', '20:00', 'T2,T3,T4,T5,T6,T7', 20, 30, '16:00-18:00, 17:00-19:00, 18:00-20:00, 19:00-21:00', 'GOLD', 'Quán ăn bình dân tại Hà Nội - Phở, Bún, Cơm', '2026-04-25 12:44:46.865532', '2026-05-08 00:42:00.109802', '2026-05-08 00:42:00.109802', 6),
(2, 'Cửa Hàng Bị Từ Chối', 2, NULL, NULL, NULL, NULL, 'REJECTED', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'BRONZE', NULL, '2026-04-25 19:01:27.792253', '2026-05-08 10:02:11.149464', '2026-05-08 10:02:11.149464', NULL),
(3, 'bcbc - Đặc Sản BN', 3, '0832132728', 'Đường Quốc Lộ 21', 'Tỉnh Bắc Ninh', 'Huyện Gia Bình', 'ACTIVE', 'manual', 'BIDV', 'Nguyễn Trung Anh', '13131312312312', 'Nhà hàng / Quán ăn', 'Đồ ăn mặn, Đồ ngọt / Bánh', '/uploads/cccd_store3.png', '/uploads/license_store3.png', '/uploads/vsattp_store3.png', '08:00', '20:00', 'T2,T3,T4,T5,T6,T7', 20, 30, '16:00-18:00, 17:00-19:00, 18:00-20:00, 19:00-21:00', 'SILVER', 'Đặc sản Bắc Ninh - Bánh đa cua, Nem chua rán, Chè Thái', '2026-04-25 21:20:02.585837', '2026-05-08 10:02:43.315964', '2026-05-08 10:02:43.315964', 6),
(4, 'Tiệm Bánh Nhà Làm', 4, '0777451107', 'Nghi Xuân', 'Tỉnh Hà Tĩnh', 'Huyện Nghi Xuân', 'ACTIVE', 'manual', 'VietinBank', 'Phan Anh', '13131312312313', 'Nhà hàng / Quán ăn', 'Đồ ngọt / Bánh, Thực phẩm đóng gói', '/uploads/cccd_store4.png', '/uploads/license_store4.png', '/uploads/vsattp_store4.png', '08:00', '20:00', 'T2,T3,T4,T5,T6,T7', 20, 30, '16:00-18:00, 17:00-19:00, 18:00-20:00, 19:00-21:00', 'BRONZE', 'Tiệm bánh handmade - Bánh dứa, Bông lan, Mochi', '2026-05-06 07:34:10.018444', '2026-05-08 01:48:47.081729', '2026-05-08 01:48:47.081729', 6);

-- ============================================
-- PHASE 3: ADDITIONAL USERS (Staff, Extra Buyers, Pending Seller)
-- ============================================

-- Staff users (ROLE_STORE_STAFF)
INSERT INTO users (active, address, created_at, email, full_name, password, phone, provider, updated_at, username, role_id, work_store_id, weak_password) VALUES
(b'1', 'Hà Nội, Việt Nam',   NOW(), 'staff1@dealxanh.com', 'Trần Văn Nhân Viên (Staff Store 1)', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0905111111', 'local', NOW(), 'staff_store1', 5, 1, 1),
(b'1', 'Bắc Ninh, Việt Nam', NOW(), 'staff3@dealxanh.com', 'Lê Thị Nhân Viên (Staff Store 3)',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0905222222', 'local', NOW(), 'staff_store3', 5, 3, 1),
(b'1', 'Hà Tĩnh, Việt Nam',  NOW(), 'staff4@dealxanh.com', 'Phạm Văn Nhân Viên (Staff Store 4)', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0905333333', 'local', NOW(), 'staff_store4', 5, 4, 1);

SET @staff1_id = LAST_INSERT_ID();
SET @staff3_id = @staff1_id + 1;
SET @staff4_id = @staff1_id + 2;

-- Extra buyers
INSERT INTO users (active, created_at, email, full_name, password, phone, provider, updated_at, username, role_id, weak_password) VALUES
(b'1', NOW(), 'minh@test.com',   'Phạm Minh Tuấn',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0911111111', 'local', NOW(), 'buyer_minh', 2, 0),
(b'1', NOW(), 'lan@test.com',    'Hoàng Thanh Lan',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0922222222', 'local', NOW(), 'buyer_lan',  2, 0),
(b'1', NOW(), 'huy@test.com',    'Đỗ Quốc Huy',      '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0933333333', 'local', NOW(), 'buyer_huy',  2, 0),
(b'1', NOW(), 'mai@test.com',    'Vũ Thị Mai',       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0944444444', 'local', NOW(), 'buyer_mai',  2, 0),
(b'1', NOW(), 'an@test.com',     'Nguyễn Văn An',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0955555555', 'local', NOW(), 'buyer_an',   2, 0);

SET @buyer2_id = LAST_INSERT_ID();
SET @buyer3_id = @buyer2_id + 1;
SET @buyer4_id = @buyer2_id + 2;
SET @buyer5_id = @buyer2_id + 3;
SET @buyer6_id = @buyer2_id + 4;

-- PENDING Seller for verification testing
INSERT INTO users (active, created_at, email, full_name, password, phone, provider, updated_at, username, role_id, weak_password) VALUES
(b'1', NOW(), 'seller_pending@test.com', 'Nguyễn Văn Chờ Duyệt', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0966666666', 'local', NOW(), 'seller_pending', 1, 0);
SET @seller_pending_id = LAST_INSERT_ID();

-- PENDING Store
INSERT INTO stores (store_name, owner_id, business_type, categories, city, district, address, phone, status, approval_mode,
    bank_name, bank_account_owner, bank_account_number, business_license_url, cccd_url, vsattp_url,
    open_time, close_time, operating_days, max_slots_per_time, pickup_duration_minutes, pickup_slots, partner_tier, created_at, updated_at)
VALUES ('Quán Ăn Chờ Duyệt', @seller_pending_id,
    'Nhà hàng / Quán ăn', 'Đồ ăn mặn, Đồ uống / Cà phê', 'Đà Nẵng', 'Hải Châu', '123 Nguyễn Văn Linh', '0966666666',
    'PENDING', 'manual', 'Vietcombank (VCB)', 'Nguyễn Văn Chờ Duyệt', '1234567890123',
    '/uploads/license_pending.png', '/uploads/cccd_pending.png', '/uploads/vsattp_pending.png',
    '08:00', '20:00', 'T2,T3,T4,T5,T6,T7', 15, 30, '16:00-18:00,18:00-20:00', 'BRONZE', NOW(), NOW());
SET @store_pending_id = LAST_INSERT_ID();

-- ============================================
-- PHASE 4: REFERENCE VARIABLES
-- ============================================
SET @buyer_id  = 5;
SET @admin_id  = 6;
SET @mod_id    = 7;
SET @store1_id = 1;
SET @store3_id = 3;
SET @store4_id = 4;
SET @owner1_id = 1;
SET @owner3_id = 3;
SET @owner4_id = 4;

-- ============================================
-- PHASE 5: CATEGORIES (6 danh mục)
-- ============================================
INSERT INTO categories (name, description, icon_url) VALUES
('Đồ ăn mặn', 'Các món ăn chính: cơm, phở, bún, bánh mì...', 'https://i.pinimg.com/webp/736x/d3/cd/48/d3cd48e72749f6c88ff404466ba06f55.webp'),
('Đồ ngọt / Bánh', 'Bánh ngọt, chè, dessert...', 'https://i.pinimg.com/736x/60/f2/1e/60f21e07b69ed6cac3442de27ba4e4c2.jpg'),
('Đồ uống / Cà phê', 'Cà phê, trà sữa, nước ép...', 'https://i.pinimg.com/736x/a3/3c/5c/a33c5c6cdaa90fe835ee68d7c789c597.jpg'),
('Thực phẩm đóng gói', 'Thực phẩm đóng gói sẵn, đồ khô...', 'https://i.pinimg.com/736x/c0/46/68/c04668023077b07df9660cebb33e6791.jpg'),
('Ăn vặt / Snack', 'Snack, xiên que, đồ ăn nhanh...', 'https://i.pinimg.com/1200x/b8/9b/06/b89b066f3f3da16e0516d0614721faf2.jpg'),
('Cơm văn phòng', 'Cơm hộp, cơm văn phòng, suất ăn trưa...', 'https://i.pinimg.com/webp/1200x/0a/e8/de/0ae8de6c7f78ad1d9ffc55f87e5ae657.webp');

SET @cat_savory   = (SELECT category_id FROM categories WHERE name = 'Đồ ăn mặn'       LIMIT 1);
SET @cat_sweet    = (SELECT category_id FROM categories WHERE name = 'Đồ ngọt / Bánh'  LIMIT 1);
SET @cat_drinks   = (SELECT category_id FROM categories WHERE name = 'Đồ uống / Cà phê' LIMIT 1);
SET @cat_packaged = (SELECT category_id FROM categories WHERE name = 'Thực phẩm đóng gói' LIMIT 1);
SET @cat_snacks   = (SELECT category_id FROM categories WHERE name = 'Ăn vặt / Snack'  LIMIT 1);
SET @cat_rice     = (SELECT category_id FROM categories WHERE name = 'Cơm văn phòng'    LIMIT 1);

-- ============================================
-- PHASE 6: PRODUCTS (23 sản phẩm - đủ trạng thái + loại)
-- ============================================

-- Store 1: 8 products
INSERT INTO products (name, description, original_price, current_price, image_url, stock_quantity, product_type, active, deleted, approval_status, expiry_date, category_id, store_id, created_by, created_at, updated_at) VALUES
('Phở Bò Tái Chín',   'Phở bò tái chín nước dùng xương hầm 12h',                        50000, 50000, '/img/products/pho-bo-tai.jpg',    50, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-15 00:00:00', @cat_savory, @store1_id, @owner1_id, '2026-05-01 08:00:00', '2026-05-01 08:00:00'),
('Bún Chả Hà Nội',    'Bún chả nướng than hoa chuẩn vị HN',                             45000, 45000, '/img/products/bun-cha.jpg',       40, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-10 00:00:00', @cat_savory, @store1_id, @owner1_id, '2026-05-01 08:00:00', '2026-05-01 08:00:00'),
('Bánh Mì Thịt Nướng','Bánh mì thịt nướng que tre đặc biệt',                             25000, 25000, '/img/products/banhmi-thit-nuong.jpg',100,'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-20 00:00:00', @cat_savory, @store1_id, @owner1_id, '2026-05-02 08:00:00', '2026-05-02 08:00:00'),
('Bánh Mì Chả Lụa',   'Bánh mì chả lụa truyền thống',                                   20000, 19000, '/img/products/banhmi-cha-lua.jpg', 80, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-05-30 00:00:00', @cat_savory, @store1_id, @owner1_id, '2026-05-02 08:00:00', '2026-05-02 08:00:00'),
('Cơm Sườn Nướng',    'Cơm sườn nướng mật ong + canh + rau',                            40000, 38000, '/img/products/com-suon.jpg',       30, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-05 00:00:00', @cat_rice,   @store1_id, @owner1_id, '2026-05-05 08:00:00', '2026-05-05 08:00:00'),
('Trà Chanh Tươi',    'Trà chanh tươi mát lạnh',                                         15000, 15000, '/img/products/tra-chanh.jpg',     200, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-30 00:00:00', @cat_drinks, @store1_id, @owner1_id, '2026-05-05 08:00:00', '2026-05-05 08:00:00'),
('Bánh Flan Caramen', 'Bánh flan caramen béo ngậy',                                      12000, 12000, '/img/products/flan.jpg',           60, 'SPECIFIC_DEAL', b'1', b'0', 'PENDING',  '2026-06-25 00:00:00', @cat_sweet,  @store1_id, @owner1_id, '2026-05-15 08:00:00', '2026-05-15 08:00:00'),
('Phở Cuốn Thịt Bò',  'Phở cuốn thịt bò sốt me chua ngọt - BỊ TỪ CHỐI',                  55000, 55000, '/img/products/pho-cuon.jpg',       25, 'SPECIFIC_DEAL', b'0', b'0', 'REJECTED', '2026-06-01 00:00:00', @cat_savory, @store1_id, @owner1_id, '2026-05-14 08:00:00', '2026-05-14 08:00:00');

UPDATE products SET rejection_reason = 'Hình ảnh không rõ ràng, cần chụp lại sản phẩm thực tế' WHERE name = 'Phở Cuốn Thịt Bò' AND store_id = @store1_id;

SET @p1_pho_bo   = (SELECT product_id FROM products WHERE name = 'Phở Bò Tái Chín'    AND store_id = @store1_id LIMIT 1);
SET @p1_bun_cha  = (SELECT product_id FROM products WHERE name = 'Bún Chả Hà Nội'     AND store_id = @store1_id LIMIT 1);
SET @p1_banhmi   = (SELECT product_id FROM products WHERE name = 'Bánh Mì Thịt Nướng' AND store_id = @store1_id LIMIT 1);
SET @p1_chalua   = (SELECT product_id FROM products WHERE name = 'Bánh Mì Chả Lụa'    AND store_id = @store1_id LIMIT 1);
SET @p1_com_suon = (SELECT product_id FROM products WHERE name = 'Cơm Sườn Nướng'     AND store_id = @store1_id LIMIT 1);
SET @p1_tra_chanh= (SELECT product_id FROM products WHERE name = 'Trà Chanh Tươi'     AND store_id = @store1_id LIMIT 1);

-- Store 3: 7 products
INSERT INTO products (name, description, original_price, current_price, image_url, stock_quantity, product_type, active, deleted, approval_status, expiry_date, category_id, store_id, created_by, created_at, updated_at) VALUES
('Bánh Đa Cua',       'Bánh đa cua đồng chuẩn Hải Phòng',                                 35000, 34000, '/img/products/banh-da-cua.jpg',   45, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-15 00:00:00', @cat_savory, @store3_id, @owner3_id, '2026-05-03 08:00:00', '2026-05-03 08:00:00'),
('Nem Chua Rán',      'Nem chua rán chấm tương ớt',                                       20000, 20000, '/img/products/nem-chua-ran.jpg',  70, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-20 00:00:00', @cat_snacks, @store3_id, @owner3_id, '2026-05-03 08:00:00', '2026-05-03 08:00:00'),
('Chè Thái',          'Chè Thái thập cẩm đầy đủ topping',                                  25000, 25000, '/img/products/che-thai.jpg',       50, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-10 00:00:00', @cat_sweet,  @store3_id, @owner3_id, '2026-05-04 08:00:00', '2026-05-04 08:00:00'),
('Trà Sữa Trân Châu', 'Trà sữa trân châu đen đường nâu',                                  30000, 29000, '/img/products/tra-sua.jpg',        80, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-25 00:00:00', @cat_drinks, @store3_id, @owner3_id, '2026-05-04 08:00:00', '2026-05-04 08:00:00'),
('Xoài Lắc',          'Xoài lắc muối ớt chua cay - CHỜ DUYỆT',                            15000, 15000, '/img/products/xoai-lac.jpg',       40, 'SPECIFIC_DEAL', b'1', b'0', 'PENDING',  '2026-06-05 00:00:00', @cat_snacks, @store3_id, @owner3_id, '2026-05-15 08:00:00', '2026-05-15 08:00:00'),
('Cơm Tấm Sườn',      'Cơm tấm sườn bì chả trứng',                                       40000, 39000, '/img/products/com-tam.jpg',        30, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-08 00:00:00', @cat_rice,   @store3_id, @owner3_id, '2026-05-05 08:00:00', '2026-05-05 08:00:00'),
('Combo Ăn Trưa BN',  'Combo Bánh Đa Cua + Trà Sữa Trân Châu siêu tiết kiệm',             55000, 55000, '/img/products/combo-bn.jpg',       20, 'COMBO',         b'1', b'0', 'APPROVED', '2026-06-15 00:00:00', @cat_savory, @store3_id, @owner3_id, '2026-05-20 08:00:00', '2026-05-20 08:00:00');

SET @p3_banh_da  = (SELECT product_id FROM products WHERE name = 'Bánh Đa Cua'        AND store_id = @store3_id LIMIT 1);
SET @p3_nem_chua = (SELECT product_id FROM products WHERE name = 'Nem Chua Rán'       AND store_id = @store3_id LIMIT 1);
SET @p3_che_thai = (SELECT product_id FROM products WHERE name = 'Chè Thái'           AND store_id = @store3_id LIMIT 1);
SET @p3_tra_sua  = (SELECT product_id FROM products WHERE name = 'Trà Sữa Trân Châu'  AND store_id = @store3_id LIMIT 1);
SET @p3_com_tam  = (SELECT product_id FROM products WHERE name = 'Cơm Tấm Sườn'       AND store_id = @store3_id LIMIT 1);
SET @p3_combo_bn = (SELECT product_id FROM products WHERE name = 'Combo Ăn Trưa BN'   AND store_id = @store3_id LIMIT 1);

-- Store 4: 8 products
INSERT INTO products (name, description, original_price, current_price, image_url, stock_quantity, product_type, active, deleted, approval_status, expiry_date, category_id, store_id, created_by, created_at, updated_at) VALUES
('Bánh Dứa Đài Loan',        'Bánh dứa Đài Loan nhân dứa thật',                            35000, 34000, '/img/products/banh-dua.jpg',            30, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-20 00:00:00', @cat_sweet,    @store4_id, @owner4_id, '2026-05-06 08:00:00', '2026-05-06 08:00:00'),
('Bánh Bông Lan Trứng Muối', 'Bánh bông lan sốt trứng muối béo ngậy',                      45000, 44000, '/img/products/bonglan-trungmuoi.jpg',    25, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-18 00:00:00', @cat_sweet,    @store4_id, @owner4_id, '2026-05-06 08:00:00', '2026-05-06 08:00:00'),
('Bánh Quy Bơ',              'Bánh quy bơ hộp quà tặng 200g',                               60000, 59000, '/img/products/banh-quy.jpg',             20, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-07-01 00:00:00', @cat_packaged, @store4_id, @owner4_id, '2026-05-07 08:00:00', '2026-05-07 08:00:00'),
('Kẹo Dẻo Trái Cây',         'Kẹo dẻo làm từ nước ép trái cây tươi',                       25000, 25000, '/img/products/keo-deo.jpg',              50, 'SPECIFIC_DEAL', b'1', b'0', 'APPROVED', '2026-06-30 00:00:00', @cat_packaged, @store4_id, @owner4_id, '2026-05-07 08:00:00', '2026-05-07 08:00:00'),
('Bánh Mochi Nhật',          'Bánh mochi nhân kem đậu đỏ matcha - CHỜ DUYỆT',               40000, 40000, '/img/products/mochi.jpg',                15, 'SPECIFIC_DEAL', b'1', b'0', 'PENDING',  '2026-06-15 00:00:00', @cat_sweet,    @store4_id, @owner4_id, '2026-05-16 08:00:00', '2026-05-16 08:00:00'),
('Bánh Mì Hoa Cúc',          'Bánh mì hoa cúc sữa tươi thơm mềm - BỊ TỪ CHỐI',              22000, 22000, '/img/products/banhmi-hoacuc.jpg',        35, 'SPECIFIC_DEAL', b'1', b'0', 'REJECTED', '2026-06-10 00:00:00', @cat_sweet,    @store4_id, @owner4_id, '2026-05-13 08:00:00', '2026-05-13 08:00:00'),
('Combo Bánh Ngọt 3 Món',    'Combo Bánh Dứa + Bông Lan + Mochi - giá siêu ưu đãi',        99000, 99000, '/img/products/combo-banh-3mon.jpg',     10, 'COMBO',         b'1', b'0', 'APPROVED', '2026-06-10 00:00:00', @cat_sweet,    @store4_id, @owner4_id, '2026-05-20 08:00:00', '2026-05-20 08:00:00'),
('Bánh Socola Handmade',     'Bánh socola làm bằng tay - HẾT HÀNG',                         50000, 50000, '/img/products/banh-socola.jpg',          0, 'SPECIFIC_DEAL', b'0', b'0', 'APPROVED', '2026-06-30 00:00:00', @cat_sweet,    @store4_id, @owner4_id, '2026-05-01 08:00:00', '2026-05-20 08:00:00');

UPDATE products SET rejection_reason = 'Sản phẩm không đạt tiêu chuẩn vệ sinh an toàn thực phẩm' WHERE name = 'Bánh Mì Hoa Cúc' AND store_id = @store4_id;

SET @p4_banh_dua   = (SELECT product_id FROM products WHERE name = 'Bánh Dứa Đài Loan'        AND store_id = @store4_id LIMIT 1);
SET @p4_bong_lan   = (SELECT product_id FROM products WHERE name = 'Bánh Bông Lan Trứng Muối' AND store_id = @store4_id LIMIT 1);
SET @p4_banh_quy   = (SELECT product_id FROM products WHERE name = 'Bánh Quy Bơ'              AND store_id = @store4_id LIMIT 1);
SET @p4_keo_deo    = (SELECT product_id FROM products WHERE name = 'Kẹo Dẻo Trái Cây'         AND store_id = @store4_id LIMIT 1);
SET @p4_mochi      = (SELECT product_id FROM products WHERE name = 'Bánh Mochi Nhật'          AND store_id = @store4_id LIMIT 1);
SET @p4_combo_3mon = (SELECT product_id FROM products WHERE name = 'Combo Bánh Ngọt 3 Món'    AND store_id = @store4_id LIMIT 1);
SET @p4_socola     = (SELECT product_id FROM products WHERE name = 'Bánh Socola Handmade'     AND store_id = @store4_id LIMIT 1);

-- ============================================
-- PHASE 7: DEALS (12 deals - đủ 4 loại × 4 trạng thái)
-- ============================================

-- D1: FLASH_SALE - ACTIVE - Store 1
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Flash Sale 40% Phở Bún', 'FLASH40_STORE1', 'FLASH_SALE', 'Giảm 40% cho Phở và Bún tại Store 1', 'PERCENT', 40, 40000, 50000, 100, 35, 3, '2026-05-14 00:00:00', '2026-05-28 23:59:59', 'ACTIVE', 'SPECIFIC_STORES', 'CODE_REQUIRED', '/img/deals/flash40.jpg', '/uploads/banner-d1.png', 100, NOW(), NOW(), @store1_id, @admin_id);
SET @d1_id = LAST_INSERT_ID();

-- D2: VOUCHER 20K - ACTIVE - Platform
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Voucher 20K Mừng Khai Trương', 'VOUCHER20K', 'VOUCHER', 'Giảm 20,000đ cho đơn từ 100,000đ', 'FIXED', 20000, NULL, 100000, 500, 185, 2, '2026-05-10 00:00:00', '2026-06-10 23:59:59', 'ACTIVE', 'ALL_STORES', 'CODE_REQUIRED', '/img/deals/voucher20k.jpg', '/uploads/banner-d2.png', 80, NOW(), NOW(), NULL, @admin_id);
SET @d2_id = LAST_INSERT_ID();

-- D3: COMBO - ACTIVE - Store 3
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Combo Cơm Trà Siêu Rẻ', 'COMBO_COMTRA', 'COMBO', 'Combo 1 cơm + 1 trà sữa giảm 15,000đ', 'FIXED', 15000, NULL, 50000, 50, 12, 1, '2026-05-12 00:00:00', '2026-05-28 23:59:59', 'ACTIVE', 'SPECIFIC_STORES', 'CODE_REQUIRED', '/img/deals/combo-comtra.jpg', '/uploads/banner-d3.png', 70, NOW(), NOW(), @store3_id, @owner3_id);
SET @d3_id = LAST_INSERT_ID();

-- D4: SEASONAL - SCHEDULED - Platform
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Sale Hè 2026 - Giảm 25%', 'SALEHE2026', 'SEASONAL', 'Chào hè 2026 - Giảm tự động 25% đồ uống & ăn vặt', 'PERCENT', 25, 50000, 50000, NULL, 0, NULL, '2026-05-28 00:00:00', '2026-06-15 23:59:59', 'SCHEDULED', 'ALL_STORES', 'AUTO_APPLY', '/img/deals/sale-he-2026.jpg', '/uploads/banner-d4.png', 90, NOW(), NOW(), NULL, @admin_id);
SET @d4_id = LAST_INSERT_ID();

-- D5: FLASH_SALE 50% - ENDED - Store 4
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Flash Sale 50% Bánh Ngọt', 'FLASH50_BANH', 'FLASH_SALE', 'Giảm 50% cho tất cả bánh ngọt - giới hạn 6h', 'PERCENT', 50, 30000, 30000, 80, 78, 2, '2026-05-16 08:00:00', '2026-05-16 14:00:00', 'ENDED', 'SPECIFIC_STORES', 'AUTO_APPLY', '/img/deals/flash50-banh.jpg', '/uploads/banner-d5.png', 95, NOW(), NOW(), @store4_id, @owner4_id);
SET @d5_id = LAST_INSERT_ID();

-- D6: VOUCHER 15% - ACTIVE - Platform
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Voucher 15% Cho Người Mới', 'NEWUSER15', 'VOUCHER', 'Giảm 15% tối đa 30k cho người dùng mới', 'PERCENT', 15, 30000, 50000, 1000, 320, 1, '2026-05-01 00:00:00', '2026-07-01 23:59:59', 'ACTIVE', 'ALL_STORES', 'CODE_REQUIRED', '/img/deals/newuser15.jpg', '/uploads/banner-d6.png', 60, NOW(), NOW(), NULL, @admin_id);
SET @d6_id = LAST_INSERT_ID();

-- D7: COMBO - SCHEDULED - Store 4
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Combo Bánh Trà Chiều', 'COMBO_BANHTRA', 'COMBO', 'Combo 1 bánh + 1 trà giảm 10,000đ', 'FIXED', 10000, NULL, 40000, 30, 0, NULL, '2026-05-28 00:00:00', '2026-06-05 23:59:59', 'SCHEDULED', 'SPECIFIC_STORES', 'CODE_REQUIRED', '/img/deals/combo-banhtra.jpg', '/uploads/banner-d7.png', 65, NOW(), NOW(), @store4_id, @owner4_id);
SET @d7_id = LAST_INSERT_ID();

-- D8: SEASONAL - SCHEDULED - Platform
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Tết Đoan Ngọ - Giảm 20%', 'TETDOAN_NGO', 'SEASONAL', 'Mừng Tết Đoan Ngọ giảm 20% toàn bộ bánh kẹo', 'PERCENT', 20, 40000, 100000, NULL, 0, NULL, '2026-05-30 00:00:00', '2026-06-10 23:59:59', 'SCHEDULED', 'ALL_STORES', 'AUTO_APPLY', '/img/deals/tet-doan-ngo.jpg', '/uploads/banner-d8.png', 75, NOW(), NOW(), NULL, @admin_id);
SET @d8_id = LAST_INSERT_ID();

-- D9: FLASH_SALE - ENDED - Store 1
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Flash Sale Cơm Trưa 30%', 'FLASH30_COM', 'FLASH_SALE', 'Giảm 30% các suất cơm trưa', 'PERCENT', 30, 20000, 30000, 60, 60, 2, '2026-05-01 10:00:00', '2026-05-01 17:00:00', 'ENDED', 'SPECIFIC_STORES', 'AUTO_APPLY', '/img/deals/flash30-com.jpg', '/uploads/banner-d9.png', 50, NOW(), NOW(), @store1_id, @owner1_id);
SET @d9_id = LAST_INSERT_ID();

-- D10: VOUCHER 50K - ACTIVE - Platform
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Voucher 50K Đơn Lớn', 'VOUCHER50K_BIG', 'VOUCHER', 'Giảm 50,000đ cho đơn từ 300,000đ', 'FIXED', 50000, NULL, 300000, 200, 47, 3, '2026-05-08 00:00:00', '2026-06-08 23:59:59', 'ACTIVE', 'ALL_STORES', 'CODE_REQUIRED', '/img/deals/voucher50k.jpg', '/uploads/banner-d10.png', 85, NOW(), NOW(), NULL, @admin_id);
SET @d10_id = LAST_INSERT_ID();

-- D11: SEASONAL - PAUSED - Platform
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Sale Cuối Tuần 15%', 'WEEKEND15', 'SEASONAL', 'Giảm 15% cuối tuần - đang tạm dừng', 'PERCENT', 15, 30000, 50000, NULL, 0, NULL, '2026-05-20 00:00:00', '2026-06-20 23:59:59', 'PAUSED', 'ALL_STORES', 'AUTO_APPLY', '/img/deals/weekend15.jpg', '/uploads/banner-d11.png', 55, NOW(), NOW(), NULL, @admin_id);
SET @d11_id = LAST_INSERT_ID();

-- D12: FLASH_SALE - PAUSED - Store 1
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Flash Sale Phở Bò 35%', 'FLASH35_PHO', 'FLASH_SALE', 'Giảm 35% Phở Bò - đang tạm dừng', 'PERCENT', 35, 35000, 30000, 80, 20, 2, '2026-05-18 00:00:00', '2026-05-30 23:59:59', 'PAUSED', 'SPECIFIC_STORES', 'CODE_REQUIRED', '/img/deals/flash35-pho.jpg', '/uploads/banner-d12.png', 88, NOW(), NOW(), @store1_id, @owner1_id);
SET @d12_id = LAST_INSERT_ID();

-- ============================================
-- PHASE 8: DEAL CATEGORIES (Platform deals)
-- ============================================
INSERT INTO deal_categories (deal_id, category_id, priority, active, created_at) VALUES
(@d2_id, @cat_savory, 1, TRUE, NOW()), (@d2_id, @cat_rice,   2, TRUE, NOW()), (@d2_id, @cat_drinks, 3, TRUE, NOW()),
(@d4_id, @cat_drinks, 1, TRUE, NOW()), (@d4_id, @cat_snacks, 2, TRUE, NOW()),
(@d6_id, @cat_savory, 1, TRUE, NOW()), (@d6_id, @cat_sweet,  2, TRUE, NOW()), (@d6_id, @cat_drinks, 3, TRUE, NOW()), (@d6_id, @cat_snacks, 4, TRUE, NOW()), (@d6_id, @cat_rice, 5, TRUE, NOW()),
(@d8_id, @cat_sweet,  1, TRUE, NOW()), (@d8_id, @cat_packaged, 2, TRUE, NOW()),
(@d10_id, @cat_savory, 1, TRUE, NOW()), (@d10_id, @cat_sweet,  2, TRUE, NOW()), (@d10_id, @cat_drinks, 3, TRUE, NOW()), (@d10_id, @cat_rice, 4, TRUE, NOW()),
(@d11_id, @cat_savory, 1, TRUE, NOW()), (@d11_id, @cat_sweet,  2, TRUE, NOW()), (@d11_id, @cat_drinks, 3, TRUE, NOW()), (@d11_id, @cat_snacks, 4, TRUE, NOW()), (@d11_id, @cat_packaged, 5, TRUE, NOW()), (@d11_id, @cat_rice, 6, TRUE, NOW());

-- ============================================
-- PHASE 9: DEAL PRODUCTS (Store deals)
-- ============================================
INSERT INTO deal_products (deal_id, product_id, original_price, sale_price, max_quantity, sold_quantity, priority, created_at) VALUES
(@d1_id, @p1_pho_bo,  50000, 30000, 30, 18, 1, NOW()), (@d1_id, @p1_bun_cha, 45000, 27000, 25, 12, 2, NOW()),
(@d3_id, @p3_com_tam, 40000, 28000, 20,  8, 1, NOW()), (@d3_id, @p3_tra_sua, 30000, 25000, 30,  4, 2, NOW()),
(@d5_id, @p4_banh_dua, 35000, 17500, 15, 15, 1, NOW()), (@d5_id, @p4_bong_lan, 45000, 22500, 10, 10, 2, NOW()), (@d5_id, @p4_banh_quy, 60000, 30000, 10, 3, 3, NOW()),
(@d7_id, @p4_banh_quy, 60000, 50000, 15, 0, 1, NOW()), (@d7_id, @p4_keo_deo,  25000, 20000, 20, 0, 2, NOW()),
(@d9_id, @p1_com_suon, 40000, 28000, 30, 30, 1, '2026-05-01 10:00:00'),
(@d12_id, @p1_pho_bo,   50000, 32500, 50, 15, 1, NOW()), (@d12_id, @p1_tra_chanh, 15000, 9750, 30, 5, 2, NOW());

-- ============================================
-- PHASE 10: ORDERS (28 orders - đủ 5 trạng thái × nhiều payment)
-- ============================================

-- Store 1: 8 orders
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, coupon_code, scheduled_pickup_time, actual_pickup_time, note, created_at, updated_at) VALUES
(@buyer_id,  @store1_id,  95000, 20000, 75000, 'COMPLETED',        'PAID',   'MOMO',          'VOUCHER20K',     '2026-05-14 12:00:00', '2026-05-14 12:15:00', 'Ít hành, nhiều rau', '2026-05-14 10:30:00', '2026-05-14 12:15:00'),
(@buyer_id,  @store1_id,  50000, 20000, 30000, 'COMPLETED',        'PAID',   'CASH',          'FLASH40_STORE1', '2026-05-15 11:30:00', '2026-05-15 11:45:00', 'Thêm tương ớt',      '2026-05-15 09:00:00', '2026-05-15 11:45:00'),
(@buyer2_id, @store1_id,  45000,     0, 45000, 'READY_FOR_PICKUP', 'PAID',   'ZALOPAY',       NULL,             '2026-05-26 12:30:00', NULL,                   'Gọi trước khi đến',  '2026-05-25 10:00:00', '2026-05-25 11:00:00'),
(@buyer3_id, @store1_id,  25000,     0, 25000, 'PENDING',          'UNPAID', 'CASH',          NULL,             '2026-05-26 13:00:00', NULL,                   NULL,                 '2026-05-25 11:30:00', '2026-05-25 11:30:00'),
(@buyer4_id, @store1_id, 105000, 30000, 75000, 'CONFIRMED',        'PAID',   'BANK_TRANSFER', 'VOUCHER20K',     '2026-05-26 14:00:00', NULL,                   'Đóng gói kỹ',        '2026-05-25 08:00:00', '2026-05-25 08:30:00'),
(@buyer5_id, @store1_id,  20000,     0, 20000, 'CANCELLED',        'UNPAID', 'CASH',          NULL,             '2026-05-25 13:00:00', NULL,                   'Khách đổi ý',        '2026-05-25 11:00:00', '2026-05-25 11:15:00'),
(@buyer6_id, @store1_id,  40000,     0, 40000, 'COMPLETED',        'PAID',   'BANK_TRANSFER', NULL,             '2026-05-23 12:00:00', '2026-05-23 12:10:00', NULL,                 '2026-05-23 10:00:00', '2026-05-23 12:10:00'),
(@buyer_id,  @store1_id,  35000,     0, 35000, 'CONFIRMED',        'UNPAID', 'BANK_TRANSFER', NULL,             '2026-05-26 16:00:00', NULL,                   'Chờ thanh toán QR',  '2026-05-25 09:00:00', '2026-05-25 09:00:00');

SET @o1_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id AND user_id = @buyer_id  AND status = 'COMPLETED'        AND DATE(created_at) = '2026-05-14' LIMIT 1);
SET @o2_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id AND user_id = @buyer_id  AND status = 'COMPLETED'        AND DATE(created_at) = '2026-05-15' LIMIT 1);
SET @o3_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id AND user_id = @buyer2_id AND status = 'READY_FOR_PICKUP' LIMIT 1);
SET @o4_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id AND user_id = @buyer3_id AND status = 'PENDING'          LIMIT 1);
SET @o5_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id AND user_id = @buyer4_id AND status = 'CONFIRMED'        LIMIT 1);
SET @o6_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id AND user_id = @buyer5_id AND status = 'CANCELLED'        LIMIT 1);
SET @o7_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id AND user_id = @buyer6_id AND status = 'COMPLETED'        AND DATE(created_at) = '2026-05-23' LIMIT 1);
SET @o8_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id AND user_id = @buyer_id  AND status = 'CONFIRMED'        AND payment_status = 'UNPAID' AND DATE(created_at) = '2026-05-25' LIMIT 1);

-- Store 3: 7 orders
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, coupon_code, scheduled_pickup_time, actual_pickup_time, note, created_at, updated_at) VALUES
(@buyer_id,  @store3_id, 65000, 15000, 50000, 'COMPLETED',        'PAID',   'MOMO',          'COMBO_COMTRA', '2026-05-14 12:00:00', '2026-05-14 12:20:00', 'Ngon lắm',           '2026-05-14 10:00:00', '2026-05-14 12:20:00'),
(@buyer2_id, @store3_id, 25000,     0, 25000, 'COMPLETED',        'PAID',   'CASH',          NULL,           '2026-05-15 11:00:00', '2026-05-15 11:05:00', NULL,                 '2026-05-15 09:30:00', '2026-05-15 11:05:00'),
(@buyer3_id, @store3_id, 55000,     0, 55000, 'READY_FOR_PICKUP', 'PAID',   'ZALOPAY',       NULL,           '2026-05-26 12:00:00', NULL,                   'Chan nhiều nước',    '2026-05-25 09:00:00', '2026-05-25 10:30:00'),
(@buyer4_id, @store3_id, 20000,     0, 20000, 'PENDING',          'UNPAID', 'CASH',          NULL,           '2026-05-26 13:30:00', NULL,                   NULL,                 '2026-05-25 12:00:00', '2026-05-25 12:00:00'),
(@buyer5_id, @store3_id, 40000,     0, 40000, 'CANCELLED',        'UNPAID', 'BANK_TRANSFER', NULL,           '2026-05-25 14:00:00', NULL,                   'Hết nguyên liệu',    '2026-05-25 11:00:00', '2026-05-25 11:30:00'),
(@buyer6_id, @store3_id, 55000,     0, 55000, 'CONFIRMED',        'PAID',   'BANK_TRANSFER', NULL,           '2026-05-26 15:00:00', NULL,                   'Đặt trước 1 ngày',   '2026-05-25 07:00:00', '2026-05-25 07:30:00'),
(@buyer_id,  @store3_id, 35000,  5250, 29750, 'COMPLETED',        'PAID',   'MOMO',          'NEWUSER15',    '2026-05-22 12:00:00', '2026-05-22 12:10:00', NULL,                 '2026-05-22 10:00:00', '2026-05-22 12:10:00');

SET @o9_id  = (SELECT order_id FROM orders WHERE store_id = @store3_id AND user_id = @buyer_id  AND coupon_code = 'COMBO_COMTRA' LIMIT 1);
SET @o10_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND user_id = @buyer2_id AND status = 'COMPLETED'        AND DATE(created_at) = '2026-05-15' LIMIT 1);
SET @o11_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND user_id = @buyer3_id AND status = 'READY_FOR_PICKUP' LIMIT 1);
SET @o12_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND user_id = @buyer4_id AND status = 'PENDING'          LIMIT 1);
SET @o13_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND user_id = @buyer5_id AND status = 'CANCELLED'        LIMIT 1);
SET @o14_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND user_id = @buyer6_id AND status = 'CONFIRMED'        LIMIT 1);
SET @o15_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND user_id = @buyer_id  AND coupon_code = 'NEWUSER15'   LIMIT 1);

-- Store 4: 7 orders
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, coupon_code, scheduled_pickup_time, actual_pickup_time, note, created_at, updated_at) VALUES
(@buyer_id,  @store4_id, 80000, 40000, 40000, 'COMPLETED',        'PAID',   'MOMO',          'FLASH50_BANH', '2026-05-16 10:00:00', '2026-05-16 10:10:00', 'Bánh ngon xuất sắc', '2026-05-16 08:00:00', '2026-05-16 10:10:00'),
(@buyer2_id, @store4_id, 45000,  6750, 38250, 'COMPLETED',        'PAID',   'CASH',          'NEWUSER15',    '2026-05-15 15:00:00', '2026-05-15 15:20:00', NULL,                '2026-05-15 13:00:00', '2026-05-15 15:20:00'),
(@buyer3_id, @store4_id, 60000,     0, 60000, 'CONFIRMED',        'PAID',   'ZALOPAY',       NULL,           '2026-05-26 16:00:00', NULL,                   'Gói quà tặng',      '2026-05-25 13:00:00', '2026-05-25 13:30:00'),
(@buyer4_id, @store4_id, 25000,     0, 25000, 'PENDING',          'UNPAID', 'CASH',          NULL,           '2026-05-26 17:00:00', NULL,                   NULL,                '2026-05-25 14:00:00', '2026-05-25 14:00:00'),
(@buyer5_id, @store4_id, 40000,     0, 40000, 'CANCELLED',        'UNPAID', 'CASH',          NULL,           '2026-05-25 16:00:00', NULL,                   'Hết Mochi - hủy',   '2026-05-25 12:00:00', '2026-05-25 12:30:00'),
(@buyer6_id, @store4_id, 99000,     0, 99000, 'COMPLETED',        'PAID',   'BANK_TRANSFER', NULL,           '2026-05-24 15:00:00', '2026-05-24 15:15:00', 'Combo rất đáng tiền','2026-05-24 12:00:00', '2026-05-24 15:15:00'),
(@buyer_id,  @store4_id, 80000,     0, 80000, 'READY_FOR_PICKUP', 'PAID',   'MOMO',          NULL,           '2026-05-26 11:00:00', NULL,                   'Chờ khách đến lấy', '2026-05-25 08:00:00', '2026-05-25 08:30:00');

SET @o16_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND user_id = @buyer_id  AND coupon_code = 'FLASH50_BANH' LIMIT 1);
SET @o17_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND user_id = @buyer2_id AND status = 'COMPLETED'        AND DATE(created_at) = '2026-05-15' LIMIT 1);
SET @o18_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND user_id = @buyer3_id AND status = 'CONFIRMED'        LIMIT 1);
SET @o19_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND user_id = @buyer4_id AND status = 'PENDING'          LIMIT 1);
SET @o20_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND user_id = @buyer5_id AND status = 'CANCELLED'        LIMIT 1);
SET @o21_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND user_id = @buyer6_id AND status = 'COMPLETED'        AND DATE(created_at) = '2026-05-24' LIMIT 1);
SET @o22_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND user_id = @buyer_id  AND status = 'READY_FOR_PICKUP' AND DATE(created_at) = '2026-05-25' LIMIT 1);

-- Past orders (7-day chart data)
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, coupon_code, scheduled_pickup_time, actual_pickup_time, note, created_at, updated_at) VALUES
(@buyer2_id, @store1_id,  25000,     0, 25000, 'COMPLETED', 'PAID', 'CASH',          NULL,             '2026-05-19 12:00:00', '2026-05-19 12:15:00', NULL,           '2026-05-19 10:00:00', '2026-05-19 12:15:00'),
(@buyer3_id, @store3_id,  50000, 10000, 40000, 'COMPLETED', 'PAID', 'MOMO',          'VOUCHER20K',     '2026-05-20 12:00:00', '2026-05-20 12:10:00', NULL,           '2026-05-20 10:00:00', '2026-05-20 12:10:00'),
(@buyer4_id, @store4_id,  80000, 20000, 60000, 'COMPLETED', 'PAID', 'ZALOPAY',       'VOUCHER50K_BIG', '2026-05-21 12:00:00', '2026-05-21 12:20:00', NULL,           '2026-05-21 09:00:00', '2026-05-21 12:20:00'),
(@buyer5_id, @store1_id, 100000, 40000, 60000, 'COMPLETED', 'PAID', 'BANK_TRANSFER', 'FLASH40_STORE1', '2026-05-22 12:00:00', '2026-05-22 12:30:00', 'Nhiều thịt',   '2026-05-22 10:00:00', '2026-05-22 12:30:00'),
(@buyer6_id, @store3_id,  35000,     0, 35000, 'COMPLETED', 'PAID', 'CASH',          NULL,             '2026-05-23 12:00:00', '2026-05-23 12:05:00', NULL,           '2026-05-23 11:00:00', '2026-05-23 12:05:00'),
(@buyer_id,  @store4_id,  70000, 10500, 59500, 'COMPLETED', 'PAID', 'MOMO',          'NEWUSER15',      '2026-05-24 12:00:00', '2026-05-24 12:15:00', NULL,           '2026-05-24 10:30:00', '2026-05-24 12:15:00');

SET @o23_id = (SELECT order_id FROM orders WHERE store_id = @store1_id AND DATE(created_at) = '2026-05-19' LIMIT 1);
SET @o24_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND DATE(created_at) = '2026-05-20' LIMIT 1);
SET @o25_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND DATE(created_at) = '2026-05-21' LIMIT 1);
SET @o26_id = (SELECT order_id FROM orders WHERE store_id = @store1_id AND DATE(created_at) = '2026-05-22' LIMIT 1);
SET @o27_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND DATE(created_at) = '2026-05-23' AND user_id = @buyer6_id LIMIT 1);
SET @o28_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND DATE(created_at) = '2026-05-24' AND user_id = @buyer_id AND coupon_code = 'NEWUSER15' LIMIT 1);

-- ============================================
-- PHASE 11: ORDER ITEMS
-- ============================================
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(@o1_id, @p1_pho_bo, 1, 50000), (@o1_id, @p1_bun_cha, 1, 45000),
(@o2_id, @p1_pho_bo, 1, 50000),
(@o3_id, @p1_bun_cha, 1, 45000),
(@o4_id, @p1_banhmi, 1, 25000),
(@o5_id, @p1_pho_bo, 1, 50000), (@o5_id, @p1_bun_cha, 1, 45000), (@o5_id, @p1_tra_chanh, 1, 15000),
(@o6_id, @p1_chalua, 1, 20000),
(@o7_id, @p1_com_suon, 1, 40000),
(@o8_id, @p1_chalua, 1, 20000), (@o8_id, @p1_tra_chanh, 1, 15000),
-- Store 3
(@o9_id, @p3_com_tam, 1, 40000), (@o9_id, @p3_tra_sua, 1, 30000),
(@o10_id, @p3_che_thai, 1, 25000),
(@o11_id, @p3_banh_da, 1, 35000), (@o11_id, @p3_nem_chua, 1, 20000),
(@o12_id, @p3_nem_chua, 1, 20000),
(@o13_id, @p3_com_tam, 1, 40000),
(@o14_id, @p3_combo_bn, 1, 55000),
(@o15_id, @p3_banh_da, 1, 35000),
-- Store 4
(@o16_id, @p4_banh_dua, 1, 35000), (@o16_id, @p4_bong_lan, 1, 45000),
(@o17_id, @p4_bong_lan, 1, 45000),
(@o18_id, @p4_banh_quy, 1, 60000),
(@o19_id, @p4_keo_deo, 1, 25000),
(@o20_id, @p4_mochi, 1, 40000),
(@o21_id, @p4_combo_3mon, 1, 99000),
(@o22_id, @p4_banh_dua, 1, 35000), (@o22_id, @p4_bong_lan, 1, 45000),
-- Past
(@o23_id, @p1_banhmi, 1, 25000),
(@o24_id, @p3_com_tam, 1, 40000), (@o24_id, @p3_nem_chua, 1, 20000),
(@o25_id, @p4_banh_quy, 1, 60000),
(@o26_id, @p1_pho_bo, 2, 50000),
(@o27_id, @p3_banh_da, 1, 35000),
(@o28_id, @p4_bong_lan, 1, 45000);

-- ============================================
-- PHASE 12: TRANSACTIONS (SALE + PAYOUT + COMMISSION)
-- ============================================

-- SALE transactions Store 1
INSERT INTO transactions (store_id, order_id, amount, platform_fee, net_amount, type, status, description, created_at, updated_at) VALUES
(@store1_id, @o1_id,  75000, 7500,  67500, 'SALE', 'COMPLETED', 'Doanh thu đơn O1', '2026-05-14 12:15:00', '2026-05-14 12:15:00'),
(@store1_id, @o2_id,  30000, 3000,  27000, 'SALE', 'COMPLETED', 'Doanh thu đơn O2', '2026-05-15 11:45:00', '2026-05-15 11:45:00'),
(@store1_id, @o7_id,  40000, 4000,  36000, 'SALE', 'COMPLETED', 'Doanh thu đơn O7', '2026-05-23 12:10:00', '2026-05-23 12:10:00'),
(@store1_id, @o23_id, 25000, 2500,  22500, 'SALE', 'COMPLETED', 'Doanh thu đơn O23','2026-05-19 12:15:00', '2026-05-19 12:15:00'),
(@store1_id, @o26_id, 60000, 6000,  54000, 'SALE', 'COMPLETED', 'Doanh thu đơn O26','2026-05-22 12:30:00', '2026-05-22 12:30:00');

-- SALE transactions Store 3
INSERT INTO transactions (store_id, order_id, amount, platform_fee, net_amount, type, status, description, created_at, updated_at) VALUES
(@store3_id, @o9_id,  50000, 5000,  45000, 'SALE', 'COMPLETED', 'Doanh thu đơn O9', '2026-05-14 12:20:00', '2026-05-14 12:20:00'),
(@store3_id, @o10_id, 25000, 2500,  22500, 'SALE', 'COMPLETED', 'Doanh thu đơn O10','2026-05-15 11:05:00', '2026-05-15 11:05:00'),
(@store3_id, @o15_id, 29750, 2975,  26775, 'SALE', 'COMPLETED', 'Doanh thu đơn O15','2026-05-22 12:10:00', '2026-05-22 12:10:00'),
(@store3_id, @o24_id, 40000, 4000,  36000, 'SALE', 'COMPLETED', 'Doanh thu đơn O24','2026-05-20 12:10:00', '2026-05-20 12:10:00'),
(@store3_id, @o27_id, 35000, 3500,  31500, 'SALE', 'COMPLETED', 'Doanh thu đơn O27','2026-05-23 12:05:00', '2026-05-23 12:05:00');

-- SALE transactions Store 4
INSERT INTO transactions (store_id, order_id, amount, platform_fee, net_amount, type, status, description, created_at, updated_at) VALUES
(@store4_id, @o16_id, 40000, 4000,  36000, 'SALE', 'COMPLETED', 'Doanh thu đơn O16','2026-05-16 10:10:00', '2026-05-16 10:10:00'),
(@store4_id, @o17_id, 38250, 3825,  34425, 'SALE', 'COMPLETED', 'Doanh thu đơn O17','2026-05-15 15:20:00', '2026-05-15 15:20:00'),
(@store4_id, @o21_id, 99000, 9900,  89100, 'SALE', 'COMPLETED', 'Doanh thu đơn O21','2026-05-24 15:15:00', '2026-05-24 15:15:00'),
(@store4_id, @o25_id, 60000, 6000,  54000, 'SALE', 'COMPLETED', 'Doanh thu đơn O25','2026-05-21 12:20:00', '2026-05-21 12:20:00'),
(@store4_id, @o28_id, 59500, 5950,  53550, 'SALE', 'COMPLETED', 'Doanh thu đơn O28','2026-05-24 12:15:00', '2026-05-24 12:15:00');

-- PAYOUT Pending
INSERT INTO transactions (store_id, order_id, amount, net_amount, type, status, description, created_at, updated_at) VALUES
(@store1_id, NULL, 67500, 67500, 'PAYOUT', 'PENDING', 'Thanh toán kỳ 14/05 - Store 1', '2026-05-14 12:30:00', '2026-05-14 12:30:00'),
(@store3_id, NULL, 45000, 45000, 'PAYOUT', 'PENDING', 'Thanh toán kỳ 14/05 - Store 3', '2026-05-14 12:30:00', '2026-05-14 12:30:00'),
(@store4_id, NULL, 36000, 36000, 'PAYOUT', 'PENDING', 'Thanh toán kỳ 16/05 - Store 4', '2026-05-16 10:30:00', '2026-05-16 10:30:00'),
(@store1_id, NULL, 36000, 36000, 'PAYOUT', 'PENDING', 'Thanh toán kỳ 23/05 - Store 1', '2026-05-23 12:30:00', '2026-05-23 12:30:00'),
(@store4_id, NULL, 89100, 89100, 'PAYOUT', 'PENDING', 'Thanh toán kỳ 24/05 - Store 4', '2026-05-24 15:30:00', '2026-05-24 15:30:00');

-- PAYOUT Completed (có transaction_ref)
INSERT INTO transactions (store_id, order_id, amount, net_amount, type, status, description, transaction_ref, payment_method, created_at, updated_at) VALUES
(@store1_id, NULL, 22500, 22500, 'PAYOUT', 'COMPLETED', 'TT kỳ 19/05 - Store 1', 'PAYOUT-REF-001', 'BANK_TRANSFER', '2026-05-19 12:30:00', '2026-05-19 18:00:00'),
(@store3_id, NULL, 36000, 36000, 'PAYOUT', 'COMPLETED', 'TT kỳ 20/05 - Store 3', 'PAYOUT-REF-002', 'BANK_TRANSFER', '2026-05-20 12:30:00', '2026-05-20 18:00:00'),
(@store4_id, NULL, 54000, 54000, 'PAYOUT', 'COMPLETED', 'TT kỳ 21/05 - Store 4', 'PAYOUT-REF-003', 'BANK_TRANSFER', '2026-05-21 12:30:00', '2026-05-21 18:00:00');

-- COMMISSION
INSERT INTO transactions (store_id, order_id, amount, type, status, description, created_at, updated_at) VALUES
(@store1_id, @o1_id,  7500, 'COMMISSION', 'COMPLETED', 'Hoa hồng 10% đơn O1',  '2026-05-14 12:15:00', '2026-05-14 12:15:00'),
(@store3_id, @o9_id,  5000, 'COMMISSION', 'COMPLETED', 'Hoa hồng 10% đơn O9',  '2026-05-14 12:20:00', '2026-05-14 12:20:00'),
(@store4_id, @o16_id, 4000, 'COMMISSION', 'COMPLETED', 'Hoa hồng 10% đơn O16', '2026-05-16 10:10:00', '2026-05-16 10:10:00');

-- ============================================
-- PHASE 13: CARTS + CART ITEMS
-- ============================================
INSERT INTO carts (user_id, created_at, updated_at) VALUES (@buyer_id, NOW(), NOW());
SET @cart5_id = LAST_INSERT_ID();
INSERT INTO carts (user_id, created_at, updated_at) VALUES (@buyer2_id, NOW(), NOW());
SET @cart_b2_id = LAST_INSERT_ID();
INSERT INTO carts (user_id, created_at, updated_at) VALUES (@buyer3_id, NOW(), NOW());
SET @cart_b3_id = LAST_INSERT_ID();

INSERT INTO cart_items (cart_id, product_id, quantity, unit_price) VALUES
(@cart5_id,  @p1_pho_bo,   2, 50000),
(@cart5_id,  @p3_com_tam,  1, 40000),
(@cart5_id,  @p4_banh_dua, 1, 35000),
(@cart_b2_id, @p1_bun_cha, 1, 45000),
(@cart_b2_id, @p3_tra_sua, 2, 30000),
(@cart_b3_id, @p4_combo_3mon, 1, 99000);

-- ============================================
-- PHASE 14: DISPUTES (4 trạng thái)
-- ============================================
INSERT INTO disputes (user_id, order_id, reason, description, status, evidence_url, admin_note, created_at, updated_at) VALUES
(@buyer_id,  @o2_id,  'Sản phẩm không đúng mô tả', 'Đặt Phở Bò Tái Chín nhưng nhận Bún Chả', 'PENDING', '/uploads/evidence-1.png', NULL, '2026-05-25 08:00:00', '2026-05-25 08:00:00'),
(@buyer3_id, @o10_id, 'Thức ăn bị hỏng/ôi thiu', 'Chè Thái bị chua, có mùi lạ', 'PENDING', '/uploads/evidence-2.png', NULL, '2026-05-25 07:30:00', '2026-05-25 07:30:00'),
(@buyer_id,  @o1_id,  'Thiếu topping/món', 'Bún Chả thiếu nem rán đi kèm như mô tả', 'REVIEWING', NULL, 'Đang liên hệ cửa hàng', '2026-05-24 14:00:00', '2026-05-25 09:00:00'),
(@buyer2_id, @o26_id, 'Đơn hàng bị hủy không lý do', 'Cửa hàng tự hủy đơn không báo trước', 'RESOLVED_REFUND', '/uploads/evidence-3.png', 'Hoàn tiền 60,000đ. Cảnh cáo store.', '2026-05-22 15:00:00', '2026-05-23 10:00:00'),
(@buyer4_id, @o17_id, 'Yêu cầu hoàn tiền', 'Bánh bị vỡ khi nhận, không đẹp như hình', 'RESOLVED_REJECTED', '/uploads/evidence-4.png', 'Bánh vỡ do vận chuyển, không ảnh hưởng chất lượng', '2026-05-23 16:00:00', '2026-05-24 11:00:00');

-- ============================================
-- PHASE 15: REVIEWS + STORE RATINGS
-- ============================================
INSERT INTO reviews (user_id, store_id, product_id, order_id, rating, comment, verified, created_at) VALUES
(@buyer_id,  @store1_id, @p1_pho_bo,  @o1_id,  5, 'Phở ngon, nước dùng đậm đà!', b'1', '2026-05-14 13:00:00'),
(@buyer_id,  @store1_id, @p1_bun_cha, @o2_id,  4, 'Bún chả ngon nhưng hơi ít thịt', b'1', '2026-05-15 12:00:00'),
(@buyer2_id, @store1_id, @p1_pho_bo,  @o26_id, 3, 'Tạm được, giá hơi cao', b'1', '2026-05-22 13:00:00'),
(@buyer2_id, @store1_id, @p1_banhmi,  @o23_id, 5, 'Bánh mì thịt nướng rất ngon!', b'1', '2026-05-19 13:00:00'),
(@buyer_id,  @store3_id, @p3_com_tam, @o9_id,  5, 'Cơm tấm ngon đúng điệu!', b'1', '2026-05-14 13:00:00'),
(@buyer2_id, @store3_id, @p3_che_thai,@o10_id, 4, 'Chè Thái ngon nhưng hơi ngọt', b'1', '2026-05-15 11:30:00'),
(@buyer6_id, @store3_id, @p3_banh_da, @o27_id, 5, 'Bánh đa cua tuyệt vời! Chuẩn HP', b'1', '2026-05-23 13:00:00'),
(@buyer_id,  @store4_id, @p4_banh_dua,@o16_id, 5, 'Bánh dứa ngon xuất sắc!', b'1', '2026-05-16 10:30:00'),
(@buyer2_id, @store4_id, @p4_bong_lan,@o17_id, 4, 'Bánh bông lan mềm, ngon tuyệt', b'1', '2026-05-15 16:00:00'),
(@buyer_id,  @store4_id, @p4_bong_lan,@o28_id, 3, 'Lần này bánh hơi khô', b'1', '2026-05-24 13:00:00');

UPDATE stores SET average_rating = 4.25, total_reviews = 4 WHERE store_id = @store1_id;
UPDATE stores SET average_rating = 4.67, total_reviews = 3 WHERE store_id = @store3_id;
UPDATE stores SET average_rating = 4.00, total_reviews = 3 WHERE store_id = @store4_id;

-- ============================================
-- PHASE 16: QR CODES cho READY_FOR_PICKUP orders
-- ============================================
UPDATE orders SET pickup_qr_code = CONCAT('DX-', order_id, '-', SUBSTRING(MD5(RAND()), 1, 6)) WHERE status = 'READY_FOR_PICKUP';

-- ============================================
-- PHASE 17: NOTIFICATIONS
-- ============================================
INSERT INTO notifications (user_id, title, message, type, is_read, link_url, created_at) VALUES
(@admin_id,  'Store mới chờ duyệt', 'Quán Ăn Chờ Duyệt đã đăng ký', 'STORE_VERIFY', b'0', '/admin/seller-verify', '2026-05-25 08:00:00'),
(@admin_id,  'Sản phẩm mới chờ duyệt', 'Bánh Flan Caramen đang chờ phê duyệt', 'PRODUCT_APPROVAL', b'0', '/admin/products?status=PENDING', '2026-05-25 08:30:00'),
(@admin_id,  'Dispute mới', 'Buyer đã mở khiếu nại mới', 'DISPUTE', b'0', '/admin/dispute', '2026-05-25 09:00:00'),
(@admin_id,  'Doanh thu hôm qua', 'Tổng GMV: 234,500đ từ 5 đơn', 'REPORT', b'1', '/admin/dashboard', '2026-05-25 00:00:00'),
(@mod_id,    'Store mới chờ duyệt', 'Quán Ăn Chờ Duyệt đã đăng ký', 'STORE_VERIFY', b'0', '/admin/seller-verify', '2026-05-25 08:00:00'),
(@mod_id,    'Sản phẩm chờ duyệt', 'Có 3 sản phẩm đang chờ phê duyệt', 'PRODUCT_APPROVAL', b'0', '/admin/products?status=PENDING', '2026-05-25 08:30:00'),
(@owner1_id, 'Đơn hàng mới', 'Bạn có đơn hàng mới - 75,000đ', 'NEW_ORDER', b'0', '/seller/orders', '2026-05-25 11:30:00'),
(@owner1_id, 'Đơn hàng sẵn sàng nhận', 'Đơn O3 đã đến giờ pickup', 'PICKUP_REMINDER', b'0', '/seller/orders', '2026-05-25 11:00:00'),
(@owner1_id, 'Sản phẩm bị từ chối', 'Phở Cuốn Thịt Bò đã bị từ chối', 'PRODUCT_REJECTED', b'0', '/seller/products', '2026-05-14 08:30:00'),
(@owner4_id, 'Tier Upgrade', 'Chúc mừng! Bạn đã đạt hạng BẠC (SILVER)', 'TIER_UPGRADE', b'1', '/seller/dashboard', '2026-05-20 00:00:00'),
(@owner4_id, 'Deal hết hạn', 'Flash Sale 50% Bánh Ngọt đã kết thúc', 'DEAL_EXPIRED', b'1', '/seller/deals', '2026-05-16 14:00:00'),
(@buyer_id,  'Đơn hàng đã sẵn sàng', 'Đơn hàng tại abcbcbcbc đã sẵn sàng để nhận!', 'ORDER_READY', b'0', '/buyer/orders', '2026-05-25 11:00:00'),
(@buyer_id,  'Đơn hàng đã hoàn thành', 'Đơn O1 đã hoàn thành. Cảm ơn bạn!', 'ORDER_COMPLETED', b'1', '/buyer/orders', '2026-05-14 12:15:00'),
(@buyer2_id, 'Đơn hàng đã sẵn sàng', 'Đơn hàng tại abcbcbcbc đã sẵn sàng để nhận!', 'ORDER_READY', b'0', '/buyer/orders', '2026-05-25 11:00:00'),
(@buyer3_id, 'Khuyến mãi mới', 'Voucher 20K đang chờ bạn! Nhập: VOUCHER20K', 'PROMOTION', b'0', '/deals', '2026-05-25 09:00:00');

-- ============================================
-- 
-- ============================================
-- PHASE 17B: ADDITIONAL DATA (merged from Dump20260601)
-- ============================================

-- Additional Deals
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Flashsale12', 'FLASHSALE1', 'SEASONAL', 'Test deal', 'PERCENT', 15, 25000, 50000, NULL, 0, NULL, '2026-05-13 16:39:00', '2026-05-20 16:39:00', 'SCHEDULED', 'ALL_STORES', 'AUTO_APPLY', NULL, NULL, 50, NOW(), NOW(), NULL, @admin_id),
('Test Voucher', 'AAA', 'VOUCHER', 'Test voucher', 'PERCENT', 10, 10000, 100000, 100, 0, NULL, '2026-05-30 22:08:00', '2026-06-06 22:08:00', 'SCHEDULED', 'ALL_STORES', 'CODE_REQUIRED', NULL, NULL, 50, NOW(), NOW(), NULL, @admin_id),
('Flashsale100', 'FLASHSALE100', 'FLASH_SALE', 'giam gia test', 'PERCENT', 5, 10000, 15000, 10, 0, NULL, '2026-05-30 23:18:00', '2026-05-31 02:18:00', 'SCHEDULED', 'SPECIFIC_STORES', 'AUTO_APPLY', NULL, '/uploads/608e31e6682cb0c258bf2ff1ec87c2dc.jpg', 50, NOW(), NOW(), @store3_id, @owner3_id);

-- Reviews
INSERT INTO reviews (comment, created_at, image_url, rating, verified, order_id, product_id, store_id, user_id) VALUES
('Pho ngon, nuoc dung dam da. Se quay lai!', '2026-05-14 13:00:00', NULL, 5, 1, @o1_id, @p1_pho_bo, @store1_id, @buyer_id),
('Bun cha ngon nhung hoi it thit', '2026-05-15 12:00:00', NULL, 4, 1, @o2_id, @p1_bun_cha, @store1_id, @buyer_id),
('Com tam ngon dung dieu, day dan', '2026-05-14 13:00:00', NULL, 5, 1, @o7_id, @p3_com_tam, @store3_id, @buyer_id),
('Che Thai ngon nhung hoi ngot', '2026-05-15 11:30:00', NULL, 4, 1, @o8_id, @p3_che_thai, @store3_id, @buyer_id),
('Banh dua ngon xuat sac, nhan nhieu!', '2026-05-16 10:30:00', NULL, 5, 1, @o13_id, @p4_banh_dua, @store4_id, @buyer_id),
('Banh bong lan mem, ngon tuyet', '2026-05-15 16:00:00', NULL, 4, 1, @o12_id, @p4_bong_lan, @store4_id, @buyer_id),
('Lan nay banh hoi kho, khong duoc nhu lan truoc', '2026-05-15 13:00:00', NULL, 3, 1, @o13_id, @p4_bong_lan, @store4_id, @buyer_id);

-- Disputes
INSERT INTO disputes (admin_note, created_at, description, evidence_url, reason, status, updated_at, complainant_id, order_id) VALUES
(NULL, '2026-05-16 08:00:00', 'Toi dat Pho Bo Tai Chin nhung nhan duoc Bun Cha.', '/uploads/evidence-dispute-1.png', 'San pham khong dung mo ta', 'REVIEWING', '2026-05-17 21:59:18', @buyer_id, @o2_id),
(NULL, '2026-05-16 07:30:00', 'Che Thai bi chua, co mui la.', '/uploads/evidence-dispute-2.png', 'Thuc an bi hong/oi thiu', 'PENDING', '2026-05-16 07:30:00', @buyer_id, @o8_id),
('Dang lien he cua hang xac minh', '2026-05-15 14:00:00', 'Toi dat Bun Cha nhung thieu nem ran.', NULL, 'Thieu topping/mon', 'REVIEWING', '2026-05-16 09:00:00', @buyer_id, @o1_id),
('Hoan tien 60,000d. Canh cao cua hang.', '2026-05-13 15:00:00', 'Cua hang tu huy don khong bao truoc.', '/uploads/evidence-dispute-3.png', 'Don hang bi huy khong ly do', 'RESOLVED_REFUND', '2026-05-14 10:00:00', @buyer_id, @o20_id),
('Banh hoi vo do van chuyen, khong anh huong chat luong.', '2026-05-14 16:00:00', 'Banh bi vo khi nhan, khong dep nhu hinh.', '/uploads/evidence-dispute-4.png', 'Yeu cau hoan tien', 'RESOLVED_REJECTED', '2026-05-15 11:00:00', @buyer_id, @o12_id);

-- ============================================
-- PHASE 18: VERIFICATION SUMMARY
-- ============================================
SELECT '============================================' AS '';
SELECT '  DEALXANH MASTER DB - SETUP COMPLETE' AS '';
SELECT '============================================' AS '';
SELECT CONCAT('Roles:     ', COUNT(*)) FROM roles;
SELECT CONCAT('Users:     ', COUNT(*)) FROM users;
SELECT CONCAT('Stores:    ', COUNT(*)) FROM stores;
SELECT CONCAT('Categories:', COUNT(*)) FROM categories;
SELECT CONCAT('Products:  ', COUNT(*)) FROM products;
SELECT CONCAT('Deals:     ', COUNT(*)) FROM deals;
SELECT CONCAT('Orders:    ', COUNT(*)) FROM orders;
SELECT CONCAT('OrderItems:', COUNT(*)) FROM order_items;
SELECT CONCAT('Transact:  ', COUNT(*)) FROM transactions;
SELECT CONCAT('Carts:     ', COUNT(*)) FROM carts;
SELECT CONCAT('CartItems: ', COUNT(*)) FROM cart_items;
SELECT CONCAT('Disputes:  ', COUNT(*)) FROM disputes;
SELECT CONCAT('Reviews:   ', COUNT(*)) FROM reviews;
SELECT CONCAT('Notifs:    ', COUNT(*)) FROM notifications;

SELECT '' AS '';
SELECT '=== USERS BY ROLE ===' AS '';
SELECT r.name AS role, COUNT(*) AS count FROM users u JOIN roles r ON u.role_id = r.role_id GROUP BY r.name ORDER BY r.role_id;

SELECT '=== DEALS SUMMARY ===' AS '';
SELECT deal_type, status, COUNT(*) FROM deals GROUP BY deal_type, status ORDER BY deal_type, status;

SELECT '=== ORDERS SUMMARY ===' AS '';
SELECT status, COUNT(*) AS count, COALESCE(SUM(final_amount), 0) AS total FROM orders GROUP BY status ORDER BY status;

SELECT '=== TRANSACTIONS ===' AS '';
SELECT type, status, COUNT(*) AS count, COALESCE(SUM(amount), 0) AS total FROM transactions GROUP BY type, status;

SELECT '' AS '';
SELECT '=== PASSWORD: 123456 cho TẤT CẢ users ===' AS '';
SELECT '=== LOGIN URLs ===' AS '';
SELECT '  Admin:     /admin/login     (admin / admin@dealxanh.com)' AS '';
SELECT '  Moderator: /admin/login     (moderator / moderator@dealxanh.com)' AS '';
SELECT '  Seller:    /seller/login    (anhphan, anhnphe186085, trumphagamesteam, seller_pending)' AS '';
SELECT '  Staff:     /staff/login     (staff_store1, staff_store3, staff_store4)' AS '';
SELECT '  Buyer:     /login           (nguyenxuanphananh, buyer_minh, buyer_lan, buyer_huy, buyer_mai, buyer_an)' AS '';

UPDATE categories
SET 
    name = 'Đồ ăn liền đóng gói',
    description = 'Các sản phẩm ăn liền có hạn sử dụng rõ ràng như mì gói, mì ly, cháo ăn liền, phở/bún ăn liền, xúc xích đóng gói...',
    icon_url = 'https://i.pinimg.com/1200x/2d/ab/ed/2dabedd5f6e9ec32d5d0740af97f9448.jpg'
WHERE category_id = 1;

UPDATE categories
SET 
    name = 'Bánh kẹo & Đồ ngọt',
    description = 'Các loại bánh kẹo, chocolate, thạch, pudding, bánh quy và đồ ngọt đóng gói có hạn sử dụng...',
    icon_url = 'https://i.pinimg.com/1200x/68/0a/e8/680ae807d3b340551d0a34dbc2d10f87.jpg'
WHERE category_id = 2;

UPDATE categories
SET 
    name = 'Đồ uống đóng chai/lon',
    description = 'Nước ngọt, trà, cà phê, nước ép, sữa và các loại đồ uống đóng chai/lon có hạn sử dụng...',
    icon_url = 'https://i.pinimg.com/736x/ef/23/31/ef2331cf59e7f003682c2b0221510799.jpg'
WHERE category_id = 3;

UPDATE categories
SET 
    name = 'Thực phẩm đóng gói',
    description = 'Thực phẩm chế biến và đóng gói sẵn như đồ hộp, thực phẩm túi, sản phẩm khô, ngũ cốc và các mặt hàng có hạn sử dụng...',
    icon_url = 'https://i.pinimg.com/736x/c0/46/68/c04668023077b07df9660cebb33e6791.jpg'
WHERE category_id = 4;

UPDATE categories
SET 
    name = 'Snack & Ăn vặt',
    description = 'Các món ăn vặt đóng gói như snack, khoai tây chiên, rong biển, bánh mặn, hạt và đồ nhâm nhi có hạn sử dụng...',
    icon_url = 'https://i.pinimg.com/1200x/b8/9b/06/b89b066f3f3da16e0516d0614721faf2.jpg'
WHERE category_id = 5;

UPDATE categories
SET 
    name = 'Gia vị & Nguyên liệu',
    description = 'Gia vị, nước chấm, sốt, hạt nêm, bột nấu ăn và nguyên liệu phụ trợ nấu nướng có hạn sử dụng...',
    icon_url = 'https://i.pinimg.com/736x/f7/8b/64/f78b64ce916c1670fddf59f7d95d8079.jpg'
WHERE category_id = 6;