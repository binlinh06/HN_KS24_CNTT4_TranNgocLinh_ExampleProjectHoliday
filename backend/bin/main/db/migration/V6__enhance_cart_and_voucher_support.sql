-- V6__enhance_cart_and_voucher_support.sql
-- Enhance carts, cart_items, and vouchers tables, and add idempotency_records table

-- 1. Add version column to carts for optimistic locking
ALTER TABLE carts ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 2. Add special_note and unit_price_snapshot to cart_items
ALTER TABLE cart_items ADD COLUMN special_note VARCHAR(150) NULL;
ALTER TABLE cart_items ADD COLUMN unit_price_snapshot DECIMAL(15, 2) NULL;

-- 3. Add applied_voucher_id to carts with foreign key constraint
ALTER TABLE carts ADD COLUMN applied_voucher_id CHAR(36) NULL;
ALTER TABLE carts ADD CONSTRAINT fk_carts_voucher FOREIGN KEY (applied_voucher_id) REFERENCES vouchers(id) ON DELETE SET NULL;

-- 4. Add is_active to vouchers
ALTER TABLE vouchers ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1;

-- 5. Create idempotency_records table
CREATE TABLE idempotency_records (
    id VARCHAR(36) NOT NULL,
    customer_id CHAR(36) NOT NULL,
    idempotency_key CHAR(36) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    request_hash CHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_body LONGTEXT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_idempotency UNIQUE (customer_id, operation, idempotency_key),
    CONSTRAINT fk_idempotency_customer FOREIGN KEY (customer_id) REFERENCES customer_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Create indexes
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);
CREATE INDEX idx_cart_item_options_option ON cart_item_options(option_id);
CREATE INDEX idx_idempotency_expires_at ON idempotency_records(expires_at);

-- 7. Seed sample vouchers safely (idempotent)
INSERT INTO vouchers (id, code, discount_type, discount_value, min_order_value, start_date, end_date, usage_limit, used_count, is_active, created_at, updated_at)
SELECT 'c2657e0f-3151-409f-9293-ee7f4339e10a', 'PHO10', 'PERCENTAGE', 10.00, 100000.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 100, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'PHO10');

INSERT INTO vouchers (id, code, discount_type, discount_value, min_order_value, start_date, end_date, usage_limit, used_count, is_active, created_at, updated_at)
SELECT '7d56fbdf-9b2c-4971-a476-0f8d22384a20', 'GIAM20K', 'FIXED_AMOUNT', 20000.00, 150000.00, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 50, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'GIAM20K');
