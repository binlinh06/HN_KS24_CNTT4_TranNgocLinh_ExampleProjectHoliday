# HỆ THỐNG QUẢN LÝ VÀ BÁN PHỞ BÒ (PHỞ BÒ MANAGEMENT SYSTEM)

Hệ thống quản lý và bán phở bò toàn diện bao gồm trọn vẹn 8 giai đoạn phát triển:
- **Phase 1-2**: Nền tảng Core Backend (Spring Boot 3.4, Java 21, MySQL 8, Flyway) & Frontend Client Platform (Next.js 14, Tailwind CSS, TypeScript).
- **Phase 3-4**: Quản lý Thực đơn, Danh mục, Tùy chọn món, Giỏ hàng & Voucher Khuyến mãi.
- **Phase 5-6**: Đặt hàng Online, Thanh toán (COD/Online Gateway), Tracking thời gian thực (SSE) & Đánh giá (Review).
- **Phase 7**: Vận hành tại Cửa hàng (POS Bán hàng tại bàn, Màn hình Bếp Kitchen Queue, Quản lý Bàn ăn).
- **Phase 8**: Quản trị Hệ thống, Quản lý Nhân viên, Ca làm việc & Điểm danh, Kho nguyên liệu, Báo cáo Doanh thu (CSV Export) & Nhật ký kiểm toán (Audit Logs).

---

## 🛠️ Công Nghệ Sử Dụng

### Backend
- **Java**: 21
- **Framework**: Spring Boot 3.4.0
- **Database**: MySQL 8.0 / JPA / Hibernate 6
- **Migration**: Flyway DB
- **Security**: Spring Security + JWT (Stateless)
- **Realtime**: Server-Sent Events (SSE)
- **Build Tool**: Gradle Wrapper (`gradlew`)

### Frontend
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Vanilla Tailwind CSS (Design Tokens: Primary `#0F6B4F`, Accent `#C7A45B`, Font `Be Vietnam Pro`)
- **State Management**: TanStack Query (React Query) & Zustand
- **Realtime**: `@microsoft/fetch-event-source`
- **Package Manager**: `pnpm`

---

## 📚 Tài Liệu Dự Án (`docs/`)

- [Thắt lập Môi trường Phát triển (SETUP.md)](docs/SETUP.md)
- [Hướng dẫn Triển khai Production (DEPLOYMENT.md)](docs/DEPLOYMENT.md)
- [Hướng dẫn Sử dụng Chức năng (USER_GUIDE.md)](docs/USER_GUIDE.md)
- [Tổng quan API RESTful (API_OVERVIEW.md)](docs/API_OVERVIEW.md)
- [Thiết kế Cơ sở Dữ liệu (DATABASE.md)](docs/DATABASE.md)
- [Chính sách Bảo mật (SECURITY.md)](docs/SECURITY.md)
- [Hướng dẫn Kiểm thử (TESTING.md)](docs/TESTING.md)

---

## 🚀 Khởi Chạy Nhanh

### 1. Khởi động MySQL Database
```bash
docker-compose up -d mysql
```

### 2. Khởi động Backend Service
```bash
cd backend
$env:SPRING_DATASOURCE_PASSWORD="your_local_password"
.\gradlew.bat bootRun
```

### 3. Khởi động Frontend Application
```bash
cd frontend
pnpm install
pnpm dev
```
Truy cập ứng dụng tại `http://localhost:3000`.
