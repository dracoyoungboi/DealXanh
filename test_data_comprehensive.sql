-- ============================================
-- DEALXANH - COMPREHENSIVE TEST DATA SCRIPT
-- Inject dữ liệu test cho TẤT CẢ chức năng Admin
-- ============================================
--
-- GIỮ NGUYÊN dữ liệu có sẵn từ backup:
--   Roles:    ID 1-4
--   Users:    ID 1-7 (1-4 store owners, 5 buyer, 6 admin, 7 moderator)
--   Stores:   ID 1-4 (1 active HN, 2 rejected, 3 active BN, 4 active HT)
--
-- Dùng MySQL variables bắt AUTO_INCREMENT ID sau mỗi INSERT
-- để đảm bảo FK tham chiếu đúng, không phụ thuộc vào giá trị ID cứng.
-- ============================================

-- ============================================
-- PHẦN 1: CATEGORIES
-- ============================================
INSERT INTO categories (name, description, icon_url) VALUES
('Đồ ăn mặn', 'Các món ăn chính: cơm, phở, bún, bánh mì...', '/img/categories/savory.png'),
('Đồ ngọt / Bánh', 'Bánh ngọt, chè, dessert...', '/img/categories/sweet.png'),
('Đồ uống / Cà phê', 'Cà phê, trà sữa, nước ép...', '/img/categories/drinks.png'),
('Thực phẩm đóng gói', 'Thực phẩm đóng gói sẵn, đồ khô...', '/img/categories/packaged.png'),
('Ăn vặt / Snack', 'Snack, xiên que, đồ ăn nhanh...', '/img/categories/snacks.png'),
('Cơm văn phòng', 'Cơm hộp, cơm văn phòng, suất ăn trưa...', '/img/categories/rice.png');

SET @cat_savory   = (SELECT category_id FROM categories WHERE name = 'Đồ ăn mặn'       LIMIT 1);
SET @cat_sweet    = (SELECT category_id FROM categories WHERE name = 'Đồ ngọt / Bánh'  LIMIT 1);
SET @cat_drinks   = (SELECT category_id FROM categories WHERE name = 'Đồ uống / Cà phê' LIMIT 1);
SET @cat_packaged = (SELECT category_id FROM categories WHERE name = 'Thực phẩm đóng gói' LIMIT 1);
SET @cat_snacks   = (SELECT category_id FROM categories WHERE name = 'Ăn vặt / Snack'  LIMIT 1);
SET @cat_rice     = (SELECT category_id FROM categories WHERE name = 'Cơm văn phòng'    LIMIT 1);

-- ============================================
-- PHẦN 2: USERS & STORES (reference từ backup)
-- ============================================
SET @buyer_id   = 5;  -- nguyenxuanphananh (ROLE_USER)
SET @admin_id   = 6;  -- admin (ROLE_ADMIN)
SET @store1_id  = 1;  -- abcbcbcbc (ACTIVE, Hà Nội)
SET @store3_id  = 3;  -- bcbc (ACTIVE, Bắc Ninh)
SET @store4_id  = 4;  -- Tiệm bánh nhà làm (ACTIVE, Hà Tĩnh)

-- ============================================
-- PHẦN 3: PRODUCTS
-- ============================================

-- --- Store 1: 8 sản phẩm ---
INSERT INTO products (name, description, original_price, image_url, stock_quantity, product_type, active, deleted, approval_status, category_id, store_id, created_at, updated_at) VALUES
('Phở Bò Tái Chín',   'Phở bò tái chín nước dùng xương hầm 12h',   50000, '/img/products/pho-bo-tai.jpg',    50, 'FOOD',  b'1', b'0', 'APPROVED', @cat_savory, @store1_id, '2026-05-01 08:00:00', '2026-05-01 08:00:00'),
('Bún Chả Hà Nội',    'Bún chả nướng than hoa chuẩn vị HN',        45000, '/img/products/bun-cha.jpg',       40, 'FOOD',  b'1', b'0', 'APPROVED', @cat_savory, @store1_id, '2026-05-01 08:00:00', '2026-05-01 08:00:00'),
('Bánh Mì Thịt Nướng','Bánh mì thịt nướng que tre đặc biệt',        25000, '/img/products/banhmi-thit-nuong.jpg',100,'FOOD',  b'1', b'0', 'APPROVED', @cat_savory, @store1_id, '2026-05-02 08:00:00', '2026-05-02 08:00:00'),
('Bánh Mì Chả Lụa',   'Bánh mì chả lụa truyền thống',              20000, '/img/products/banhmi-cha-lua.jpg', 80, 'FOOD',  b'1', b'0', 'APPROVED', @cat_savory, @store1_id, '2026-05-02 08:00:00', '2026-05-02 08:00:00'),
('Cơm Sườn Nướng',    'Cơm sườn nướng mật ong + canh + rau',       40000, '/img/products/com-suon.jpg',       30, 'FOOD',  b'1', b'0', 'APPROVED', @cat_rice,   @store1_id, '2026-05-05 08:00:00', '2026-05-05 08:00:00'),
('Trà Chanh Tươi',    'Trà chanh tươi mát lạnh',                    15000, '/img/products/tra-chanh.jpg',     200, 'DRINK', b'1', b'0', 'APPROVED', @cat_drinks, @store1_id, '2026-05-05 08:00:00', '2026-05-05 08:00:00'),
('Bánh Flan Caramen', 'Bánh flan caramen béo ngậy',                 12000, '/img/products/flan.jpg',           60, 'FOOD',  b'1', b'0', 'PENDING',  @cat_sweet,  @store1_id, '2026-05-15 08:00:00', '2026-05-15 08:00:00'),
('Phở Cuốn Thịt Bò',  'Phở cuốn thịt bò sốt me chua ngọt',          55000, '/img/products/pho-cuon.jpg',       25, 'FOOD',  b'0', b'0', 'REJECTED', @cat_savory, @store1_id, '2026-05-14 08:00:00', '2026-05-14 08:00:00');

