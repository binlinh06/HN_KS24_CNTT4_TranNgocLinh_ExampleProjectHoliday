# Hướng dẫn Thiết lập Môi trường Phát triển (Local Setup)

## 1. Yêu cầu Tiền đề
- **JDK**: Java 21 LTS
- **Node.js**: Node 18 LTS hoặc Node 20 LTS
- **Package Manager**: `pnpm` (`npm install -g pnpm`)
- **Database**: MySQL 8.0+
- **Docker & Docker Compose**: (Tùy chọn cho môi trường containerized)

---

## 2. Cấu hình Cơ sở Dữ liệu Local

1. Tạo database `pho_bo_management` trong MySQL 8.0:
```sql
CREATE DATABASE pho_bo_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Cấu hình biến môi trường local trong `.env` (tham khảo `.env.example`):
```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/pho_bo_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password
```

---

## 3. Chạy Backend Services

```bash
cd backend

# Chạy Unit & Integration Test Suite (56 tests)
$env:SPRING_DATASOURCE_PASSWORD="your_password"; .\gradlew.bat test

# Khởi chạy Spring Boot Dev Server
$env:SPRING_DATASOURCE_PASSWORD="your_password"; .\gradlew.bat bootRun
```

Backend REST API sẽ sẵn sàng tại `http://localhost:8080`.
OpenAPI Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

---

## 4. Chạy Frontend Web App

```bash
cd frontend

# Cài đặt dependencies
pnpm install

# Kiểm tra linting
pnpm lint

# Khởi chạy Next.js Dev Server
pnpm dev
```

Frontend UI sẽ khả dụng tại `http://localhost:3000`.
