# Chính sách Bảo mật Hệ thống (Security Policy & Architecture)

Hệ thống Quản lý và Bán Phở Bò tuân thủ nghiêm ngặt các tiêu chuẩn bảo mật phát triển phần mềm hiện đại.

---

## 1. Các Nguyên tắc Bảo mật Cốt lõi

1. **Quản lý Mật khẩu & Secret an toàn**:
   - Tất cả mật khẩu người dùng được mã hóa bằng thuật toán `BCryptPasswordEncoder` mạnh mẽ.
   - Không chứa bất kỳ mật khẩu thật, JWT secret thật, payment secret hay API key thật nào trong source code, Git history, documentation hoặc log files.
   - Các file bí mật môi trường (`.env`) được loại trừ hoàn toàn khỏi Git repository (`.gitignore`).

2. **Cơ chế Phân quyền (RBAC) & Phân định Vai trò**:
   - `ROLE_ADMIN`: Toàn quyền cấu hình hệ thống, quản lý người dùng, tra cứu nhật ký kiểm toán. **Không có quyền vận hành nghiệp vụ bán hàng hàng ngày**.
   - `ROLE_MANAGER`: Toàn quyền vận hành cửa hàng (quản lý nhân viên Staff, thực đơn, voucher, duyệt đánh giá, ca làm, kho nguyên liệu, báo cáo doanh thu).
   - `ROLE_STAFF`: Giới hạn nghiệp vụ theo chức danh vị trí chuẩn hóa (`CASHIER`, `KITCHEN`, `WAITER`). Hỗ trợ fallback tương thích legacy dữ liệu "Staff".
   - `ROLE_CUSTOMER`: Chỉ truy cập các tài nguyên cá nhân (đơn hàng, giỏ hàng, đánh giá cá nhân).

3. **Chống Tự khóa & Vô hiệu hóa Tài khoản (Self-Lockout Protection)**:
   - Hệ thống ngăn chặn Admin tự khóa tài khoản của chính mình hoặc vô hiệu hóa tài khoản Admin duy nhất còn lại trong hệ thống.

4. **Chống Tấn công CSV Formula Injection (Excel Macro Injection)**:
   - Tất cả các cột văn bản xuất ra file CSV báo cáo doanh thu (`ReportService`) được tự động escape nếu bắt đầu bằng các ký tự nguy hiểm (`=`, `+`, `-`, `@`).

5. **Concurrency & Data Consistency Protection**:
   - Áp dụng khóa bi quan `PESSIMISTIC_WRITE` cho check-in điểm danh nhân viên (`EmployeeProfile` & `Attendance`) và điều chỉnh kho nguyên liệu (`Ingredient`) để chống xung đột race-condition.

6. **Ngăn chặn Lộ thông tin Nhạy cảm trong API & Logs**:
   - Các DTO phản hồi người dùng tuyệt đối không bao gồm `passwordHash` hay refresh tokens.
   - Trình xử lý ngoại lệ toàn cục (`GlobalExceptionHandler`) làm sạch thông tin lỗi, không trả về raw SQL hay stack traces cho client.