-- Capture product IDs cho Store 1
SET @p1_pho_bo      = (SELECT product_id FROM products WHERE name = 'Phở Bò Tái Chín'    AND store_id = @store1_id LIMIT 1);
SET @p1_bun_cha     = (SELECT product_id FROM products WHERE name = 'Bún Chả Hà Nội'     AND store_id = @store1_id LIMIT 1);
SET @p1_banhmi      = (SELECT product_id FROM products WHERE name = 'Bánh Mì Thịt Nướng' AND store_id = @store1_id LIMIT 1);
SET @p1_chalua      = (SELECT product_id FROM products WHERE name = 'Bánh Mì Chả Lụa'    AND store_id = @store1_id LIMIT 1);
SET @p1_com_suon    = (SELECT product_id FROM products WHERE name = 'Cơm Sườn Nướng'     AND store_id = @store1_id LIMIT 1);
SET @p1_tra_chanh   = (SELECT product_id FROM products WHERE name = 'Trà Chanh Tươi'     AND store_id = @store1_id LIMIT 1);

-- --- Store 3: 6 sản phẩm ---
INSERT INTO products (name, description, original_price, image_url, stock_quantity, product_type, active, deleted, approval_status, category_id, store_id, created_at, updated_at) VALUES
('Bánh Đa Cua',       'Bánh đa cua đồng chuẩn Hải Phòng',        35000, '/img/products/banh-da-cua.jpg',   45, 'FOOD',  b'1', b'0', 'APPROVED', @cat_savory, @store3_id, '2026-05-03 08:00:00', '2026-05-03 08:00:00'),
('Nem Chua Rán',      'Nem chua rán chấm tương ớt',              20000, '/img/products/nem-chua-ran.jpg',  70, 'SNACK', b'1', b'0', 'APPROVED', @cat_snacks, @store3_id, '2026-05-03 08:00:00', '2026-05-03 08:00:00'),
('Chè Thái',          'Chè Thái thập cẩm đầy đủ topping',         25000, '/img/products/che-thai.jpg',       50, 'FOOD',  b'1', b'0', 'APPROVED', @cat_sweet,  @store3_id, '2026-05-04 08:00:00', '2026-05-04 08:00:00'),
('Trà Sữa Trân Châu', 'Trà sữa trân châu đen đường nâu',          30000, '/img/products/tra-sua.jpg',        80, 'DRINK', b'1', b'0', 'APPROVED', @cat_drinks, @store3_id, '2026-05-04 08:00:00', '2026-05-04 08:00:00'),
('Xoài Lắc',          'Xoài lắc muối ớt chua cay',                15000, '/img/products/xoai-lac.jpg',       40, 'SNACK', b'1', b'0', 'PENDING',  @cat_snacks, @store3_id, '2026-05-15 08:00:00', '2026-05-15 08:00:00'),
('Cơm Tấm Sườn',      'Cơm tấm sườn bì chả trứng',               40000, '/img/products/com-tam.jpg',        30, 'FOOD',  b'1', b'0', 'APPROVED', @cat_rice,   @store3_id, '2026-05-05 08:00:00', '2026-05-05 08:00:00');

SET @p3_banh_da  = (SELECT product_id FROM products WHERE name = 'Bánh Đa Cua'        AND store_id = @store3_id LIMIT 1);
SET @p3_nem_chua = (SELECT product_id FROM products WHERE name = 'Nem Chua Rán'       AND store_id = @store3_id LIMIT 1);
SET @p3_che_thai = (SELECT product_id FROM products WHERE name = 'Chè Thái'           AND store_id = @store3_id LIMIT 1);
SET @p3_tra_sua  = (SELECT product_id FROM products WHERE name = 'Trà Sữa Trân Châu'  AND store_id = @store3_id LIMIT 1);
SET @p3_com_tam  = (SELECT product_id FROM products WHERE name = 'Cơm Tấm Sườn'       AND store_id = @store3_id LIMIT 1);

-- --- Store 4: 6 sản phẩm ---
INSERT INTO products (name, description, original_price, image_url, stock_quantity, product_type, active, deleted, approval_status, category_id, store_id, created_at, updated_at) VALUES
('Bánh Dứa Đài Loan',        'Bánh dứa Đài Loan nhân dứa thật',            35000, '/img/products/banh-dua.jpg',            30, 'FOOD', b'1', b'0', 'APPROVED', @cat_sweet,    @store4_id, '2026-05-06 08:00:00', '2026-05-06 08:00:00'),
('Bánh Bông Lan Trứng Muối', 'Bánh bông lan sốt trứng muối béo ngậy',       45000, '/img/products/bonglan-trungmuoi.jpg',    25, 'FOOD', b'1', b'0', 'APPROVED', @cat_sweet,    @store4_id, '2026-05-06 08:00:00', '2026-05-06 08:00:00'),
('Bánh Quy Bơ',              'Bánh quy bơ hộp quà tặng 200g',                60000, '/img/products/banh-quy.jpg',             20, 'FOOD', b'1', b'0', 'APPROVED', @cat_packaged, @store4_id, '2026-05-07 08:00:00', '2026-05-07 08:00:00'),
('Kẹo Dẻo Trái Cây',         'Kẹo dẻo làm từ nước ép trái cây tươi',         25000, '/img/products/keo-deo.jpg',              50, 'FOOD', b'1', b'0', 'APPROVED', @cat_packaged, @store4_id, '2026-05-07 08:00:00', '2026-05-07 08:00:00'),
('Bánh Mochi Nhật',          'Bánh mochi nhân kem đậu đỏ matcha',            40000, '/img/products/mochi.jpg',                15, 'FOOD', b'1', b'0', 'PENDING',  @cat_sweet,    @store4_id, '2026-05-16 08:00:00', '2026-05-16 08:00:00'),
('Bánh Mì Hoa Cúc',          'Bánh mì hoa cúc sữa tươi thơm mềm',            22000, '/img/products/banhmi-hoacuc.jpg',        35, 'FOOD', b'1', b'0', 'REJECTED', @cat_sweet,    @store4_id, '2026-05-13 08:00:00', '2026-05-13 08:00:00');

