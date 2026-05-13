-- Add CHECK constraints for deal validations
-- This migration adds database-level validation for deal rules

-- Check constraint for discount value when type is PERCENT (1% - 85%)
ALTER TABLE deals
ADD CONSTRAINT chk_discount_percent
CHECK (discount_type <> 'PERCENT' OR (discount_value >= 1 AND discount_value <= 85));

-- Check constraint for discount value when type is FIXED (1,000đ - 500,000đ)
ALTER TABLE deals
ADD CONSTRAINT chk_discount_fixed
CHECK (discount_type <> 'FIXED' OR (discount_value >= 1000 AND discount_value <= 500000));

-- Check constraint for time range (end_time > start_time)
ALTER TABLE deals
ADD CONSTRAINT chk_time_range
CHECK (end_time > start_time);

-- Check constraint for deal duration (max 30 days)
-- Note: MySQL doesn't support DATEDIFF in CHECK constraints directly
-- This will be validated at application level instead

-- Check constraint for min order amount (must be >= 0)
ALTER TABLE deals
ADD CONSTRAINT chk_min_order_amount
CHECK (min_order_amount >= 0);

-- Check constraint for max usage count (must be > 0 if specified)
ALTER TABLE deals
ADD CONSTRAINT chk_max_usage_count
CHECK (max_usage_count IS NULL OR max_usage_count > 0);

-- Check constraints for deal_products table
-- Sale price must be positive and less than original price
ALTER TABLE deal_products
ADD CONSTRAINT chk_sale_price_positive
CHECK (sale_price > 0);

-- Sale price must be less than original price
ALTER TABLE deal_products
ADD CONSTRAINT chk_sale_price_less_than_original
CHECK (sale_price < original_price);

-- Sale price must be at least 15% of original price
ALTER TABLE deal_products
ADD CONSTRAINT chk_sale_price_min_percent
CHECK (sale_price >= original_price * 0.15);

-- Max quantity must be positive if specified
ALTER TABLE deal_products
ADD CONSTRAINT chk_max_quantity_positive
CHECK (max_quantity IS NULL OR max_quantity > 0);

-- Sold quantity must be non-negative
ALTER TABLE deal_products
ADD CONSTRAINT chk_sold_quantity_non_negative
CHECK (sold_quantity >= 0);

-- Sold quantity must not exceed max quantity
ALTER TABLE deal_products
ADD CONSTRAINT chk_sold_quantity_not_exceed_max
CHECK (max_quantity IS NULL OR sold_quantity <= max_quantity);
