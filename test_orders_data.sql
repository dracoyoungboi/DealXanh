-- ============================================
-- SCRIPT TEST DỮ LIỆU CHO TRANG ORDERS ADMIN
-- DealXanh - Admin Orders Page Test Data
-- ============================================

-- NOTE: Script này giả định các bảng sau đã tồn tại:
-- - users ( với user_id)
-- - stores (với store_id)
-- - products (với product_id)
-- - orders (với order_id)
-- - order_items (với order_item_id)

-- ============================================
-- BƯỚC 1: TẠO USERS TEST (nếu chưa có)
-- ============================================

-- Admin user (nếu chưa có)
INSERT INTO users (username, email, password, full_name, phone, created_at, updated_at)
VALUES ('admin_test', 'admin@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin Test', '0901234567', NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Store owners cho test
INSERT INTO users (username, email, password, full_name, phone, created_at, updated_at)
VALUES
('store_owner_1', 'owner1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyễn Văn A', '0901111111', NOW(), NOW()),
('store_owner_2', 'owner2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Thị B', '0902222222', NOW(), NOW()),
('store_owner_3', 'owner3@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Văn C', '0903333333', NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Buyers (người mua hàng) cho test
INSERT INTO users (username, email, password, full_name, phone, created_at, updated_at)
VALUES
('buyer1', 'buyer1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Minh Tuấn', '0911111111', NOW(), NOW()),
('buyer2', 'buyer2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Hoàng Thanh Lan', '0922222222', NOW(), NOW()),
('buyer3', 'buyer3@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Đỗ Quốc Huy', '0933333333', NOW(), NOW()),
('buyer4', 'buyer4@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Vũ Thị Mai', '0944444444', NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Lấy user IDs đã tạo (sau khi insert)
SET @owner1_id = (SELECT user_id FROM users WHERE username='store_owner_1' LIMIT 1);
SET @owner2_id = (SELECT user_id FROM users WHERE username='store_owner_2' LIMIT 1);
SET @owner3_id = (SELECT user_id FROM users WHERE username='store_owner_3' LIMIT 1);
SET @buyer1_id = (SELECT user_id FROM users WHERE username='buyer1' LIMIT 1);
SET @buyer2_id = (SELECT user_id FROM users WHERE username='buyer2' LIMIT 1);
SET @buyer3_id = (SELECT user_id FROM users WHERE username='buyer3' LIMIT 1);
SET @buyer4_id = (SELECT user_id FROM users WHERE username='buyer4' LIMIT 1);

-- ============================================
-- BƯỚC 2: TẠO STORES TEST (nếu chưa có)
-- ============================================

INSERT INTO stores (store_name, owner_id, logo_url, business_type, city, district, address, phone, status, created_at, updated_at)
VALUES
('Bánh Mì Như Ý', @owner1_id, '/img/stores/banhmi-nhuy.jpg', 'Quán Ăn', 'Hà Nội', 'Hoàn Kiếm', '123 Hàng Bông', '0901111111', 'ACTIVE', NOW(), NOW()),
('Cà Phê Thức Mình', @owner2_id, '/img/stores/cafe-thucminh.jpg', 'Quán Cà Phê', 'Hồ Chí Minh', 'Quận 1', '456 Nguyễn Huệ', '0902222222', 'ACTIVE', NOW(), NOW()),
('Phở Bò 3 Mươi', @owner3_id, '/img/stores/pho-30.jpg', 'Quán Ăn', 'Hà Nội', 'Ba Đình', '789 Kim Mã', '0903333333', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE store_name=store_name;

-- Lấy store IDs
SET @store1_id = (SELECT store_id FROM stores WHERE store_name='Bánh Mì Như Ý' LIMIT 1);
SET @store2_id = (SELECT store_id FROM stores WHERE store_name='Cà Phê Thức Mình' LIMIT 1);
SET @store3_id = (SELECT store_id FROM stores WHERE store_name='Phở Bò 3 Mươi' LIMIT 1);

-- ============================================
-- BƯỚC 3: TẠO PRODUCTS TEST (nếu chưa có)
-- ============================================

INSERT INTO products (store_id, product_name, description, price, original_price, image_url, stock, status, created_at, updated_at)
VALUES
-- Products cho Bánh Mì Như Ý
(@store1_id, 'Bánh Mì Thịt Nướng', 'Bánh mì thịt nướng đặc biệt', 25000.0, 30000.0, '/img/products/banhmi-thitnuong.jpg', 100, 'ACTIVE', NOW(), NOW()),
(@store1_id, 'Bánh Mì Chả Lụa', 'Bánh mì chả lụa truyền thống', 20000.0, 25000.0, '/img/products/banhmi-chalua.jpg', 80, 'ACTIVE', NOW(), NOW()),
(@store1_id, 'Bánh Mì Bi Sườn', 'Bánh mì bi sườn bì tonnes', 30000.0, 35000.0, '/img/products/banhmi-bisuon.jpg', 60, 'ACTIVE', NOW(), NOW()),

-- Products cho Cà Phê Thức Mình
(@store2_id, 'Cà Phê Sữa Đá', 'Cà phê sữa đá truyền thống', 25000.0, 30000.0, '/img/products/caphe-suada.jpg', 200, 'ACTIVE', NOW(), NOW()),
(@store2_id, 'Bạc Xỉu', 'Bạc xỉu nhạt hơn cà phê sữa', 20000.0, 25000.0, '/img/products/bacxiu.jpg', 150, 'ACTIVE', NOW(), NOW()),
(@store2_id, 'Cà Phê Muối', 'Cà phê muối giảm cân', 35000.0, 40000.0, '/img/products/caphe-muoi.jpg', 100, 'ACTIVE', NOW(), NOW()),

-- Products cho Phở Bò 3 Mươi
(@store3_id, 'Phở Bò Tái', 'Phở bò tái chín từ từ', 45000.0, 50000.0, '/img/products/pho-tai.jpg', 80, 'ACTIVE', NOW(), NOW()),
(@store3_id, 'Phở Bò Gàu', 'Phở bò gàu đầy đặn', 55000.0, 60000.0, '/img/products/pho-gau.jpg', 70, 'ACTIVE', NOW(), NOW()),
(@store3_id, 'Phở Bò Đặc Biệt', 'Phở bò tất cả các phần', 65000.0, 70000.0, '/img/products/pho-dacbiet.jpg', 50, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE product_name=product_name;

-- Lấy product IDs
SET @prod1_1 = (SELECT product_id FROM products WHERE product_name='Bánh Mì Thịt Nướng' LIMIT 1);
SET @prod1_2 = (SELECT product_id FROM products WHERE product_name='Bánh Mì Chả Lụa' LIMIT 1);
SET @prod1_3 = (SELECT product_id FROM products WHERE product_name='Bánh Mì Bi Sườn' LIMIT 1);
SET @prod2_1 = (SELECT product_id FROM products WHERE product_name='Cà Phê Sữa Đá' LIMIT 1);
SET @prod2_2 = (SELECT product_id FROM products WHERE product_name='Bạc Xỉu' LIMIT 1);
SET @prod3_1 = (SELECT product_id FROM products WHERE product_name='Phở Bò Tái' LIMIT 1);
SET @prod3_2 = (SELECT product_id FROM products WHERE product_name='Phở Bò Gàu' LIMIT 1);

-- ============================================
-- BƯỚC 4: TẠO ORDERS TEST
-- ============================================

-- Orders cho Bánh Mì Như Ý (Store 1) - 5 orders
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, scheduled_pickup_time, note, created_at, updated_at)
VALUES
-- Order 1: COMPLETED
(@buyer1_id, @store1_id, 45000.0, 5000.0, 40000.0, 'COMPLETED', 'PAID', 'MOMO', DATE_SUB(NOW(), INTERVAL 3 DAY), 'Không hành, ít rau', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),

-- Order 2: COMPLETED
(@buyer2_id, @store1_id, 50000.0, 0.0, 50000.0, 'COMPLETED', 'PAID', 'BANK_TRANSFER', DATE_SUB(NOW(), INTERVAL 2 DAY), 'Thêm ớt', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),

-- Order 3: READY_FOR_PICKUP
(@buyer3_id, @store1_id, 70000.0, 10000.0, 60000.0, 'READY_FOR_PICKUP', 'PAID', 'CASH', DATE_ADD(NOW(), INTERVAL 2 HOUR), 'Gọi trước khi giao', DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW()),

-- Order 4: PENDING
(@buyer4_id, @store1_id, 20000.0, 0.0, 20000.0, 'PENDING', 'UNPAID', 'CASH', DATE_ADD(NOW(), INTERVAL 4 HOUR), NULL, NOW(), NOW()),

-- Order 5: CONFIRMED
(@buyer1_id, @store1_id, 95000.0, 15000.0, 80000.0, 'CONFIRMED', 'PAID', 'ZALOPAY', DATE_ADD(NOW(), INTERVAL 6 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW());

-- Orders cho Cà Phê Thức Mình (Store 2) - 4 orders
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, scheduled_pickup_time, note, created_at, updated_at)
VALUES
-- Order 6: COMPLETED
(@buyer2_id, @store2_id, 45000.0, 0.0, 45000.0, 'COMPLETED', 'PAID', 'MOMO', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Ít đá, nhiều sữa', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),

-- Order 7: COMPLETED
(@buyer3_id, @store2_id, 20000.0, 0.0, 20000.0, 'COMPLETED', 'PAID', 'CASH', DATE_SUB(NOW(), INTERVAL 12 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW()),

-- Order 8: PENDING
(@buyer4_id, @store2_id, 55000.0, 5000.0, 50000.0, 'PENDING', 'UNPAID', 'BANK_TRANSFER', DATE_ADD(NOW(), INTERVAL 3 HOUR), NULL, NOW(), NOW()),

-- Order 9: CANCELLED
(@buyer1_id, @store2_id, 25000.0, 0.0, 25000.0, 'CANCELLED', 'UNPAID', 'CASH', DATE_ADD(NOW(), INTERVAL 5 HOUR), 'Khách hủy vì đổi ý', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW());

-- Orders cho Phở Bò 3 Mươi (Store 3) - 4 orders
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, scheduled_pickup_time, note, created_at, updated_at)
VALUES
-- Order 10: COMPLETED
(@buyer3_id, @store3_id, 100000.0, 10000.0, 90000.0, 'COMPLETED', 'PAID', 'MOMO', DATE_SUB(NOW(), INTERVAL 4 DAY), 'Nhiều quẩy, chan nước', DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),

-- Order 11: READY_FOR_PICKUP
(@buyer4_id, @store3_id, 45000.0, 0.0, 45000.0, 'READY_FOR_PICKUP', 'PAID', 'CASH', DATE_ADD(NOW(), INTERVAL 1 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),

-- Order 12: PENDING
(@buyer1_id, @store3_id, 65000.0, 0.0, 65000.0, 'PENDING', 'UNPAID', 'ZALOPAY', DATE_ADD(NOW(), INTERVAL 5 HOUR), NULL, NOW(), NOW()),

-- Order 13: CONFIRMED
(@buyer2_id, @store3_id, 110000.0, 20000.0, 90000.0, 'CONFIRMED', 'PAID', 'BANK_TRANSFER', DATE_ADD(NOW(), INTERVAL 7 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW());

-- ============================================
-- BƯỚC 5: TẠO ORDER_ITEMS TEST
-- ============================================

-- Order items cho Order 1 (buyer1, store1) - COMPLETED
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
((SELECT order_id FROM orders ORDER BY order_id DESC LIMIT 1), @prod1_1, 1, 25000.0);

-- Lấy order IDs vừa tạo
SET @order1_id = (SELECT order_id FROM orders WHERE user_id=@buyer1_id AND store_id=@store1_id ORDER BY created_at DESC LIMIT 1 OFFSET 4);
SET @order2_id = (SELECT order_id FROM orders WHERE user_id=@buyer2_id AND store_id=@store1_id LIMIT 1);
SET @order3_id = (SELECT order_id FROM orders WHERE user_id=@buyer3_id AND store_id=@store1_id LIMIT 1);
SET @order4_id = (SELECT order_id FROM orders WHERE user_id=@buyer4_id AND store_id=@store1_id LIMIT 1);
SET @order5_id = (SELECT order_id FROM orders WHERE user_id=@buyer1_id AND store_id=@store1_id ORDER BY created_at DESC LIMIT 1);

-- Order 1: 2 bánh mì thịt nướng (giảm 5k)
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES (@order1_id, @prod1_1, 2, 25000.0);

-- Order 2: 1 chả lụa + 1 bi sườn
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
(@order2_id, @prod1_2, 1, 20000.0),
(@order2_id, @prod1_3, 1, 30000.0);

-- Order 3: 1 bi sườn (giảm 10k)
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES (@order3_id, @prod1_3, 1, 30000.0);

-- Order 4: 1 chả lụa
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES (@order4_id, @prod1_2, 1, 20000.0);

-- Order 5: 3 bánh mì mix (giảm 15k)
INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
(@order5_id, @prod1_1, 2, 25000.0),
(@order5_id, @prod1_2, 1, 20000.0);

-- Order items cho Store 2 (Cà Phê)
SET @order6_id = (SELECT order_id FROM orders WHERE store_id=@store2_id ORDER BY created_at ASC LIMIT 1 OFFSET 2);
SET @order7_id = (SELECT order_id FROM orders WHERE store_id=@store2_id ORDER BY created_at ASC LIMIT 1 OFFSET 3);
SET @order8_id = (SELECT order_id FROM orders WHERE store_id=@store2_id ORDER BY created_at ASC LIMIT 1 OFFSET 4);
SET @order9_id = (SELECT order_id FROM orders WHERE store_id=@store2_id ORDER BY created_at ASC LIMIT 1 OFFSET 5);

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
(@order6_id, @prod2_1, 1, 25000.0),
(@order6_id, @prod2_2, 1, 20000.0),
(@order7_id, @prod2_2, 1, 20000.0),
(@order8_id, @prod2_3, 1, 35000.0),
(@order8_id, @prod2_1, 1, 25000.0);

-- Order items cho Store 3 (Phở)
SET @order10_id = (SELECT order_id FROM orders WHERE store_id=@store3_id ORDER BY created_at ASC LIMIT 1 OFFSET 6);
SET @order11_id = (SELECT order_id FROM orders WHERE store_id=@store3_id ORDER BY created_at ASC LIMIT 1 OFFSET 7);
SET @order12_id = (SELECT order_id FROM orders WHERE store_id=@store3_id ORDER BY created_at ASC LIMIT 1 OFFSET 8);
SET @order13_id = (SELECT order_id FROM orders WHERE store_id=@store3_id ORDER BY created_at ASC LIMIT 1 OFFSET 9);

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES
(@order10_id, @prod3_2, 1, 55000.0),
(@order10_id, @prod3_1, 1, 45000.0),
(@order11_id, @prod3_1, 1, 45000.0),
(@order12_id, @prod3_3, 1, 65000.0),
(@order13_id, @prod3_2, 1, 55000.0),
(@order13_id, @prod3_3, 1, 65000.0);

-- ============================================
-- BƯỚC 6: VERIFY DỮ LIỆU ĐÃ INSERT
-- ============================================

SELECT '=== USERS ===' AS '';
SELECT user_id, username, email, full_name FROM users WHERE username IN ('admin_test', 'store_owner_1', 'store_owner_2', 'store_owner_3', 'buyer1', 'buyer2', 'buyer3', 'buyer4');

SELECT '=== STORES ===' AS '';
SELECT store_id, store_name, city, district, status FROM stores WHERE store_name IN ('Bánh Mì Như Ý', 'Cà Phê Thức Mình', 'Phở Bò 3 Mươi');

SELECT '=== ORDERS SUMMARY ===' AS '';
SELECT
    s.store_name,
    COUNT(o.order_id) as total_orders,
    SUM(CASE WHEN o.status = 'PENDING' THEN 1 ELSE 0 END) as pending,
    SUM(CASE WHEN o.status = 'CONFIRMED' THEN 1 ELSE 0 END) as confirmed,
    SUM(CASE WHEN o.status = 'READY_FOR_PICKUP' THEN 1 ELSE 0 END) as ready,
    SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled,
    SUM(CASE WHEN o.payment_status = 'PAID' THEN o.final_amount ELSE 0 END) as revenue
FROM stores s
LEFT JOIN orders o ON s.store_id = o.store_id
WHERE s.store_name IN ('Bánh Mì Như Ý', 'Cà Phê Thức Mình', 'Phở Bò 3 Mươi')
GROUP BY s.store_id, s.store_name
ORDER BY revenue DESC;

SELECT '=== ORDERS DETAILS ===' AS '';
SELECT
    o.order_id,
    s.store_name,
    u.full_name as customer,
    o.status,
    o.payment_status,
    o.final_amount,
    o.created_at
FROM orders o
JOIN stores s ON o.store_id = s.store_id
JOIN users u ON o.user_id = u.user_id
WHERE s.store_name IN ('Bánh Mì Như Ý', 'Cà Phê Thức Mình', 'Phở Bò 3 Mươi')
ORDER BY o.created_at DESC;

-- ============================================
-- XÓA DỮ LIỆU TEST (OPTIONAL)
-- ============================================

-- Nếu muốn xóa dữ liệu test, chạy các câu lệnh sau:

-- DELETE FROM order_items WHERE order_id IN (SELECT order_id FROM orders WHERE store_id IN (@store1_id, @store2_id, @store3_id));
-- DELETE FROM orders WHERE store_id IN (@store1_id, @store2_id, @store3_id);
-- DELETE FROM products WHERE store_id IN (@store1_id, @store2_id, @store3_id);
-- DELETE FROM stores WHERE store_id IN (@store1_id, @store2_id, @store3_id);
-- DELETE FROM users WHERE username IN ('admin_test', 'store_owner_1', 'store_owner_2', 'store_owner_3', 'buyer1', 'buyer2', 'buyer3', 'buyer4');
