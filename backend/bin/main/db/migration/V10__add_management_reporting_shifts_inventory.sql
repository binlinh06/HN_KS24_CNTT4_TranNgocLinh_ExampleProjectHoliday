-- V10__add_management_reporting_shifts_inventory.sql
-- Migration for Phase 8: Audit Logs, Employee Management, Shift Templates, Shift Assignments, Attendance, Inventory, System Config, and Performance Indexes

-- 1. Create audit_logs table
CREATE TABLE audit_logs (
    id CHAR(36) NOT NULL,
    actor_user_id CHAR(36) NULL,
    actor_role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id CHAR(36) NULL,
    result VARCHAR(50) NOT NULL,
    summary TEXT NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_audit_actor (actor_user_id, action),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Enhance employee_profiles
ALTER TABLE employee_profiles
ADD COLUMN employee_code VARCHAR(50) NULL,
ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1,
ADD COLUMN version INT NOT NULL DEFAULT 0;

-- Backfill employee_code deterministically for legacy records
UPDATE employee_profiles ep
JOIN (
    SELECT id, CONCAT('EMP-', LPAD(ROW_NUMBER() OVER (ORDER BY created_at ASC, id ASC), 4, '0')) AS generated_code
    FROM employee_profiles
) ranked ON ep.id = ranked.id
SET ep.employee_code = ranked.generated_code
WHERE ep.employee_code IS NULL;

-- Backfill is_active based on associated user status/deleted_at
UPDATE employee_profiles ep
JOIN users u ON ep.user_id = u.id
SET ep.is_active = CASE 
    WHEN u.deleted_at IS NOT NULL OR u.status IN ('DISABLED', 'LOCKED', 'DEACTIVATED') THEN 0 
    ELSE 1 
END;

-- Add UNIQUE constraint to employee_code after safe backfill
ALTER TABLE employee_profiles ADD CONSTRAINT uk_employee_code UNIQUE (employee_code);

-- 3. Enhance reviews
ALTER TABLE reviews
ADD COLUMN moderated_by_user_id CHAR(36) NULL,
ADD COLUMN moderated_at DATETIME NULL,
ADD COLUMN moderation_note TEXT NULL,
ADD COLUMN rejection_reason TEXT NULL,
ADD COLUMN version INT NOT NULL DEFAULT 0,
ADD CONSTRAINT fk_reviews_moderator FOREIGN KEY (moderated_by_user_id) REFERENCES users (id) ON DELETE SET NULL;

-- 4. Enhance vouchers (DO NOT re-add is_active as it was added in V6)
ALTER TABLE vouchers
ADD COLUMN version INT NOT NULL DEFAULT 0;

-- 5. Shift Management: Create work_shifts (templates) & shift_assignments (assignments)
CREATE TABLE work_shifts (
    id CHAR(36) NOT NULL,
    shift_code VARCHAR(50) NOT NULL UNIQUE,
    shift_name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    crosses_midnight TINYINT(1) NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shift_assignments (
    id CHAR(36) NOT NULL,
    shift_id CHAR(36) NOT NULL,
    employee_id CHAR(36) NOT NULL,
    work_date DATE NOT NULL,
    assigned_by_user_id CHAR(36) NOT NULL,
    note TEXT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_employee_shift_assignment UNIQUE (employee_id, work_date, shift_id),
    CONSTRAINT fk_shift_assign_shift FOREIGN KEY (shift_id) REFERENCES work_shifts (id) ON DELETE CASCADE,
    CONSTRAINT fk_shift_assign_employee FOREIGN KEY (employee_id) REFERENCES employee_profiles (id) ON DELETE CASCADE,
    INDEX idx_shift_assign_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed default work_shifts templates if not exist
INSERT INTO work_shifts (id, shift_code, shift_name, start_time, end_time, crosses_midnight, is_active, created_at, updated_at)
SELECT '550e8400-e29b-41d4-a716-446655440001', 'SHIFT_MORNING', 'Ca Sáng (06:00 - 14:00)', '06:00:00', '14:00:00', 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM work_shifts WHERE shift_code = 'SHIFT_MORNING');

INSERT INTO work_shifts (id, shift_code, shift_name, start_time, end_time, crosses_midnight, is_active, created_at, updated_at)
SELECT '550e8400-e29b-41d4-a716-446655440002', 'SHIFT_EVENING', 'Ca Chiều (14:00 - 22:00)', '14:00:00', '22:00:00', 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM work_shifts WHERE shift_code = 'SHIFT_EVENING');

INSERT INTO work_shifts (id, shift_code, shift_name, start_time, end_time, crosses_midnight, is_active, created_at, updated_at)
SELECT '550e8400-e29b-41d4-a716-446655440003', 'SHIFT_NIGHT', 'Ca Đêm (22:00 - 06:00)', '22:00:00', '06:00:00', 1, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM work_shifts WHERE shift_code = 'SHIFT_NIGHT');

-- Migration for legacy V1 shifts table into shift_assignments & update attendance FK
-- First, drop attendance constraint referencing legacy shifts table
ALTER TABLE attendance DROP FOREIGN KEY fk_attendance_shift;

