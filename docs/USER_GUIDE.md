# Hướng dẫn Sử dụng Chức năng Hệ thống (User Guide)

Hệ thống hỗ trợ 4 vai trò người dùng chính: **CUSTOMER** (Khách hàng), **STAFF** (Nhân viên vận hành POS/Bếp), **MANAGER** (Quản lý cửa hàng), **ADMIN** (Quản trị hệ thống).

---

## 1. Dành cho Khách hàng (CUSTOMER)
- **Xem Menu & Tìm kiếm**: Duyệt sản phẩm theo danh mục, tìm kiếm và lọc theo khoảng giá.
- **Giỏ hàng & Khuyến mãi**: Chọn món, tùy chỉnh topping/tùy chọn (độ mặn, thêm quẩy...), nhập mã voucher giảm giá.
- **Đặt hàng & Thanh toán**: Chọn phương thức thanh toán COD hoặc Ví điện tử / Online Payment Gateway.
- **Theo dõi Đơn hàng (Real-time SSE)**: Theo dõi trạng thái đơn hàng từ `CHO_XAC_NHAN` -> `DANG_CHE_BIEN` -> `DANG_GIAO` -> `HOAN_THANH`.
- **Đánh giá Sản phẩm**: Gửi đánh giá sao (1-5★) và bình luận sau khi hoàn thành đơn hàng.

---

## 2. Dành cho Nhân viên (STAFF - POS / Waiter / Kitchen)
- **Tạo đơn POS tại bàn**: Chọn bàn, chọn món, ghi chú yêu cầu riêng của khách.
- **Bàn ăn (Table Management)**: Xem trạng thái bàn (TRONG, DANG_SU_DUNG, DA_DAT), gộp bàn, chuyển bàn.
- **Màn hình Bếp (Kitchen Queue)**: Theo dõi danh sách món ăn cần chế biến theo thứ tự ưu tiên thời gian, cập nhật trạng thái `READY` / `SERVED`.
- **Thanh toán tại quầy**: Xác nhận thanh toán tiền mặt/chuyển khoản và in hóa đơn cho khách.
- **Điểm danh ca làm (Attendance)**: Thực hiện Check-in / Check-out ca làm việc đầu và cuối ca.

---

## 3. Dành cho Quản lý Cửa hàng (MANAGER)
- **Quản lý Nhân viên**: Tạo tài khoản Staff, gán chức danh (Cashier/Kitchen/Waiter), tạm dừng tài khoản.
- **Quản lý Thực đơn & Khuyến mãi**: Thêm/sửa/ẩn món ăn, danh mục, tùy chọn món và quản lý mã giảm giá Voucher.
- **Duyệt Đánh giá**: Kiểm duyệt các đánh giá của khách hàng trước khi hiển thị công khai.
- **Quản lý Ca làm & Phân công**: Tạo ca mẫu, phân công nhân viên vào ca làm việc theo ngày.
- **Quản lý Kho Nguyên liệu**: Nhập kho, xuất kho, điều chỉnh số lượng tồn kho và cảnh báo dưới ngưỡng tối thiểu.
- **Báo cáo Doanh thu & Vận hành**: Xem báo cáo tổng quan doanh thu, đơn hàng, mặt hàng bán chạy và xuất dữ liệu báo cáo dạng CSV.

---

## 4. Dành cho Quản trị viên (ADMIN)
- **Quản lý Tài khoản Quản trị & Người dùng**: Cấp quyền Admin, Manager, kiểm soát tài khoản khóa/vô hiệu hóa (Có cơ chế chống tự khóa tài khoản Admin duy nhất).
- **Cấu hình Cửa hàng & Hệ thống**: Chỉnh sửa các thông số tên cửa hàng, hotline, phí giao hàng, địa chỉ.
- **Nhật ký Kiểm toán (Audit Logs)**: Tra cứu lịch sử thao tác hệ thống của tất cả các tài khoản quản trị để bảo đảm an toàn thông tin.
