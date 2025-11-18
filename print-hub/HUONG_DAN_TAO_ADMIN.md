# Hướng Dẫn Tạo/Cập Nhật User Admin

## Cách 1: Tự động tạo Admin (Khuyến nghị)

Khi khởi động backend lần đầu, hệ thống sẽ **tự động tạo** một admin user mặc định:

- **Email**: `admin@printshop.com`
- **Password**: `admin123`
- **Role**: `ROLE_ADMIN`
- **Trạng thái**: Đã kích hoạt (isActive = true)

**Lưu ý**: Chỉ tạo nếu chưa tồn tại user với email này.

Sau khi backend khởi động, bạn sẽ thấy thông báo trong console:
```
=========================================
Default Admin User Created:
Email: admin@printshop.com
Password: admin123
=========================================
```

## Cách 2: Cập nhật User hiện có thành Admin qua API

### Yêu cầu:
- Phải đã có ít nhất 1 admin user để gọi API này
- Hoặc sử dụng cách 3 (SQL trực tiếp)

### Endpoint:
```
PUT /api/users/{userId}/roles?roles=ROLE_ADMIN
```

### Ví dụ với cURL:
```bash
curl -X PUT "http://localhost:8080/api/users/1/roles?roles=ROLE_ADMIN" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Ví dụ với JavaScript (Frontend):
```javascript
// Trong Admin page, sau khi đã đăng nhập với admin account
const updateUserToAdmin = async (userId) => {
  try {
    const response = await axios.put(
      `http://localhost:8080/api/users/${userId}/roles?roles=ROLE_ADMIN`,
      {},
      {
        headers: {
          ...authService.getAuthHeader(),
          'Content-Type': 'application/json',
        },
      }
    );
    console.log('User updated to admin:', response.data);
  } catch (error) {
    console.error('Error updating user:', error);
  }
};
```

## Cách 3: Cập nhật trực tiếp trong Database (Nhanh nhất)

### Bước 1: Kết nối database
```sql
USE printshop;
```

### Bước 2: Tìm ID của user và role ADMIN
```sql
-- Tìm user cần cập nhật
SELECT id, email, full_name FROM users WHERE email = 'your-email@example.com';

-- Tìm role_id của ROLE_ADMIN
SELECT id, name FROM roles WHERE name = 'ROLE_ADMIN';
```

### Bước 3: Gán role ADMIN cho user
```sql
-- Thay 1 bằng user_id và 1 bằng admin_role_id từ bước 2
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1)
ON DUPLICATE KEY UPDATE user_id = user_id;
```

### Hoặc nếu muốn xóa role cũ và chỉ giữ ADMIN:
```sql
-- Xóa tất cả roles của user
DELETE FROM user_roles WHERE user_id = 1;

-- Thêm role ADMIN
INSERT INTO user_roles (user_id, role_id)
VALUES (1, (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));
```

### Bước 4: Kiểm tra
```sql
SELECT u.id, u.email, u.full_name, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.email = 'your-email@example.com';
```

Kết quả mong đợi:
```
+----+------------------------+---------------+------------+
| id | email                  | full_name     | role       |
+----+------------------------+---------------+------------+
|  1 | your-email@example.com | Your Name     | ROLE_ADMIN |
+----+------------------------+---------------+------------+
```

## Cách 4: Tạo Admin User mới qua SQL

Xem file `CREATE_ADMIN_USER.sql` để có script SQL đầy đủ.

## Sau khi tạo/cập nhật Admin

1. **Restart backend** (nếu dùng cách 3 hoặc 4)
2. **Đăng nhập** với tài khoản admin:
   - Vào `/login`
   - Nhập email và password
3. **Truy cập Admin Panel**:
   - Vào `/admin`
   - Bạn sẽ thấy các tabs: Products, Categories, Brands, **Mã Giảm Giá**, **Khuyến Mãi**, Users

## Lưu ý quan trọng

⚠️ **Bảo mật**:
- Đổi password mặc định `admin123` ngay sau lần đăng nhập đầu tiên
- Không chia sẻ thông tin đăng nhập admin
- Sử dụng password mạnh cho tài khoản admin

⚠️ **Password Hash**:
- Nếu tạo admin qua SQL, cần hash password bằng BCrypt
- Password `admin123` đã được hash: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
- Hoặc sử dụng cách 1 (tự động) hoặc cách 2 (API) để tránh phải hash thủ công

## Troubleshooting

### Lỗi 403 Forbidden khi vào /admin
- Kiểm tra user đã có role `ROLE_ADMIN` chưa
- Kiểm tra JWT token có hợp lệ không
- Đảm bảo đã đăng nhập với tài khoản admin

### Không thấy admin user được tạo tự động
- Kiểm tra console log khi khởi động backend
- Kiểm tra database xem đã có user với email `admin@printshop.com` chưa
- Nếu đã có, hệ thống sẽ không tạo lại

### User có nhiều roles
- User có thể có cả `ROLE_ADMIN` và `ROLE_CUSTOMER`
- Chỉ cần có `ROLE_ADMIN` là đủ để truy cập admin panel