SET @p4_banh_dua   = (SELECT product_id FROM products WHERE name = 'Bánh Dứa Đài Loan'         AND store_id = @store4_id LIMIT 1);
SET @p4_bong_lan   = (SELECT product_id FROM products WHERE name = 'Bánh Bông Lan Trứng Muối'  AND store_id = @store4_id LIMIT 1);
SET @p4_banh_quy   = (SELECT product_id FROM products WHERE name = 'Bánh Quy Bơ'               AND store_id = @store4_id LIMIT 1);
SET @p4_keo_deo    = (SELECT product_id FROM products WHERE name = 'Kẹo Dẻo Trái Cây'          AND store_id = @store4_id LIMIT 1);
SET @p4_mochi      = (SELECT product_id FROM products WHERE name = 'Bánh Mochi Nhật'           AND store_id = @store4_id LIMIT 1);

-- ============================================
-- PHẦN 4: DEALS
-- ============================================

-- Deal D1: Flash Sale 40% - Store 1 (ACTIVE)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Flash Sale 40% Phở & Bún', 'FLASH40_STORE1', 'FLASH_SALE', 'Giảm 40% cho các món Phở và Bún tại Store 1', 'PERCENT', 40, 40000, 50000, 100, 35, 3, '2026-05-14 00:00:00', '2026-05-20 23:59:59', 'ACTIVE', 'SPECIFIC_STORES', 'CODE_REQUIRED', '/img/deals/flash40-pho.jpg', '/uploads/deal-banner-1.png', 100, NOW(), NOW(), @store1_id, @admin_id);
SET @d1_id = LAST_INSERT_ID();

-- Deal D2: Voucher 20K - Platform (ACTIVE, ALL_STORES)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Voucher 20K Mừng Khai Trương', 'VOUCHER20K', 'VOUCHER', 'Giảm 20,000đ cho đơn từ 100,000đ', 'FIXED', 20000, NULL, 100000, 500, 185, 2, '2026-05-10 00:00:00', '2026-06-10 23:59:59', 'ACTIVE', 'ALL_STORES', 'CODE_REQUIRED', '/img/deals/voucher20k.jpg', '/uploads/deal-banner-2.png', 80, NOW(), NOW(), NULL, @admin_id);
SET @d2_id = LAST_INSERT_ID();

-- Deal D3: Combo Cơm + Trà - Store 3 (ACTIVE)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Combo Cơm Trà Siêu Rẻ', 'COMBO_COMTRA', 'COMBO', 'Combo 1 phần cơm + 1 trà sữa giảm 15,000đ', 'FIXED', 15000, NULL, 50000, 50, 12, 1, '2026-05-12 00:00:00', '2026-05-22 23:59:59', 'ACTIVE', 'SPECIFIC_STORES', 'CODE_REQUIRED', '/img/deals/combo-comtra.jpg', '/uploads/deal-banner-3.png', 70, NOW(), NOW(), @store3_id, @admin_id);
SET @d3_id = LAST_INSERT_ID();

-- Deal D4: Seasonal Sale 25% - Platform (SCHEDULED, AUTO_APPLY)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Sale Hè 2026 - Giảm 25%', 'SALEHE2026', 'SEASONAL', 'Chào hè 2026 - Giảm tự động 25% đồ uống & ăn vặt', 'PERCENT', 25, 50000, 50000, NULL, 0, NULL, '2026-05-18 00:00:00', '2026-06-15 23:59:59', 'SCHEDULED', 'ALL_STORES', 'AUTO_APPLY', '/img/deals/sale-he-2026.jpg', '/uploads/deal-banner-4.png', 90, NOW(), NOW(), NULL, @admin_id);
SET @d4_id = LAST_INSERT_ID();

-- Deal D5: Flash Sale 50% Bánh - Store 4 (ACTIVE, AUTO_APPLY, 6h)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Flash Sale 50% Bánh Ngọt', 'FLASH50_BANH', 'FLASH_SALE', 'Giảm 50% cho tất cả bánh ngọt - giới hạn 6h', 'PERCENT', 50, 30000, 30000, 80, 28, 2, '2026-05-16 08:00:00', '2026-05-16 14:00:00', 'ACTIVE', 'SPECIFIC_STORES', 'AUTO_APPLY', '/img/deals/flash50-banh.jpg', '/uploads/deal-banner-5.png', 95, NOW(), NOW(), @store4_id, @admin_id);
SET @d5_id = LAST_INSERT_ID();

-- Deal D6: Voucher 15% New User - Platform (ACTIVE)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Voucher 15% Cho Người Mới', 'NEWUSER15', 'VOUCHER', 'Giảm 15% tối đa 30k cho người dùng mới', 'PERCENT', 15, 30000, 50000, 1000, 320, 1, '2026-05-01 00:00:00', '2026-07-01 23:59:59', 'ACTIVE', 'ALL_STORES', 'CODE_REQUIRED', '/img/deals/newuser15.jpg', '/uploads/deal-banner-6.png', 60, NOW(), NOW(), NULL, @admin_id);
SET @d6_id = LAST_INSERT_ID();

-- Deal D7: Combo Bánh + Trà - Store 4 (SCHEDULED)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Combo Bánh Trà Chiều', 'COMBO_BANHTRA', 'COMBO', 'Combo 1 bánh + 1 trà chiều giảm 10,000đ', 'FIXED', 10000, NULL, 40000, 30, 0, NULL, '2026-05-20 00:00:00', '2026-05-30 23:59:59', 'SCHEDULED', 'SPECIFIC_STORES', 'CODE_REQUIRED', '/img/deals/combo-banhtra.jpg', '/uploads/deal-banner-7.png', 65, NOW(), NOW(), @store4_id, @admin_id);
SET @d7_id = LAST_INSERT_ID();

-- Deal D8: Seasonal Tết Đoan Ngọ - Platform (SCHEDULED, AUTO_APPLY)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Tết Đoan Ngọ - Giảm 20%', 'TETDOAN_NGO', 'SEASONAL', 'Mừng Tết Đoan Ngọ giảm 20% toàn bộ bánh kẹo', 'PERCENT', 20, 40000, 100000, NULL, 0, NULL, '2026-05-25 00:00:00', '2026-06-05 23:59:59', 'SCHEDULED', 'ALL_STORES', 'AUTO_APPLY', '/img/deals/tet-doan-ngo.jpg', '/uploads/deal-banner-8.png', 75, NOW(), NOW(), NULL, @admin_id);
SET @d8_id = LAST_INSERT_ID();