-- Backfill legacy V1 shifts into shift_assignments using SHIFT_MORNING as template default
INSERT INTO shift_assignments (id, shift_id, employee_id, work_date, assigned_by_user_id, note, status, created_at, updated_at)
SELECT 
    s.id, 
    '550e8400-e29b-41d4-a716-446655440001', 
    s.employee_id, 
    DATE(s.start_time), 
    s.employee_id, 
    'Legacy shift assignment migrated from V1', 
    'COMPLETED', 
    s.created_at, 
    s.updated_at
FROM shifts s
WHERE NOT EXISTS (SELECT 1 FROM shift_assignments sa WHERE sa.id = s.id);

-- Re-add attendance FK to point to shift_assignments
ALTER TABLE attendance ADD CONSTRAINT fk_attendance_assignment FOREIGN KEY (shift_id) REFERENCES shift_assignments (id) ON DELETE SET NULL;

-- 6. Enhance attendance (Keep status NULL for legacy records, backfill work_date from check_in)
ALTER TABLE attendance
ADD COLUMN work_date DATE NULL,
ADD COLUMN status VARCHAR(50) NULL,
ADD COLUMN check_in_source VARCHAR(50) NULL DEFAULT 'SYSTEM',
ADD COLUMN check_out_source VARCHAR(50) NULL DEFAULT 'SYSTEM',
ADD COLUMN approved_by_user_id CHAR(36) NULL,
ADD COLUMN approved_at DATETIME NULL,
ADD COLUMN version INT NOT NULL DEFAULT 0;

UPDATE attendance SET work_date = DATE(check_in) WHERE check_in IS NOT NULL AND work_date IS NULL;

-- 7. Enhance ingredients
ALTER TABLE ingredients
ADD COLUMN ingredient_code VARCHAR(50) NULL,
ADD COLUMN current_stock DECIMAL(15,4) NOT NULL DEFAULT 0.0000,
ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1,
ADD COLUMN version INT NOT NULL DEFAULT 0;

-- Backfill ingredient_code deterministically for legacy records
UPDATE ingredients ing
JOIN (
    SELECT id, CONCAT('ING-', LPAD(ROW_NUMBER() OVER (ORDER BY created_at ASC, id ASC), 4, '0')) AS generated_code
    FROM ingredients
) ranked ON ing.id = ranked.id
SET ing.ingredient_code = ranked.generated_code
WHERE ing.ingredient_code IS NULL;

ALTER TABLE ingredients ADD CONSTRAINT uk_ingredient_code UNIQUE (ingredient_code);

-- Seed sample ingredients safely if table is empty
INSERT INTO ingredients (id, ingredient_code, name, unit, min_threshold, current_stock, is_active, created_at, updated_at)
SELECT 'ing-001', 'ING-0001', 'Thịt bò tái', 'kg', 10.0000, 50.0000, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Thịt bò tái');

INSERT INTO ingredients (id, ingredient_code, name, unit, min_threshold, current_stock, is_active, created_at, updated_at)
SELECT 'ing-002', 'ING-0002', 'Bánh phở', 'kg', 20.0000, 100.0000, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Bánh phở');

INSERT INTO ingredients (id, ingredient_code, name, unit, min_threshold, current_stock, is_active, created_at, updated_at)
SELECT 'ing-003', 'ING-0003', 'Xương ống bò', 'kg', 15.0000, 40.0000, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE name = 'Xương ống bò');

-- 8. Enhance inventory_transactions (Keep stock_before & stock_after NULL for legacy records)
ALTER TABLE inventory_transactions
ADD COLUMN stock_before DECIMAL(15,4) NULL,
ADD COLUMN stock_after DECIMAL(15,4) NULL,
ADD COLUMN reference_type VARCHAR(50) NULL,
ADD COLUMN reference_id CHAR(36) NULL,
ADD COLUMN performed_by_employee_id CHAR(36) NULL;

-- 9. Enhance system_configurations
ALTER TABLE system_configurations
ADD COLUMN description VARCHAR(255) NULL,
ADD COLUMN value_type VARCHAR(50) NOT NULL DEFAULT 'STRING',
ADD COLUMN is_public TINYINT(1) NOT NULL DEFAULT 0,
ADD COLUMN version INT NOT NULL DEFAULT 0;

-- Backfill value_type according to configuration key whitelist
UPDATE system_configurations SET value_type = 'DECIMAL' WHERE config_key IN ('default_shipping_fee', 'vat_rate');
UPDATE system_configurations SET value_type = 'INTEGER' WHERE config_key IN ('order_auto_cancel_minutes', 'low_stock_threshold');
UPDATE system_configurations SET value_type = 'BOOLEAN' WHERE config_key IN ('review_public_enabled', 'store_open');
UPDATE system_configurations SET value_type = 'TIME' WHERE config_key IN ('opening_time', 'closing_time');

-- 10. Performance Indexes
CREATE INDEX idx_orders_status_created ON orders (status, created_at);
CREATE INDEX idx_inv_tx_ingredient_created ON inventory_transactions (ingredient_id, created_at);
