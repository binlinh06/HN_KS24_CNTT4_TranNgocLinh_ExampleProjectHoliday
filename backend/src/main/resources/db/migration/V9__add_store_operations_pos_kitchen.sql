-- V9__add_store_operations_pos_kitchen.sql

-- 1. Create table for archiving duplicate kitchen queue items
CREATE TABLE kitchen_queue_migration_conflicts (
    id CHAR(36) NOT NULL,
    original_queue_id CHAR(36) NULL,
    order_item_id CHAR(36) NULL,
    item_status VARCHAR(50) NULL,
    created_at DATETIME NULL,
    archived_at DATETIME NOT NULL,
    conflict_reason VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Rank, archive, and delete duplicate kitchen queue records
-- Insert duplicates (rn > 1) into conflicts table
INSERT INTO kitchen_queue_migration_conflicts (id, original_queue_id, order_item_id, item_status, created_at, archived_at, conflict_reason)
SELECT 
    UUID(), 
    kq.id, 
    kq.order_item_id, 
    kq.item_status, 
    kq.created_at, 
    NOW(), 
    'DUPLICATE_ORDER_ITEM_ID'
FROM kitchen_queue kq
JOIN (
    SELECT id, ROW_NUMBER() OVER (
        PARTITION BY order_item_id 
        ORDER BY updated_at DESC, created_at DESC, id DESC
    ) as rn
    FROM kitchen_queue
) ranked ON kq.id = ranked.id
WHERE ranked.rn > 1;

-- Delete duplicates from main table
DELETE FROM kitchen_queue 
WHERE id IN (
    SELECT temp_id FROM (
        SELECT id as temp_id 
        FROM (
            SELECT id, ROW_NUMBER() OVER (
                PARTITION BY order_item_id 
                ORDER BY updated_at DESC, created_at DESC, id DESC
            ) as rn
            FROM kitchen_queue
        ) ranked
        WHERE ranked.rn > 1
    ) sub
);

-- 3. Add UNIQUE constraint to kitchen_queue.order_item_id
ALTER TABLE kitchen_queue ADD CONSTRAINT uk_kitchen_queue_item UNIQUE (order_item_id);

-- 4. Enhance kitchen_queue with execution tracking
ALTER TABLE kitchen_queue 
ADD COLUMN priority INT NOT NULL DEFAULT 0,
ADD COLUMN claimed_by_employee_id CHAR(36) NULL,
ADD COLUMN started_at DATETIME NULL,
ADD COLUMN completed_at DATETIME NULL,
ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
ADD CONSTRAINT fk_kitchen_queue_employee FOREIGN KEY (claimed_by_employee_id) REFERENCES employee_profiles (id) ON DELETE SET NULL;

-- 5. Enhance tables with version and status update timestamp
ALTER TABLE tables 
ADD COLUMN status_updated_at DATETIME NULL,
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE tables SET status_updated_at = COALESCE(updated_at, created_at, NOW());

-- 6. Create table_sessions table
CREATE TABLE table_sessions (
    id CHAR(36) NOT NULL,
    table_id CHAR(36) NOT NULL,
    order_id CHAR(36) NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    opened_by_employee_id CHAR(36) NOT NULL,
    closed_by_employee_id CHAR(36) NULL,
    opened_at DATETIME NOT NULL,
    closed_at DATETIME NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_table_sessions_table FOREIGN KEY (table_id) REFERENCES tables (id) ON DELETE CASCADE,
    CONSTRAINT fk_table_sessions_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE SET NULL,
    CONSTRAINT fk_table_sessions_opened FOREIGN KEY (opened_by_employee_id) REFERENCES employee_profiles (id) ON DELETE RESTRICT,
    CONSTRAINT fk_table_sessions_closed FOREIGN KEY (closed_by_employee_id) REFERENCES employee_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Create invoices table
CREATE TABLE invoices (
    id CHAR(36) NOT NULL,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    order_id CHAR(36) NOT NULL UNIQUE,
    payment_id CHAR(36) NULL,
    issued_by_employee_id CHAR(36) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    discount_amount DECIMAL(15, 2) NOT NULL,
    final_amount DECIMAL(15, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    issued_at DATETIME NOT NULL,
    print_count INT NOT NULL DEFAULT 0,
    last_printed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_invoices_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_payment FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE SET NULL,
    CONSTRAINT fk_invoices_employee FOREIGN KEY (issued_by_employee_id) REFERENCES employee_profiles (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Create invoice_print_events table for re-print auditing
CREATE TABLE invoice_print_events (
    id CHAR(36) NOT NULL,
    invoice_id CHAR(36) NOT NULL,
    printed_by_employee_id CHAR(36) NOT NULL,
    printed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    print_reason VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ipe_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE CASCADE,
    CONSTRAINT fk_ipe_employee FOREIGN KEY (printed_by_employee_id) REFERENCES employee_profiles (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Enhance orders with operational tracking timestamps and personnel IDs
ALTER TABLE orders
ADD COLUMN accepted_at DATETIME NULL,
ADD COLUMN accepted_by_employee_id CHAR(36) NULL,
ADD COLUMN rejected_at DATETIME NULL,
ADD COLUMN rejected_by_employee_id CHAR(36) NULL,
ADD COLUMN rejection_reason VARCHAR(255) NULL,
ADD COLUMN served_at DATETIME NULL,
ADD COLUMN handed_over_at DATETIME NULL,
ADD CONSTRAINT fk_orders_accepted_employee FOREIGN KEY (accepted_by_employee_id) REFERENCES employee_profiles (id) ON DELETE SET NULL,
ADD CONSTRAINT fk_orders_rejected_employee FOREIGN KEY (rejected_by_employee_id) REFERENCES employee_profiles (id) ON DELETE SET NULL;

-- 10. Enhance payments with POS cash tracking columns
ALTER TABLE payments
ADD COLUMN cash_received DECIMAL(15, 2) NULL,
ADD COLUMN change_amount DECIMAL(15, 2) NULL,
ADD COLUMN received_by_employee_id CHAR(36) NULL,
ADD COLUMN terminal_reference VARCHAR(100) NULL,
ADD COLUMN payment_channel VARCHAR(30) NULL,
ADD CONSTRAINT fk_payments_employee FOREIGN KEY (received_by_employee_id) REFERENCES employee_profiles (id) ON DELETE SET NULL;

-- 11. Add indexes for staff operational query optimization
CREATE INDEX idx_orders_type_status_created ON orders (order_type, status, created_at);
CREATE INDEX idx_orders_table_status ON orders (table_id, status);
CREATE INDEX idx_table_sessions_table_status ON table_sessions (table_id, status);
CREATE INDEX idx_kitchen_queue_status_queued ON kitchen_queue (item_status, created_at);

-- 12. Modify idempotency_records to support staff/POS operations
ALTER TABLE idempotency_records DROP FOREIGN KEY fk_idempotency_customer;
ALTER TABLE idempotency_records MODIFY customer_id CHAR(36) NULL;
