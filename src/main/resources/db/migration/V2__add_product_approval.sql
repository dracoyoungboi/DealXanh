-- Add approval status columns to products table
ALTER TABLE products
ADD COLUMN approval_status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Trạng thái duyệt: PENDING, APPROVED, REJECTED',
ADD COLUMN rejection_reason TEXT COMMENT 'Lý do từ chối sản phẩm',
ADD INDEX idx_approval_status (approval_status);

-- Update existing products to APPROVED status
UPDATE products SET approval_status = 'APPROVED' WHERE active = true AND deleted = false;