-- Deal D9: Flash Sale 30% Cơm - Store 1 (ENDED)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Flash Sale Cơm Trưa 30%', 'FLASH30_COM', 'FLASH_SALE', 'Giảm 30% các suất cơm trưa - đã kết thúc', 'PERCENT', 30, 20000, 30000, 60, 60, 2, '2026-05-01 10:00:00', '2026-05-01 17:00:00', 'ENDED', 'SPECIFIC_STORES', 'AUTO_APPLY', '/img/deals/flash30-com.jpg', '/uploads/deal-banner-9.png', 50, NOW(), NOW(), @store1_id, @admin_id);
SET @d9_id = LAST_INSERT_ID();

-- Deal D10: Voucher 50K đơn lớn - Platform (ACTIVE)
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by) VALUES
('Voucher 50K Đơn Lớn', 'VOUCHER50K_BIG', 'VOUCHER', 'Giảm 50,000đ cho đơn từ 300,000đ', 'FIXED', 50000, NULL, 300000, 200, 47, 3, '2026-05-08 00:00:00', '2026-06-08 23:59:59', 'ACTIVE', 'ALL_STORES', 'CODE_REQUIRED', '/img/deals/voucher50k.jpg', '/uploads/deal-banner-10.png', 85, NOW(), NOW(), NULL, @admin_id);
SET @d10_id = LAST_INSERT_ID();

-- ============================================
-- PHẦN 5: DEAL CATEGORIES (Platform deals)
-- ============================================

-- D2 (VOUCHER20K): Đồ ăn mặn, Cơm văn phòng, Đồ uống
INSERT INTO deal_categories (deal_id, category_id, priority, active, created_at) VALUES
(@d2_id, @cat_savory, 1, TRUE, NOW()),
(@d2_id, @cat_rice,   2, TRUE, NOW()),
(@d2_id, @cat_drinks, 3, TRUE, NOW());

-- D4 (SALEHE2026): Đồ uống, Ăn vặt
INSERT INTO deal_categories (deal_id, category_id, priority, active, created_at) VALUES
(@d4_id, @cat_drinks, 1, TRUE, NOW()),
(@d4_id, @cat_snacks, 2, TRUE, NOW());

-- D6 (NEWUSER15): gần hết categories
INSERT INTO deal_categories (deal_id, category_id, priority, active, created_at) VALUES
(@d6_id, @cat_savory,   1, TRUE, NOW()),
(@d6_id, @cat_sweet,    2, TRUE, NOW()),
(@d6_id, @cat_drinks,   3, TRUE, NOW()),
(@d6_id, @cat_snacks,   4, TRUE, NOW()),
(@d6_id, @cat_rice,     5, TRUE, NOW());

-- D8 (TETDOAN_NGO): Đồ ngọt/Bánh, Thực phẩm đóng gói
INSERT INTO deal_categories (deal_id, category_id, priority, active, created_at) VALUES
(@d8_id, @cat_sweet,    1, TRUE, NOW()),
(@d8_id, @cat_packaged, 2, TRUE, NOW());

-- D10 (VOUCHER50K_BIG): các danh mục chính
INSERT INTO deal_categories (deal_id, category_id, priority, active, created_at) VALUES
(@d10_id, @cat_savory, 1, TRUE, NOW()),
(@d10_id, @cat_sweet,  2, TRUE, NOW()),
(@d10_id, @cat_drinks, 3, TRUE, NOW()),
(@d10_id, @cat_rice,   4, TRUE, NOW());

-- ============================================
-- PHẦN 6: DEAL PRODUCTS (Store deals)
-- ============================================

-- D1 (FLASH40_STORE1): Phở Bò Tái Chín + Bún Chả HN
INSERT INTO deal_products (deal_id, product_id, original_price, sale_price, max_quantity, sold_quantity, priority, created_at) VALUES
(@d1_id, @p1_pho_bo,  50000, 30000, 30, 18, 1, NOW()),
(@d1_id, @p1_bun_cha, 45000, 27000, 25, 12, 2, NOW());

-- D3 (COMBO_COMTRA): Cơm Tấm Sườn + Trà Sữa
INSERT INTO deal_products (deal_id, product_id, original_price, sale_price, max_quantity, sold_quantity, priority, created_at) VALUES
(@d3_id, @p3_com_tam, 40000, 28000, 20, 8, 1, NOW()),
(@d3_id, @p3_tra_sua, 30000, 25000, 30, 4, 2, NOW());

-- D5 (FLASH50_BANH): Bánh Dứa + Bánh Bông Lan + Bánh Quy
INSERT INTO deal_products (deal_id, product_id, original_price, sale_price, max_quantity, sold_quantity, priority, created_at) VALUES
(@d5_id, @p4_banh_dua, 35000, 17500, 15, 15, 1, NOW()),
(@d5_id, @p4_bong_lan, 45000, 22500, 10, 10, 2, NOW()),
(@d5_id, @p4_banh_quy, 60000, 30000, 10,  3, 3, NOW());

-- D7 (COMBO_BANHTRA): Bánh Quy + Kẹo Dẻo
INSERT INTO deal_products (deal_id, product_id, original_price, sale_price, max_quantity, sold_quantity, priority, created_at) VALUES
(@d7_id, @p4_banh_quy, 60000, 50000, 15, 0, 1, NOW()),
(@d7_id, @p4_keo_deo,  25000, 20000, 20, 0, 2, NOW());

-- D9 (FLASH30_COM - ENDED): Cơm Sườn Nướng
INSERT INTO deal_products (deal_id, product_id, original_price, sale_price, max_quantity, sold_quantity, priority, created_at) VALUES
(@d9_id, @p1_com_suon, 40000, 28000, 30, 30, 1, '2026-05-01 10:00:00');

-- ============================================
-- PHẦN 7: ORDERS
-- ============================================

