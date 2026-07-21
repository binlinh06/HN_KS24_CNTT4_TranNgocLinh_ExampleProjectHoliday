-- V2__insert_roles.sql
-- Insert default roles for Pho Bo Management System

INSERT INTO roles (id, code, name, description) VALUES
('e3e566ef-c7db-4e2e-8a03-7cb7aaee43cc', 'ADMIN', 'Quản trị viên', 'Quyền hạn cao nhất hệ thống'),
('7fba7e4a-4e22-4972-8822-6b95b871c552', 'MANAGER', 'Quản lý', 'Quản lý cửa hàng, ca trực, kho và chấm công'),
('9bb58f8e-d98c-4eb2-a083-ef451e06ad48', 'STAFF', 'Nhân viên', 'Nhân viên thu ngân, phục vụ, bếp'),
('d2b58ea1-cf33-4f9e-be08-591b920bfd65', 'CUSTOMER', 'Khách hàng', 'Khách hàng đặt phở online');
