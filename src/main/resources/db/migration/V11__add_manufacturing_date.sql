-- V11: Thêm ngày sản xuất (manufacturing_date) vào products
ALTER TABLE products ADD COLUMN manufacturing_date DATETIME NULL AFTER expiry_date;