-- --- Store 1: 6 orders ---
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, coupon_code, scheduled_pickup_time, actual_pickup_time, note, created_at, updated_at) VALUES
(@buyer_id, @store1_id,  95000, 20000, 75000, 'COMPLETED',        'PAID',   'MOMO',          'VOUCHER20K',     '2026-05-14 12:00:00', '2026-05-14 12:15:00', 'Ít hành, nhiều rau', '2026-05-14 10:30:00', '2026-05-14 12:15:00'),
(@buyer_id, @store1_id,  50000, 20000, 30000, 'COMPLETED',        'PAID',   'CASH',          'FLASH40_STORE1', '2026-05-15 11:30:00', '2026-05-15 11:45:00', 'Thêm tương ớt',      '2026-05-15 09:00:00', '2026-05-15 11:45:00'),
(@buyer_id, @store1_id,  45000,     0, 45000, 'READY_FOR_PICKUP', 'PAID',   'ZALOPAY',       NULL,             '2026-05-16 12:30:00', NULL,                   'Gọi trước 5p',       '2026-05-16 10:00:00', '2026-05-16 11:00:00'),
(@buyer_id, @store1_id,  25000,     0, 25000, 'PENDING',          'UNPAID', 'CASH',          NULL,             '2026-05-16 13:00:00', NULL,                   NULL,                 '2026-05-16 11:30:00', '2026-05-16 11:30:00'),
(@buyer_id, @store1_id, 105000, 30000, 75000, 'CONFIRMED',        'PAID',   'BANK_TRANSFER', 'VOUCHER20K',     '2026-05-16 14:00:00', NULL,                   'Đóng gói kỹ',        '2026-05-16 08:00:00', '2026-05-16 08:30:00'),
(@buyer_id, @store1_id,  20000,     0, 20000, 'CANCELLED',        'UNPAID', 'CASH',          NULL,             '2026-05-16 13:00:00', NULL,                   'Khách đổi ý',        '2026-05-16 11:00:00', '2026-05-16 11:15:00');

-- Capture Store 1 order IDs (lấy 6 dòng cuối cùng theo created_at)
SET @o1_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id ORDER BY created_at ASC LIMIT 1 OFFSET 0);
SET @o2_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id ORDER BY created_at ASC LIMIT 1 OFFSET 1);
SET @o3_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id ORDER BY created_at ASC LIMIT 1 OFFSET 2);
SET @o4_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id ORDER BY created_at ASC LIMIT 1 OFFSET 3);
SET @o5_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id ORDER BY created_at ASC LIMIT 1 OFFSET 4);
SET @o6_id  = (SELECT order_id FROM orders WHERE store_id = @store1_id ORDER BY created_at ASC LIMIT 1 OFFSET 5);

-- --- Store 3: 5 orders ---
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, coupon_code, scheduled_pickup_time, actual_pickup_time, note, created_at, updated_at) VALUES
(@buyer_id, @store3_id, 65000, 15000, 50000, 'COMPLETED',        'PAID',   'MOMO',          'COMBO_COMTRA', '2026-05-14 12:00:00', '2026-05-14 12:20:00', 'Ngon lắm',          '2026-05-14 10:00:00', '2026-05-14 12:20:00'),
(@buyer_id, @store3_id, 25000,     0, 25000, 'COMPLETED',        'PAID',   'CASH',          NULL,           '2026-05-15 11:00:00', '2026-05-15 11:05:00', NULL,                '2026-05-15 09:30:00', '2026-05-15 11:05:00'),
(@buyer_id, @store3_id, 55000,     0, 55000, 'READY_FOR_PICKUP', 'PAID',   'ZALOPAY',       NULL,           '2026-05-16 12:00:00', NULL,                   'Chan nhiều nước',   '2026-05-16 09:00:00', '2026-05-16 10:30:00'),
(@buyer_id, @store3_id, 20000,     0, 20000, 'PENDING',          'UNPAID', 'CASH',          NULL,           '2026-05-16 13:30:00', NULL,                   NULL,                '2026-05-16 12:00:00', '2026-05-16 12:00:00'),
(@buyer_id, @store3_id, 40000,     0, 40000, 'CANCELLED',        'UNPAID', 'BANK_TRANSFER', NULL,           '2026-05-16 14:00:00', NULL,                   'Hủy đơn do đổi món','2026-05-16 11:00:00', '2026-05-16 11:30:00');

SET @o7_id  = (SELECT order_id FROM orders WHERE store_id = @store3_id ORDER BY created_at ASC LIMIT 1 OFFSET 0);
SET @o8_id  = (SELECT order_id FROM orders WHERE store_id = @store3_id ORDER BY created_at ASC LIMIT 1 OFFSET 1);
SET @o9_id  = (SELECT order_id FROM orders WHERE store_id = @store3_id ORDER BY created_at ASC LIMIT 1 OFFSET 2);
SET @o10_id = (SELECT order_id FROM orders WHERE store_id = @store3_id ORDER BY created_at ASC LIMIT 1 OFFSET 3);
SET @o11_id = (SELECT order_id FROM orders WHERE store_id = @store3_id ORDER BY created_at ASC LIMIT 1 OFFSET 4);

-- --- Store 4: 5 orders ---
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, coupon_code, scheduled_pickup_time, actual_pickup_time, note, created_at, updated_at) VALUES
(@buyer_id, @store4_id, 80000, 40000, 40000, 'COMPLETED',        'PAID',   'MOMO',    'FLASH50_BANH', '2026-05-16 10:00:00', '2026-05-16 10:10:00', 'Bánh ngon xuất sắc',  '2026-05-16 08:00:00', '2026-05-16 10:10:00'),
(@buyer_id, @store4_id, 45000,  5000, 40000, 'COMPLETED',        'PAID',   'CASH',    'NEWUSER15',    '2026-05-15 15:00:00', '2026-05-15 15:20:00', NULL,                  '2026-05-15 13:00:00', '2026-05-15 15:20:00'),
(@buyer_id, @store4_id, 60000,     0, 60000, 'CONFIRMED',        'PAID',   'ZALOPAY', NULL,           '2026-05-16 16:00:00', NULL,                   'Gói quà tặng',        '2026-05-16 13:00:00', '2026-05-16 13:30:00'),
(@buyer_id, @store4_id, 25000,     0, 25000, 'PENDING',          'UNPAID', 'CASH',    NULL,           '2026-05-16 17:00:00', NULL,                   NULL,                  '2026-05-16 14:00:00', '2026-05-16 14:00:00'),
(@buyer_id, @store4_id, 40000,     0, 40000, 'CANCELLED',        'UNPAID', 'CASH',    NULL,           '2026-05-16 16:00:00', NULL,                   'Hết hàng - shop hủy', '2026-05-16 12:00:00', '2026-05-16 12:30:00');

