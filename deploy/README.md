# DealXanh - Deployment Guide

## Cấu trúc thư mục deploy

```
deploy/
│
├── DealXanh.jar          ← File .jar đã build (63MB)
├── start.bat             ← Script chạy trên Windows (click đúp để chạy)
├── start.sh              ← Script chạy trên Linux/Mac
├── README.md             ← File này
│
├── config/
│   └── application.properties  ← CẤU HÌNH PRODUCTION (sửa ở đây, không cần build lại)
│
└── uploads/              ← Thư mục chứa ảnh upload (tự tạo khi chạy)
```

## Cách chạy

### Windows
1. Đảm bảo đã cài **JDK 17+** (hiện tại: JDK 26)
2. **Click đúp `start.bat`** hoặc mở terminal gõ:
   ```
   start.bat
   ```
3. Mở browser: **http://localhost:8084**

### Linux/Mac
```bash
chmod +x start.sh
./start.sh
```

## Build lại .jar khi sửa code

Khi bạn code thêm tính năng mới, build lại bằng lệnh:

```bash
cd DealXanh
./mvnw clean package -DskipTests
```

Sau đó copy file `.jar` mới từ `DealXanh/target/` vào thư mục `deploy/`:

```bash
cp DealXanh/target/DealXanh-0.0.1-SNAPSHOT.jar deploy/DealXanh.jar
```

## Cấu hình Production

Mọi thay đổi cấu hình đều làm trong `config/application.properties`:
- **Database**: `spring.datasource.*` (URL, username, password)
- **Port**: `server.port=8084`
- **Email**: `spring.mail.*` (Gmail SMTP)
- **Google OAuth2**: `spring.security.oauth2.client.registration.google.*`
- **PayOS**: `payos.*` (client-id, api-key, checksum-key)

**Không cần build lại .jar sau khi sửa config!**

## Yêu cầu hệ thống

| Thành phần | Yêu cầu |
|-----------|---------|
| JDK | 17+ (khuyến nghị 21+) |
| MySQL | 8.0+ |
| RAM | Tối thiểu 512MB |
| Disk | ~200MB (jar + uploads) |

## Lưu ý bảo mật

- **KHÔNG** commit file `config/application.properties` lên GitHub (chứa password, API key)
- Thêm `deploy/config/` vào `.gitignore`
- Đổi mật khẩu database + email + PayOS trước khi deploy production thật
