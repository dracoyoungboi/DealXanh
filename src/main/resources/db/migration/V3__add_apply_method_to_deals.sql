-- Add apply_method column to deals table
-- Cách áp dụng deal: CODE_REQUIRED (cần nhập mã) hoặc AUTO_APPLY (tự động áp dụng)

ALTER TABLE deals ADD COLUMN apply_method VARCHAR(20) DEFAULT 'CODE_REQUIRED' AFTER scope;

-- Update existing records
UPDATE deals SET apply_method = 'CODE_REQUIRED' WHERE apply_method IS NULL;

-- For seasonal sales, set to AUTO_APPLY
UPDATE deals SET apply_method = 'AUTO_APPLY' WHERE deal_type = 'SEASONAL';