SET @o12_id = (SELECT order_id FROM orders WHERE store_id = @store4_id ORDER BY created_at ASC LIMIT 1 OFFSET 0);
SET @o13_id = (SELECT order_id FROM orders WHERE store_id = @store4_id ORDER BY created_at ASC LIMIT 1 OFFSET 1);
SET @o14_id = (SELECT order_id FROM orders WHERE store_id = @store4_id ORDER BY created_at ASC LIMIT 1 OFFSET 2);
SET @o15_id = (SELECT order_id FROM orders WHERE store_id = @store4_id ORDER BY created_at ASC LIMIT 1 OFFSET 3);
SET @o16_id = (SELECT order_id FROM orders WHERE store_id = @store4_id ORDER BY created_at ASC LIMIT 1 OFFSET 4);

-- --- Past days: 6 COMPLETED orders (7-day chart data) ---
INSERT INTO orders (user_id, store_id, total_amount, discount_amount, final_amount, status, payment_status, payment_method, coupon_code, scheduled_pickup_time, actual_pickup_time, note, created_at, updated_at) VALUES
(@buyer_id, @store1_id,  25000,     0, 25000, 'COMPLETED', 'PAID', 'CASH',          NULL,             '2026-05-10 12:00:00', '2026-05-10 12:15:00', NULL,           '2026-05-10 10:00:00', '2026-05-10 12:15:00'),
(@buyer_id, @store3_id,  50000, 10000, 40000, 'COMPLETED', 'PAID', 'MOMO',          'VOUCHER20K',     '2026-05-11 12:00:00', '2026-05-11 12:10:00', NULL,           '2026-05-11 10:00:00', '2026-05-11 12:10:00'),
(@buyer_id, @store4_id,  80000, 20000, 60000, 'COMPLETED', 'PAID', 'ZALOPAY',       'VOUCHER50K_BIG', '2026-05-12 12:00:00', '2026-05-12 12:20:00', NULL,           '2026-05-12 09:00:00', '2026-05-12 12:20:00'),
(@buyer_id, @store1_id, 100000, 40000, 60000, 'COMPLETED', 'PAID', 'BANK_TRANSFER', 'FLASH40_STORE1', '2026-05-13 12:00:00', '2026-05-13 12:30:00', 'Nhiều thịt',   '2026-05-13 10:00:00', '2026-05-13 12:30:00'),
(@buyer_id, @store3_id,  35000,     0, 35000, 'COMPLETED', 'PAID', 'CASH',          NULL,             '2026-05-14 12:00:00', '2026-05-14 12:05:00', NULL,           '2026-05-14 11:00:00', '2026-05-14 12:05:00'),
(@buyer_id, @store4_id,  70000, 10000, 60000, 'COMPLETED', 'PAID', 'MOMO',          'NEWUSER15',      '2026-05-15 12:00:00', '2026-05-15 12:15:00', NULL,           '2026-05-15 10:30:00', '2026-05-15 12:15:00');

-- Lấy các order past-days theo store + thời gian
SET @o17_id = (SELECT order_id FROM orders WHERE store_id = @store1_id AND DATE(created_at) = '2026-05-10' LIMIT 1);
SET @o18_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND DATE(created_at) = '2026-05-11' LIMIT 1);
SET @o19_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND DATE(created_at) = '2026-05-12' LIMIT 1);
SET @o20_id = (SELECT order_id FROM orders WHERE store_id = @store1_id AND DATE(created_at) = '2026-05-13' LIMIT 1);
SET @o21_id = (SELECT order_id FROM orders WHERE store_id = @store3_id AND DATE(created_at) = '2026-05-14' AND status = 'COMPLETED' ORDER BY created_at DESC LIMIT 1);
SET @o22_id = (SELECT order_id FROM orders WHERE store_id = @store4_id AND DATE(created_at) = '2026-05-15' AND status = 'COMPLETED' ORDER BY created_at DESC LIMIT 1);

-- ============================================
-- PHẦN 8: ORDER ITEMS
-- ============================================

-- Store 1 orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(@o1_id,  @p1_pho_bo,   1, 50000), (@o1_id,  @p1_bun_cha,  1, 45000),
(@o2_id,  @p1_pho_bo,   1, 50000),
(@o3_id,  @p1_bun_cha,  1, 45000),
(@o4_id,  @p1_banhmi,   1, 25000),
(@o5_id,  @p1_pho_bo,   1, 50000), (@o5_id,  @p1_bun_cha,  1, 45000), (@o5_id, @p1_tra_chanh, 1, 15000),
(@o6_id,  @p1_chalua,   1, 20000);

-- Store 3 orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(@o7_id,  @p3_com_tam,  1, 40000), (@o7_id,  @p3_tra_sua,  1, 30000),
(@o8_id,  @p3_che_thai, 1, 25000),
(@o9_id,  @p3_banh_da,  1, 35000), (@o9_id,  @p3_nem_chua, 1, 20000),
(@o10_id, @p3_nem_chua, 1, 20000),
(@o11_id, @p3_com_tam,  1, 40000);

-- Store 4 orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(@o12_id, @p4_banh_dua, 1, 35000), (@o12_id, @p4_bong_lan, 1, 45000),
(@o13_id, @p4_bong_lan, 1, 45000),
(@o14_id, @p4_banh_quy, 1, 60000),
(@o15_id, @p4_keo_deo,  1, 25000),
(@o16_id, @p4_mochi,    1, 40000);

-- Past day orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(@o17_id, @p1_banhmi,   1, 25000),
(@o18_id, @p3_com_tam,  1, 40000), (@o18_id, @p3_nem_chua, 1, 20000),
(@o19_id, @p4_banh_quy, 1, 60000),
(@o20_id, @p1_pho_bo,   2, 50000),
(@o21_id, @p3_banh_da,  1, 35000),
(@o22_id, @p4_bong_lan, 1, 45000);

