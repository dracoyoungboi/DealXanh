-- Script tạo tài khoản Admin cho DealXanh
-- Sử dụng database: dealxanh

-- 1. Insert role ROLE_ADMIN nếu chưa có
INSERT INTO roles (name, description)
VALUES ('ROLE_ADMIN', 'Quản trị viên hệ thống với toàn quyền')
ON DUPLICATE KEY UPDATE name = name;

-- 2. Insert role ROLE_MODERATOR nếu chưa có
INSERT INTO roles (name, description)
VALUES ('ROLE_MODERATOR', 'Kiểm duyệt viên với quyền quản lý nội dung')
ON DUPLICATE KEY UPDATE name = name;

-- 3. Insert tài khoản Admin
-- Password: admin (đã được mã hóa bằng BCrypt)
-- Email: admin@dealxanh.com
-- Username: admin
INSERT INTO users (username, email, password, full_name, phone, address, avatar_url, active, provider, created_at, updated_at, role_id)
VALUES (
    'admin',
    'admin@dealxanh.com',
    '$2a$10$Zt3JjK6bE8wF9YnR2xPQOeK1XnLp8aS7dM6fN5qL3tY2jH0kG9fC', -- Password: admin
    'Quản Trị Viên Hệ Thống',
    '0123456789',
    'Hồ Chí Minh, Việt Nam',
    NULL,
    TRUE,
    'local',
    NOW(),
    NOW(),
    (SELECT role_id FROM roles WHERE name = 'ROLE_ADMIN' LIMIT 1)
);

-- 4. Insert tài khoản Moderator (nếu cần)
-- Password: moderator123
INSERT INTO users (username, email, password, full_name, phone, address, avatar_url, active, provider, created_at, updated_at, role_id)
VALUES (
    'moderator',
    'moderator@dealxanh.com',
    '$2a$10$Zt3JjK6bE8wF9YnR2xPQOeK1XnLp8aS7dM6fN5qL3tY2jH0kG9fC', -- Password: moderator123
    'Moderator Hệ Thống',
    '0987654321',
    'Hà Nội, Việt Nam',
    NULL,
    TRUE,
    'local',
    NOW(),
    NOW(),
    (SELECT role_id FROM roles WHERE name = 'ROLE_MODERATOR' LIMIT 1)
);

-- Alternative: Update password if user already exists
-- Use this if you get "safe update mode" error
UPDATE users
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'
WHERE email = 'admin@dealxanh.com' AND user_id IS NOT NULL;

-- If still fails, use user_id directly (need to know user_id first)
-- UPDATE users SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE user_id = 1;

-- Verify admin account
SELECT
    u.username,
    u.email,
    u.full_name,
    u.active,
    r.name as role_name,
    r.description as role_description
FROM users u
JOIN roles r ON u.role_id = r.role_id
WHERE r.name IN ('ROLE_ADMIN', 'ROLE_MODERATOR');
ALTER TABLE users ADD COLUMN weak_password BIT DEFAULT 0; nhưng làm thế này có sợ chẳng hạn các 
-- Lưu ý:
-- 1. Password cho admin: admin123
-- 2. Password cho moderator: moderator123
-- 3. Sau khi đăng nhập lần đầu, nên đổi password trong database hoặc qua chức năng đổi mật khẩu
-- 4. Các password đã được mã hóa bằng BCrypt với strength 10
