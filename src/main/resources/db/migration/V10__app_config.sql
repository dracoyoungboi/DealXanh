CREATE TABLE IF NOT EXISTS app_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO app_config (config_key, config_value) VALUES
('maintenance_mode', 'false'),
('maintenance_message', 'Hệ thống đang bảo trì, vui lòng quay lại sau.'),
('maintenance_end_time', '');