-- ============================================
-- PHẦN 9: TRANSACTIONS (SALE + PAYOUT)
-- ============================================

-- Store 1 SALE transactions
INSERT INTO transactions (store_id, order_id, amount, platform_fee, net_amount, type, status, description, created_at, updated_at) VALUES
(@store1_id, @o1_id,  75000, 7500, 67500, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-14 12:15:00', '2026-05-14 12:15:00'),
(@store1_id, @o2_id,  30000, 3000, 27000, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-15 11:45:00', '2026-05-15 11:45:00'),
(@store1_id, @o17_id, 25000, 2500, 22500, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-10 12:15:00', '2026-05-10 12:15:00'),
(@store1_id, @o20_id, 60000, 6000, 54000, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-13 12:30:00', '2026-05-13 12:30:00');

-- Store 3 SALE transactions
INSERT INTO transactions (store_id, order_id, amount, platform_fee, net_amount, type, status, description, created_at, updated_at) VALUES
(@store3_id, @o7_id,  50000, 5000, 45000, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-14 12:20:00', '2026-05-14 12:20:00'),
(@store3_id, @o8_id,  25000, 2500, 22500, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-15 11:05:00', '2026-05-15 11:05:00'),
(@store3_id, @o18_id, 40000, 4000, 36000, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-11 12:10:00', '2026-05-11 12:10:00'),
(@store3_id, @o21_id, 35000, 3500, 31500, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-14 12:05:00', '2026-05-14 12:05:00');

-- Store 4 SALE transactions
INSERT INTO transactions (store_id, order_id, amount, platform_fee, net_amount, type, status, description, created_at, updated_at) VALUES
(@store4_id, @o12_id, 40000, 4000, 36000, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-16 10:10:00', '2026-05-16 10:10:00'),
(@store4_id, @o13_id, 40000, 4000, 36000, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-15 15:20:00', '2026-05-15 15:20:00'),
(@store4_id, @o19_id, 60000, 6000, 54000, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-12 12:20:00', '2026-05-12 12:20:00'),
(@store4_id, @o22_id, 60000, 6000, 54000, 'SALE', 'COMPLETED', 'Doanh thu đơn hàng', '2026-05-15 12:15:00', '2026-05-15 12:15:00');

-- PAYOUT: Pending (chưa thanh toán)
INSERT INTO transactions (store_id, order_id, amount, platform_fee, net_amount, type, status, description, created_at, updated_at) VALUES
(@store1_id, NULL, 67500, NULL, 67500, 'PAYOUT', 'PENDING', 'Thanh toán kỳ 14/05 - Store 1', '2026-05-14 12:30:00', '2026-05-14 12:30:00'),
(@store3_id, NULL, 45000, NULL, 45000, 'PAYOUT', 'PENDING', 'Thanh toán kỳ 14/05 - Store 3', '2026-05-14 12:30:00', '2026-05-14 12:30:00'),
(@store4_id, NULL, 36000, NULL, 36000, 'PAYOUT', 'PENDING', 'Thanh toán kỳ 16/05 - Store 4', '2026-05-16 10:30:00', '2026-05-16 10:30:00');

-- PAYOUT: Completed (đã thanh toán)
INSERT INTO transactions (store_id, order_id, amount, platform_fee, net_amount, type, status, description, created_at, updated_at) VALUES
(@store1_id, NULL, 22500, NULL, 22500, 'PAYOUT', 'COMPLETED', 'Thanh toán kỳ 10/05 - Store 1 (đã trả)', '2026-05-10 12:30:00', '2026-05-10 18:00:00'),
(@store3_id, NULL, 36000, NULL, 36000, 'PAYOUT', 'COMPLETED', 'Thanh toán kỳ 11/05 - Store 3 (đã trả)', '2026-05-11 12:30:00', '2026-05-11 18:00:00');

-- ============================================
-- PHẦN 10: DISPUTES
-- ============================================

INSERT INTO disputes (user_id, order_id, reason, description, status, evidence_url, admin_note, created_at, updated_at) VALUES
(@buyer_id, @o2_id,  'Sản phẩm không đúng mô tả', 'Tôi đặt Phở Bò Tái Chín nhưng nhận được Bún Chả. Yêu cầu hoàn tiền.', 'PENDING', '/uploads/evidence-dispute-1.png', NULL, '2026-05-16 08:00:00', '2026-05-16 08:00:00'),
(@buyer_id, @o8_id,  'Thức ăn bị hỏng/ôi thiu', 'Chè Thái bị chua, có mùi lạ. Tôi nghi ngờ đã để qua đêm.', 'PENDING', '/uploads/evidence-dispute-2.png', NULL, '2026-05-16 07:30:00', '2026-05-16 07:30:00'),
(@buyer_id, @o1_id,  'Thiếu topping/món', 'Tôi đặt Bún Chả nhưng thiếu nem rán đi kèm như mô tả.', 'REVIEWING', NULL, 'Đang liên hệ cửa hàng xác minh', '2026-05-15 14:00:00', '2026-05-16 09:00:00'),
(@buyer_id, @o20_id, 'Đơn hàng bị hủy không lý do', 'Cửa hàng tự hủy đơn mà không báo trước. Tôi đã chờ hơn 2 tiếng.', 'RESOLVED_REFUND', '/uploads/evidence-dispute-3.png', 'Hoàn tiền 60,000đ. Cảnh cáo cửa hàng.', '2026-05-13 15:00:00', '2026-05-14 10:00:00'),
(@buyer_id, @o13_id, 'Yêu cầu hoàn tiền vì không hài lòng', 'Bánh bị vỡ khi nhận, không đẹp như hình.', 'RESOLVED_REJECTED', '/uploads/evidence-dispute-4.png', 'Bánh hơi vỡ do vận chuyển, không ảnh hưởng chất lượng. Cửa hàng đã có ghi chú.', '2026-05-14 16:00:00', '2026-05-15 11:00:00');

-- ============================================
-- PHẦN 11: REVIEWS + CẬP NHẬT STORE RATINGS
-- ============================================

INSERT INTO reviews (user_id, store_id, product_id, order_id, rating, comment, verified, created_at) VALUES
-- Store 1 (abcbcbcbc)
(@buyer_id, @store1_id, @p1_pho_bo,   @o1_id,  5, 'Phở ngon, nước dùng đậm đà. Sẽ quay lại!',      b'1', '2026-05-14 13:00:00'),
(@buyer_id, @store1_id, @p1_bun_cha,  @o2_id,  4, 'Bún chả ngon nhưng hơi ít thịt',                 b'1', '2026-05-15 12:00:00'),
(@buyer_id, @store1_id, @p1_pho_bo,   @o20_id, 3, 'Tạm được, giá hơi cao so với chất lượng',        b'1', '2026-05-13 13:00:00'),
(@buyer_id, @store1_id, @p1_banhmi,   @o17_id, 5, 'Bánh mì thịt nướng rất ngon!',                   b'1', '2026-05-10 13:00:00'),
-- Store 3 (bcbc)
(@buyer_id, @store3_id, @p3_com_tam,  @o7_id,  5, 'Cơm tấm ngon đúng điệu, đầy đặn',                b'1', '2026-05-14 13:00:00'),
(@buyer_id, @store3_id, @p3_che_thai, @o8_id,  4, 'Chè Thái ngon nhưng hơi ngọt',                   b'1', '2026-05-15 11:30:00'),
(@buyer_id, @store3_id, @p3_banh_da,  @o21_id, 5, 'Bánh đa cua tuyệt vời! Chuẩn Hải Phòng',         b'1', '2026-05-14 13:00:00'),
-- Store 4 (Tiệm bánh nhà làm)
(@buyer_id, @store4_id, @p4_banh_dua, @o12_id, 5, 'Bánh dứa ngon xuất sắc, nhân nhiều!',            b'1', '2026-05-16 10:30:00'),
(@buyer_id, @store4_id, @p4_bong_lan, @o13_id, 4, 'Bánh bông lan mềm, ngon tuyệt',                  b'1', '2026-05-15 16:00:00'),
(@buyer_id, @store4_id, @p4_bong_lan, @o22_id, 3, 'Lần này bánh hơi khô, không được như lần trước', b'1', '2026-05-15 13:00:00');

-- Cập nhật store ratings
-- Store 1: (5+4+3+5)/4 = 4.25
-- Store 3: (5+4+5)/3   = 4.67
-- Store 4: (5+4+3)/3   = 4.00
UPDATE stores SET average_rating = 4.25, total_reviews = 4 WHERE store_id = @store1_id;
UPDATE stores SET average_rating = 4.67, total_reviews = 3 WHERE store_id = @store3_id;
UPDATE stores SET average_rating = 4.00, total_reviews = 3 WHERE store_id = @store4_id;

-- ============================================
-- PHẦN 12: VERIFICATION
-- ============================================
SELECT '============================================' AS '';
SELECT 'DEALXANH - TEST DATA VERIFICATION' AS '';
SELECT '============================================' AS '';

SELECT CONCAT('Categories: ', COUNT(*)) AS '' FROM categories;

SELECT '--- PRODUCTS BY STATUS ---' AS '';
SELECT approval_status, COUNT(*) AS count FROM products GROUP BY approval_status;

SELECT '--- DEALS BY TYPE & STATUS ---' AS '';
SELECT deal_type, status, COUNT(*) AS count FROM deals GROUP BY deal_type, status ORDER BY deal_type, status;

SELECT '--- ORDERS BY STATUS ---' AS '';
SELECT status, COUNT(*) AS count FROM orders GROUP BY status ORDER BY status;

SELECT '--- ORDERS BY STORE ---' AS '';
SELECT s.store_name, COUNT(o.order_id) AS order_count,
       COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN o.final_amount ELSE 0 END), 0) AS revenue
FROM stores s LEFT JOIN orders o ON s.store_id = o.store_id
GROUP BY s.store_id, s.store_name ORDER BY revenue DESC;

SELECT '--- TRANSACTIONS ---' AS '';
SELECT type, status, COUNT(*) AS count, COALESCE(SUM(amount), 0) AS total_amount
FROM transactions GROUP BY type, status ORDER BY type, status;

SELECT '--- DISPUTES BY STATUS ---' AS '';
SELECT status, COUNT(*) AS count FROM disputes GROUP BY status ORDER BY status;

SELECT '--- REVIEWS ---' AS '';
SELECT s.store_name, COUNT(r.review_id) AS review_count,
       ROUND(AVG(r.rating), 2) AS avg_rating
FROM stores s LEFT JOIN reviews r ON s.store_id = r.store_id
WHERE r.review_id IS NOT NULL
GROUP BY s.store_id, s.store_name;

SELECT '============================================' AS '';
SELECT 'DATA INJECTION COMPLETE!' AS '';
SELECT '============================================' AS '';

-- ============================================
-- OPTIONAL: CLEANUP
-- ============================================
-- DELETE FROM reviews WHERE user_id = @buyer_id;
-- DELETE FROM disputes WHERE user_id = @buyer_id;
-- DELETE FROM transactions WHERE store_id IN (@store1_id, @store3_id, @store4_id);
-- DELETE FROM order_items WHERE order_id IN (SELECT order_id FROM orders WHERE user_id = @buyer_id);
-- DELETE FROM orders WHERE user_id = @buyer_id;
-- DELETE FROM deal_products WHERE deal_id IN (@d1_id, @d2_id, @d3_id, @d4_id, @d5_id, @d6_id, @d7_id, @d8_id, @d9_id, @d10_id);
-- DELETE FROM deal_categories WHERE deal_id IN (@d1_id, @d2_id, @d3_id, @d4_id, @d5_id, @d6_id, @d7_id, @d8_id, @d9_id, @d10_id);
-- DELETE FROM deals WHERE created_by = @admin_id;
-- DELETE FROM products WHERE store_id IN (@store1_id, @store3_id, @store4_id);
-- DELETE FROM categories WHERE category_id IN (@cat_savory, @cat_sweet, @cat_drinks, @cat_packaged, @cat_snacks, @cat_rice);
