# Sơ đồ và Thiết kế Cơ sở Dữ liệu (Database Design)

Cơ sở dữ liệu MySQL 8 `pho_bo_management` bao gồm 10 bản di cư Flyway (`V1__...sql` đến `V10__...sql`).

---

## 1. Lịch sử Di cư Flyway (Migrations V1-V10)

| Script Migration | Mô tả Thay đổi Database |
| :--- | :--- |
| `V1__initial_schema.sql` | Khởi tạo cấu trúc bảng core: users, roles, user_roles, categories, products, product_options, orders, order_items |
| `V2__seed_initial_data.sql` | Seed dữ liệu danh mục mẫu (Phở Bò, Nước Uống, Mon Phụ) và tài khoản mặc định |
| `V3__add_options_table.sql` | Bổ sung các tùy chọn món ăn chi tiết |
| `V4__add_cart_and_voucher.sql` | Bổ sung các bảng giỏ hàng (carts, cart_items) và khuyến mãi (vouchers) |
| `V5__add_payments_table.sql` | Bổ sung bảng quản lý lịch sử thanh toán (payments) |
| `V6__add_reviews_and_loyalty.sql` | Bổ sung bảng đánh giá (reviews) và điểm thưởng tích lũy khách hàng |
| `V7__add_tables_and_kitchen_queue.sql` | Bổ sung quản lý bàn ăn (restaurant_tables) và hàng đợi bếp (kitchen_queue) |
| `V8__add_pos_order_support.sql` | Bổ sung cột loại đơn tại bàn / mang về cho POS |
| `V9__fix_kitchen_queue_duplicate_ranking.sql` | Khắc phục duplicate ranking trong hàng đợi bếp |
| `V10__add_management_reporting_shifts_inventory.sql` | Bổ sung audit_logs, work_shifts, shift_assignments, ingredients, inventory_transactions, backfill employee_code & ingredient_code |

---

## 2. Các Bảng Chính Trong Database

- **Core User & Identity**: `users`, `roles`, `user_roles`, `employee_profiles`, `customer_profiles`
- **Menu & Catalog**: `categories`, `products`, `product_options`
- **Shopping & Voucher**: `carts`, `cart_items`, `vouchers`
- **Order & Payment**: `orders`, `order_items`, `payments`
- **Store Operations**: `restaurant_tables`, `kitchen_queue`, `attendance`
- **Shifts & Inventory**: `work_shifts`, `shift_assignments`, `ingredients`, `inventory_transactions`
- **System Audit & Config**: `system_configurations`, `audit_logs`

---

## 3. Chỉ mục Hiệu năng (Indexes)
- Index tìm kiếm đơn hàng: `idx_orders_created_at_status`, `idx_orders_customer_id`
- Index kho nguyên liệu: `idx_ingredients_code`
- Index điểm danh: `idx_attendance_employee_date`
- Index nhật ký kiểm toán: `idx_audit_logs_created_at_action`
