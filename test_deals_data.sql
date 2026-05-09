-- ============================================
-- SCRIPT TEST DỮ LIỆU CHO TRANG DEALS ADMIN
-- DealXanh - Admin Deals Page Test Data
-- ============================================

-- BƯỚC 0: Thêm cột apply_method nếu chưa có (chạy 1 lần)
-- ALTER TABLE deals ADD COLUMN apply_method VARCHAR(20) DEFAULT 'CODE_REQUIRED' AFTER scope;
-- UPDATE deals SET apply_method = 'CODE_REQUIRED' WHERE apply_method IS NULL;

-- BƯỚC 1: TẠO DEALS TEST

-- Flash Sale Deals
INSERT INTO deals (deal_name, deal_code, deal_type, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_count, usage_count, usage_per_user, start_time, end_time, status, scope, apply_method, image_url, banner_url, priority, created_at, updated_at, store_id, created_by)
VALUES
-- Flash Sale 50% - Store 1 (Bánh Mì Như Ý) - CODE_REQUIRED
('Flash Sale 50% Bánh Mì', 'FLASH50_BANHMI', 'FLASH_SALE', 'Giảm giá 50% cho tất cả các loại bánh mì', 'PERCENT', 50.0, 30000.0, 50000.0, 100, 45, 5,
 DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 'ACTIVE', 'SPECIFIC_STORES', 'CODE_REQUIRED',
 '/img/deals/flash-banhmi.jpg', '/img/deals/flash-banhmi-banner.jpg', 100, NOW(), NOW(),
 (SELECT store_id FROM stores WHERE store_name='Bánh Mì Như Ý' LIMIT 1),
 (SELECT user_id FROM users WHERE username='admin_test' LIMIT 1)),

-- Flash Sale 30% - Store 2 (Cà Phê) - CODE_REQUIRED
('Flash Sale 30% Cà Phê', 'FLASH30_CAPHE', 'FLASH_SALE', 'Giảm giá 30% cho tất cả các món cà phê', 'PERCENT', 30.0, 20000.0, 30000.0, 200, 120, 10,
 DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 'ACTIVE', 'SPECIFIC_STORES', 'CODE_REQUIRED',
 '/img/deals/flash-cafe.jpg', '/img/deals/flash-cafe-banner.jpg', 90, NOW(), NOW(),
 (SELECT store_id FROM stores WHERE store_name='Cà Phê Thức Mình' LIMIT 1),
 (SELECT user_id FROM users WHERE username='admin_test' LIMIT 1)),

-- Voucher Freeship 10k - CODE_REQUIRED
('Freeship 10k', 'FREESHIP10K', 'FREESHIP', 'Miễn phí vận chuyển 10k', 'FIXED', 10000.0, NULL, 100000.0, 500, 250, 3,
 NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'SCHEDULED', 'ALL_STORES', 'CODE_REQUIRED',
 '/img/deals/freeship.jpg', '/img/deals/freeship-banner.jpg', 80, NOW(), NOW(),
 NULL, (SELECT user_id FROM users WHERE username='admin_test' LIMIT 1)),

-- Voucher 50k cho đơn > 300k - CODE_REQUIRED
('Voucher 50K', 'VOUCHER50K', 'VOUCHER', 'Giảm 50k cho đơn từ 300k', 'FIXED', 50000.0, NULL, 300000.0, 1000, 680, 2,
 NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 'SCHEDULED', 'ALL_STORES', 'CODE_REQUIRED',
 '/img/deals/voucher50k.jpg', '/img/deals/voucher50k-banner.jpg', 70, NOW(), NOW(),
 NULL, (SELECT user_id FROM users WHERE username='admin_test' LIMIT 1)),

-- Combo Phở + Trà - CODE_REQUIRED
('Combo Phở Trà', 'COMBO_PHO_TRA', 'COMBO', 'Combo Phở + Trà đá giảm 25k', 'FIXED', 25000.0, NULL, 150000.0, 50, 32, NULL,
 DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 'ACTIVE', 'SPECIFIC_STORES', 'CODE_REQUIRED',
 '/img/deals/combo-pho-tra.jpg', '/img/deals/combo-pho-tra-banner.jpg', 60, NOW(), NOW(),
 (SELECT store_id FROM stores WHERE store_name='Phở Bò 3 Mươi' LIMIT 1),
 (SELECT user_id FROM users WHERE username='admin_test' LIMIT 1)),

