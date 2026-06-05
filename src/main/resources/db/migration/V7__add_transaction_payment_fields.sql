-- V7: Add transactionRef and paymentMethod to transactions table for PayOS integration
ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS transaction_ref VARCHAR(255) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50) DEFAULT NULL;
