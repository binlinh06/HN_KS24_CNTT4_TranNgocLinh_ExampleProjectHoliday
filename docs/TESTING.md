# Hướng dẫn Kiểm thử Hệ thống (Testing Strategy & Execution)

Hệ thống được bảo vệ bởi bộ kiểm thử tự động toàn diện trên cả Backend (JUnit 5, Mockito, Spring Boot Test) và Frontend (TypeScript, ESLint, Next.js Build Validation).

---

## 1. Kiểm thử Backend (Backend Testing Suite)

### Chạy Toàn Bộ Test Suite
```bash
cd backend
$env:SPRING_DATASOURCE_PASSWORD="your_password"; .\gradlew.bat clean test
```

### Kết quả Kiểm thử
- **Tổng số Test Cases**: **56 Tests** (Bao gồm Phase 1–7 regression tests và Phase 8 integration tests).
- **Tỷ lệ thành công**: **100% Passed (0 Failures, 0 Skipped)**.

### Danh mục Kịch bản Test trong `Phase8AdminManagementTest.java`:
1. `testEmployeeManagement`: Thêm, sửa, vô hiệu hóa tài khoản nhân viên và kiểm tra tự động sinh mã `employeeCode`.
2. `testVoucherManagement`: Tạo mã voucher, cập nhật thông tin và bảo toàn dữ liệu `usedCount`.
3. `testReviewModeration`: Duyệt và từ chối đánh giá khách hàng kèm lý do.
4. `testRevenueReporting`: Báo cáo doanh thu chỉ tính các đơn có `PaymentStatus.SUCCESS` và loại trừ đơn đã hủy.
5. `testShiftOverlapDetection`: Phát hiện và từ chối phân công ca làm việc bị trùng lặp thời gian.
6. `testAttendanceCheckIn`: Điểm danh check-in, check-out và từ chối check-in 2 lần liên tiếp.
7. `testInventoryStockManagement`: Nhập/xuất kho và bảo vệ tồn kho không được âm.
8. `testSystemConfigWhitelist`: Kiểm tra cấu hình whitelist keys và chặn truy cập secret keys.
9. `testAdminSelfLockoutProtection`: Chống tự khóa tài khoản Admin.
10. `testConcurrentAttendanceCheckIn`: Kiểm thử đa luồng đồng thời (Multithreaded concurrency test) đảm bảo duy nhất 1 check-in thành công khi 2 request check-in cùng millisecond.

---

## 2. Kiểm thử Frontend (Frontend Verification)

```bash
cd frontend

# Kiểm tra Syntax & Rule Linting
pnpm lint

# Production Static Build Generation
pnpm build
```

### Kết quả Kiểm thử Frontend:
- **Linting**: 0 Errors.
- **Production Build**: **38/38 static & dynamic pages generated 100% cleanly**.