-- Flash Sale Phở 40% - CODE_REQUIRED
('Flash Sale 40% Phở', 'FLASH40_PHO', 'FLASH_SALE', 'Giảm 40% cho các món Phở', 'PERCENT', 40.0, 40000.0, 100000.0, 150, 0, NULL,
 DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 'SCHEDULED', 'SPECIFIC_STORES', 'CODE_REQUIRED',
 '/img/deals/flash-pho.jpg', '/img/deals/flash-pho-banner.jpg', 95, NOW(), NOW(),
 (SELECT store_id FROM stores WHERE store_name='Phở Bò 3 Mươi' LIMIT 1),
 (SELECT user_id FROM users WHERE username='admin_test' LIMIT 1)),

-- Seasonal Sale 9.9 - AUTO_APPLY (giảm giá trực tiếp)
('Sale 9.9 Siêu Sale', 'SALE99', 'SEASONAL', 'Siêu Sale 9.9 - Giảm đến 60%', 'PERCENT', 60.0, 100000.0, 200000.0, NULL, NULL, NULL,
 DATE_ADD(NOW(), INTERVAL -5 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 'ENDED', 'ALL_STORES', 'AUTO_APPLY',
 '/img/deals/sale99.jpg', '/img/deals/sale99-banner.jpg', 100, NOW(), NOW(),
 NULL, (SELECT user_id FROM users WHERE username='admin_test' LIMIT 1)),

-- Voucher 15% cho đơn mới - CODE_REQUIRED
('Voucher 15% New User', 'NEWUSER15', 'VOUCHER', 'Giảm 15% cho user mới', 'PERCENT', 15.0, 30000.0, 50000.0, 1000, 450, 1,
 NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', 'ALL_STORES', 'CODE_REQUIRED',
 '/img/deals/newuser15.jpg', '/img/deals/newuser15-banner.jpg', 50, NOW(), NOW(),
 NULL, (SELECT user_id FROM users WHERE username='admin_test' LIMIT 1));

-- ============================================
-- BƯỚC 2: XÁC NHẬN DỮ LIỆU ĐÃ INSERT

SELECT '=== DEALS SUMMARY ===' AS '';

SELECT
    deal_id,
    deal_name,
    deal_code,
    deal_type,
    discount_type,
    CASE discount_type WHEN 'PERCENT' THEN CONCAT(discount_value, '%')
         ELSE CONCAT(FORMAT(discount_value, 0), 'đ') END AS discount_display,
    status,
    CASE WHEN start_time > NOW() THEN 'Sắp diễn ra'
         WHEN end_time < NOW() THEN 'Đã kết thúc'
         ELSE 'Đang diễn ra' END AS time_status,
    CONCAT(usage_count, '/', COALESCE(max_usage_count, '∞')) AS usage,
    FORMAT(start_time, 'dd/MM HH:mm') AS start_time,
    FORMAT(end_time, 'dd/MM HH:mm') AS end_time
FROM deals
ORDER BY created_at DESC;

-- ============================================
-- THỐNG KÊ TRANG THÁI

SELECT '=== DEALS BY TYPE ===' AS '';
SELECT
    deal_type,
    COUNT(*) as total_deals,
    SUM(CASE WHEN status='ACTIVE' THEN 1 ELSE 0 END) as active,
    SUM(CASE WHEN status='SCHEDULED' THEN 1 ELSE 0 END) as scheduled,
    SUM(CASE WHEN status='ENDED' THEN 1 ELSE 0 END) as ended
FROM deals
GROUP BY deal_type
ORDER BY total_deals DESC;

SELECT '=== DEALS BY SCOPE ===' AS '';
SELECT
    scope,
    COUNT(*) as total_deals
FROM deals
GROUP BY scope;

-- ============================================
-- XÓA DỮ LIỆU TEST (OPTIONAL)

-- DELETE FROM deal_products WHERE deal_id IN (SELECT deal_id FROM deals WHERE deal_code IN ('FLASH50_BANHMI', 'FLASH30_CAPHE', 'FREESHIP10K', 'VOUCHER50K', 'COMBO_PHO_TRA', 'FLASH40_PHO', 'SALE99', 'NEWUSER15'));
-- DELETE FROM deals WHERE deal_code IN ('FLASH50_BANHMI', 'FLASH30_CAPHE', 'FREESHIP10K', 'VOUCHER50K', 'COMBO_PHO_TRA', 'FLASH40_PHO', 'SALE99', 'NEWUSER15');
