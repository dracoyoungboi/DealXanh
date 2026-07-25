-- V12: Dedup deal_products + add unique constraint
-- Ngày: 2026-06-28
-- Vấn đề: deal_products có nhiều dòng trùng deal_id + product_id
--   gây lỗi "Query did not return a unique result: N results were returned"
--   khi gọi findByDealAndProduct()

-- Bước 1: Xóa toàn bộ duplicate rows, giữ lại 1 bản ghi duy nhất cho mỗi cặp (deal_id, product_id)
--         Giữ bản ghi có deal_product_id NHỎ NHẤT (tạo sớm nhất)
DELETE dp
FROM deal_products dp
INNER JOIN (
    SELECT deal_id, product_id, MIN(deal_product_id) AS keep_id
    FROM deal_products
    GROUP BY deal_id, product_id
    HAVING COUNT(*) > 1
) dup ON dp.deal_id = dup.deal_id
    AND dp.product_id = dup.product_id
    AND dp.deal_product_id != dup.keep_id;

-- Bước 2: Thêm unique constraint để ngăn duplicate trong tương lai
ALTER TABLE deal_products
ADD CONSTRAINT uq_deal_product UNIQUE (deal_id, product_id);
