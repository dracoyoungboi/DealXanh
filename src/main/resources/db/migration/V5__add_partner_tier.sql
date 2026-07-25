ALTER TABLE stores ADD COLUMN partner_tier VARCHAR(20) DEFAULT 'BRONZE' AFTER approval_mode;
UPDATE stores SET partner_tier = 'BRONZE' WHERE partner_tier IS NULL;
