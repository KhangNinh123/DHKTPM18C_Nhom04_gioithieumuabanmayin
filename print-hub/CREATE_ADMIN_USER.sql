-- Script để tạo hoặc cập nhật user thành ADMIN
-- Chạy script này trong database MariaDB/MySQL

-- Cách 1: Tạo user admin mới (nếu chưa có)
-- Thay đổi email và password theo nhu cầu
INSERT INTO users (email, password_hash, full_name, phone, default_address, is_active, created_at, updated_at)
VALUES (
    'admin@printshop.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- Password: admin123 (đã được hash)
    'Administrator',
    '0123456789',
    'Admin Address',
    true,
    NOW(),
    NOW()
);

-- Lấy user_id vừa tạo (hoặc dùng ID của user hiện có)
SET @user_id = LAST_INSERT_ID();
-- Hoặc nếu user đã tồn tại, dùng:
-- SET @user_id = (SELECT id FROM users WHERE email = 'admin@printshop.com');

-- Lấy role_id của ROLE_ADMIN
SET @admin_role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');

-- Gán role ADMIN cho user
INSERT INTO user_roles (user_id, role_id)
VALUES (@user_id, @admin_role_id)
ON DUPLICATE KEY UPDATE user_id = user_id; -- Tránh duplicate nếu đã có

-- ============================================
-- Cách 2: Cập nhật user hiện có thành ADMIN
-- ============================================

-- Bước 1: Tìm ID của user cần cập nhật
-- SELECT id, email FROM users WHERE email = 'your-email@example.com';

-- Bước 2: Lấy role_id của ROLE_ADMIN
-- SELECT id FROM roles WHERE name = 'ROLE_ADMIN';

-- Bước 3: Gán role ADMIN (thay 1 bằng user_id và 1 bằng admin_role_id)
-- INSERT INTO user_roles (user_id, role_id)
-- VALUES (1, 1)
-- ON DUPLICATE KEY UPDATE user_id = user_id;

-- ============================================
-- Cách 3: Xóa role cũ và thêm role ADMIN
-- ============================================

-- Xóa tất cả roles của user
-- DELETE FROM user_roles WHERE user_id = 1;

-- Thêm role ADMIN
-- INSERT INTO user_roles (user_id, role_id)
-- VALUES (1, (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));

-- ============================================
-- Kiểm tra user có role ADMIN
-- ============================================
-- SELECT u.id, u.email, u.full_name, r.name as role
-- FROM users u
-- JOIN user_roles ur ON u.id = ur.user_id
-- JOIN roles r ON ur.role_id = r.id
-- WHERE u.email = 'admin@printshop.com';


