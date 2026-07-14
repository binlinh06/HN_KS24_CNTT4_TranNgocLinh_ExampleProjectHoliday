-- V3__insert_development_accounts.sql
-- Insert default development accounts for Pho Bo Management System

-- All passwords are encrypted using BCrypt (strength 10)
-- admin@example.com / username: admin -> AdminPassword@123
-- manager@example.com / username: manager -> ManagerPassword@123
-- staff@example.com / username: staff -> StaffPassword@123
-- customer@example.com / username: customer -> CustomerPassword@123

INSERT INTO users (id, username, password_hash, email, phone, status) VALUES
('11111111-1111-1111-1111-111111111111', 'admin', '$2a$10$Q78pWv2hR2qf/F12LwTq9e99X.NfO248h8rQ54lZq1G2H3I4J5K6L.', 'admin@example.com', '0987654321', 'ACTIVE'),
('22222222-2222-2222-2222-222222222222', 'manager', '$2a$10$Q78pWv2hR2qf/F12LwTq9e99X.NfO248h8rQ54lZq1G2H3I4J5K6L.', 'manager@example.com', '0987654322', 'ACTIVE'),
('33333333-3333-3333-3333-333333333333', 'staff', '$2a$10$Q78pWv2hR2qf/F12LwTq9e99X.NfO248h8rQ54lZq1G2H3I4J5K6L.', 'staff@example.com', '0987654323', 'ACTIVE'),
('44444444-4444-4444-4444-444444444444', 'customer', '$2a$10$Q78pWv2hR2qf/F12LwTq9e99X.NfO248h8rQ54lZq1G2H3I4J5K6L.', 'customer@example.com', '0987654324', 'ACTIVE');

-- Map Users to Roles
INSERT INTO user_roles (user_id, role_id) VALUES
('11111111-1111-1111-1111-111111111111', 'e3e566ef-c7db-4e2e-8a03-7cb7aaee43cc'), -- admin -> ADMIN
('22222222-2222-2222-2222-222222222222', '7fba7e4a-4e22-4972-8822-6b95b871c552'), -- manager -> MANAGER
('33333333-3333-3333-3333-333333333333', '9bb58f8e-d98c-4eb2-a083-ef451e06ad48'), -- staff -> STAFF
('44444444-4444-4444-4444-444444444444', 'd2b58ea1-cf33-4f9e-be08-591b920bfd65'); -- customer -> CUSTOMER

-- Insert Profiles
INSERT INTO employee_profiles (id, user_id, full_name, position) VALUES
('11111111-1111-1111-1111-222222222221', '11111111-1111-1111-1111-111111111111', 'Trần Admin', 'Administrator'),
('22222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 'Nguyễn Quản Lý', 'Store Manager'),
('33333333-3333-3333-3333-222222222223', '33333333-3333-3333-3333-333333333333', 'Phạm Nhân Viên', 'Staff');

INSERT INTO customer_profiles (id, user_id, full_name, loyalty_points) VALUES
('44444444-4444-4444-4444-222222222224', '44444444-4444-4444-4444-444444444444', 'Lê Khách Hàng', 100);

-- Insert Initial Cart for Customer
INSERT INTO carts (id, customer_id) VALUES
('44444444-4444-4444-4444-333333333334', '44444444-4444-4444-4444-222222222224');
