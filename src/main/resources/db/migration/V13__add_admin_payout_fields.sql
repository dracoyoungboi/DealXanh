-- V13: Add admin manual payout processing fields to transactions table
ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS admin_account_number VARCHAR(255) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS admin_note VARCHAR(500) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS processed_by VARCHAR(100) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS processed_at DATETIME(6) DEFAULT NULL;
