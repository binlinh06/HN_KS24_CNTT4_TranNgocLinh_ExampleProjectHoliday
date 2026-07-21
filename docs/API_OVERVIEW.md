# Tổng quan RESTful API (API Overview)

Tất cả các endpoint API được chuẩn hóa theo chuẩn RESTful JSON: `/api/v1/...`.

---

## 1. Authentication & Security
- `POST /api/v1/auth/register` - Đăng ký tài khoản Khách hàng
- `POST /api/v1/auth/login` - Đăng nhập (Trả về JWT Access Token & Refresh Token Cookie)
- `POST /api/v1/auth/refresh-token` - Cấp lại Access Token mới
- `POST /api/v1/auth/logout` - Đăng xuất & hủy Token

---

## 2. Menu & Order APIs (Public & Customer)
- `GET /api/v1/categories` - Danh sách danh mục món
- `GET /api/v1/products` - Danh sách sản phẩm (có phân trang & bộ lọc)
- `GET /api/v1/products/{id}` - Chi tiết sản phẩm & danh sách option
- `POST /api/v1/customer/orders` - Đặt hàng trực tuyến
- `GET /api/v1/customer/orders/{id}` - Chi tiết đơn hàng
- `GET /api/v1/sse/orders/{id}/tracking` - Real-time SSE tracking đơn hàng

---

## 3. POS & Kitchen APIs (Staff)
- `POST /api/v1/staff/pos/orders` - Tạo đơn POS tại bàn
- `POST /api/v1/staff/pos/orders/{orderId}/payments` - Thanh toán đơn POS
- `GET /api/v1/staff/kitchen/queue` - Danh sách hàng đợi món tại Bếp
- `PUT /api/v1/staff/kitchen/queue/{id}/status` - Cập nhật trạng thái món bếp
- `GET /api/v1/staff/tables` - Danh sách sơ đồ bàn ăn
- `POST /api/v1/staff/attendance/check-in` - Check-in ca làm
- `POST /api/v1/staff/attendance/check-out` - Check-out ca làm

---

## 4. Manager APIs
- `GET /api/v1/manager/employees` - Danh sách nhân viên (phân trang)
- `POST /api/v1/manager/employees` - Tạo tài khoản nhân viên
- `PUT /api/v1/manager/employees/{id}` - Cập nhật thông tin nhân viên
- `GET /api/v1/manager/vouchers` - Quản lý voucher
- `GET /api/v1/manager/reviews` - Kiểm duyệt đánh giá khách hàng
- `GET /api/v1/manager/reports/overview` - Báo cáo tổng quan doanh thu
- `GET /api/v1/manager/reports/export-csv` - Xuất CSV báo cáo doanh thu
- `GET /api/v1/manager/inventory/ingredients` - Quản lý nguyên liệu & tồn kho

---

## 5. Admin APIs
- `GET /api/v1/admin/users` - Quản lý toàn bộ người dùng hệ thống
- `PUT /api/v1/admin/users/{id}/status` - Đổi trạng thái tài khoản (chống tự khóa)
- `GET /api/v1/admin/system-configs` - Danh sách cấu hình hệ thống
- `PUT /api/v1/admin/system-configs/{key}` - Cập nhật cấu hình whitelist
- `GET /api/v1/admin/audit-logs` - Tra cứu nhật ký kiểm toán hệ thống
