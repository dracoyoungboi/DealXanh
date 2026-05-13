-- Create deal_categories table for linking deals with categories
CREATE TABLE IF NOT EXISTS deal_categories (
    deal_category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    deal_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    priority INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (deal_id) REFERENCES deals(deal_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE,
    INDEX idx_deal_id (deal_id),
    INDEX idx_category_id (category_id),
    INDEX idx_deal_category (deal_id, category_id),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment
ALTER TABLE deal_categories COMMENT = 'Link deals with categories - Platform deals apply to multiple categories';
